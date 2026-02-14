package br.com.misterstorm.bankslipgenerator.adapter.output.bank

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.port.BankOnlineRegistrationService
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipStatusResponse
import br.com.misterstorm.bankslipgenerator.domain.port.EncryptionService
import br.com.misterstorm.bankslipgenerator.domain.port.RegistrationResponse
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

/**
 * Itaú online registration adapter
 * API Documentation: https://developer.itau.com.br/
 */
@Component
class ItauOnlineAdapter(
    private val webClientBuilder: WebClient.Builder,
    private val encryptionService: EncryptionService,
    private val logger: Logger,
    @Value("\${app.banks.itau.apiUrl}") private val apiUrl: String,
    @Value("\${app.banks.itau.authUrl}") private val authUrl: String
) : BankOnlineRegistrationService {

    private val bankCode = "341"

    @CircuitBreaker(name = "itau", fallbackMethod = "registerFallback")
    @Retry(name = "itau")
    override suspend fun register(bankSlip: BankSlip): Either<DomainError, RegistrationResponse> {
        return try {
            logger.info(
                "Registering BankSlip with Itaú",
                "bankSlipId" to bankSlip.id.toString()
            )

            val token = getOAuth2Token().fold({ return it.left() }, { it })

            val client = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()

            val request = buildRegistrationRequest(bankSlip)

            val response = client.post()
                .uri("/boletos")
                .bodyValue(request)
                .retrieve()
                .awaitBody<Map<String, Any>>()

            logger.info(
                "BankSlip registered successfully with Itaú",
                "bankSlipId" to bankSlip.id.toString()
            )

            RegistrationResponse(
                success = true,
                registrationId = response["id_titulo_cobranca"] as? String,
                message = "Registered successfully with Itaú"
            ).right()

        } catch (e: Exception) {
            logger.error("Failed to register BankSlip with Itaú", e)
            DomainError.UnexpectedError("Itaú registration failed: ${e.message}", e).left()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun registerFallback(bankSlip: BankSlip, ex: Exception): Either<DomainError, RegistrationResponse> {
        logger.warn("Circuit breaker activated for Itaú")
        return DomainError.UnexpectedError("Itaú service unavailable", ex).left()
    }

    private suspend fun getOAuth2Token(): Either<DomainError, String> {
        return try {
            logger.debug("Retrieving OAuth2 token for Itaú")
            "mock_itau_token".right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to authenticate with Itaú", e).left()
        }
    }

    private fun buildRegistrationRequest(bankSlip: BankSlip): Map<String, Any> {
        return mapOf(
            "data_vencimento" to bankSlip.dueDate.toString(),
            "valor_titulo" to bankSlip.amount.toString(),
            "tipo_carteira" to "109",
            "dados_sacado" to mapOf(
                "tipo_pessoa" to if (bankSlip.payer.documentNumber.length > 11) "J" else "F",
                "cpf_cnpj" to bankSlip.payer.documentNumber.replace("[^0-9]".toRegex(), ""),
                "nome" to bankSlip.payer.name,
                "endereco" to mapOf(
                    "logradouro" to bankSlip.payer.address.street,
                    "numero" to bankSlip.payer.address.number,
                    "bairro" to bankSlip.payer.address.neighborhood,
                    "cidade" to bankSlip.payer.address.city,
                    "uf" to bankSlip.payer.address.state,
                    "cep" to bankSlip.payer.address.zipCode.replace("-", "")
                )
            )
        )
    }

    override suspend fun cancel(bankSlip: BankSlip): Either<DomainError, Unit> {
        return try {
            logger.info("Cancelling BankSlip with Itaú")
            // Implement cancellation logic
            Unit.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Itaú cancellation failed", e).left()
        }
    }

    override suspend fun query(bankSlip: BankSlip): Either<DomainError, BankSlipStatusResponse> {
        return BankSlipStatusResponse("REGISTERED").right()
    }

    override fun supports(bankCode: String): Boolean = bankCode == this.bankCode
}
