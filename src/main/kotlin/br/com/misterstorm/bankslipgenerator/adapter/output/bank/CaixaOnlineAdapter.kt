package br.com.misterstorm.bankslipgenerator.adapter.output.bank

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.port.BankOnlineRegistrationService
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipStatusResponse
import br.com.misterstorm.bankslipgenerator.domain.port.RegistrationResponse
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import io.netty.handler.ssl.SslContextBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

/**
 * Caixa Econômica Federal online registration adapter
 * Uses mTLS (certificate-based authentication)
 */
@Component
class CaixaOnlineAdapter(
    private val webClientBuilder: WebClient.Builder,
    private val logger: Logger,
    @Value("\${app.banks.caixa.apiUrl}") private val apiUrl: String,
    @Value("\${app.banks.caixa.certificatePath:#{null}}") private val certificatePath: String?,
    @Value("\${app.banks.caixa.certificatePassword:#{null}}") private val certificatePassword: String?
) : BankOnlineRegistrationService {

    private val bankCode = "104"
    
    private val sslContext: SSLContext? by lazy {
        if (certificatePath != null && certificatePassword != null) {
            createSSLContext()
        } else {
            logger.warn("Caixa certificate not configured")
            null
        }
    }

    private fun createSSLContext(): SSLContext? {
        return try {
            val keyStore = KeyStore.getInstance("PKCS12")
            FileInputStream(certificatePath).use { fis ->
                keyStore.load(fis, certificatePassword!!.toCharArray())
            }

            val kmf = KeyManagerFactory.getInstance("SunX509")
            kmf.init(keyStore, certificatePassword!!.toCharArray())

            val context = SSLContext.getInstance("TLS")
            context.init(kmf.keyManagers, null, null)

            logger.info("SSL context created successfully for Caixa")
            context
        } catch (e: Exception) {
            logger.error("Failed to create SSL context for Caixa", e)
            null
        }
    }

    override suspend fun register(bankSlip: BankSlip): Either<DomainError, RegistrationResponse> {
        return try {
            if (sslContext == null) {
                return DomainError.UnexpectedError("Caixa certificate not configured").left()
            }

            logger.info("Registering BankSlip with Caixa using certificate", "bankSlipId" to bankSlip.id.toString())

            // Create HttpClient with SSL context
            val keyStore = KeyStore.getInstance("PKCS12")
            FileInputStream(certificatePath).use { fis ->
                keyStore.load(fis, certificatePassword!!.toCharArray())
            }

            val kmf = KeyManagerFactory.getInstance("SunX509")
            kmf.init(keyStore, certificatePassword!!.toCharArray())

            val sslCtx = SslContextBuilder.forClient()
                .keyManager(kmf)
                .build()

            val httpClient = HttpClient.create().secure { it.sslContext(sslCtx) }
            val connector = ReactorClientHttpConnector(httpClient)

            val client = webClientBuilder
                .baseUrl(apiUrl)
                .clientConnector(connector)
                .build()

            val request = mapOf(
                "numero_titulo" to bankSlip.documentNumber,
                "valor" to bankSlip.amount.toString(),
                "data_vencimento" to bankSlip.dueDate.toString(),
                "pagador" to mapOf(
                    "nome" to bankSlip.payer.name,
                    "cpf_cnpj" to bankSlip.payer.documentNumber
                )
            )

            val response = client.post()
                .uri("/boletos")
                .bodyValue(request)
                .retrieve()
                .awaitBody<Map<String, Any>>()

            logger.info("Caixa registration successful")

            RegistrationResponse(
                success = true,
                registrationId = response["nosso_numero"] as? String,
                message = "Registered with Caixa"
            ).right()
        } catch (e: Exception) {
            logger.error("Caixa registration failed", e)
            DomainError.UnexpectedError("Caixa registration failed: ${e.message}", e).left()
        }
    }

    override suspend fun cancel(bankSlip: BankSlip): Either<DomainError, Unit> = Unit.right()

    override suspend fun query(bankSlip: BankSlip): Either<DomainError, BankSlipStatusResponse> =
        BankSlipStatusResponse("REGISTERED").right()

    override fun supports(bankCode: String): Boolean = bankCode == this.bankCode
}
