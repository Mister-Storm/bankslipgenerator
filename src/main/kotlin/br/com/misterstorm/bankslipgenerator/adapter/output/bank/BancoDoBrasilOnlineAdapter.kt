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
import java.time.format.DateTimeFormatter

/**
 * Banco do Brasil online registration adapter
 * API Documentation: https://developers.bb.com.br/
 */
@Component
class BancoDoBrasilOnlineAdapter(
    private val webClientBuilder: WebClient.Builder,
    private val encryptionService: EncryptionService,
    private val logger: Logger,
    @Value("\${app.banks.bb.apiUrl}") private val apiUrl: String,
    @Value("\${app.banks.bb.authUrl}") private val authUrl: String
) : BankOnlineRegistrationService {

    private val bankCode = "001"
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    @CircuitBreaker(name = "bancoDoBrasil", fallbackMethod = "registerFallback")
    @Retry(name = "bancoDoBrasil")
    override suspend fun register(bankSlip: BankSlip): Either<DomainError, RegistrationResponse> {
        return try {
            logger.info(
                "Registering BankSlip with Banco do Brasil",
                "bankSlipId" to bankSlip.id.toString(),
                "documentNumber" to bankSlip.documentNumber
            )

            // Get OAuth2 token
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
                "BankSlip registered successfully with Banco do Brasil",
                "bankSlipId" to bankSlip.id.toString(),
                "numeroTitulo" to response["numero"].toString()
            )

            RegistrationResponse(
                success = true,
                registrationId = response["numero"].toString(),
                message = "Registered successfully with Banco do Brasil"
            ).right()

        } catch (e: Exception) {
            logger.error(
                "Failed to register BankSlip with Banco do Brasil",
                e,
                "bankSlipId" to bankSlip.id.toString()
            )
            DomainError.UnexpectedError("BB registration failed: ${e.message}", e).left()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun registerFallback(bankSlip: BankSlip, ex: Exception): Either<DomainError, RegistrationResponse> {
        logger.warn(
            "Circuit breaker activated for Banco do Brasil",
            "bankSlipId" to bankSlip.id.toString()
        )
        return DomainError.UnexpectedError(
            "BB service unavailable. Please try CNAB registration or try again later.",
            ex
        ).left()
    }

    private suspend fun getOAuth2Token(): Either<DomainError, String> {
        return try {
            // TODO: Implement real OAuth2 token retrieval from credentials
            // For now, return mock token - this should be replaced with actual implementation
            logger.debug("Retrieving OAuth2 token for Banco do Brasil")
            "mock_bb_token".right()
        } catch (e: Exception) {
            logger.error("Failed to get OAuth2 token for BB", e)
            DomainError.UnexpectedError("Failed to authenticate with BB", e).left()
        }
    }

    private fun buildRegistrationRequest(bankSlip: BankSlip): Map<String, Any> {
        return mapOf(
            "numeroConvenio" to "SEU_CONVENIO", // Should come from credentials
            "numeroCarteira" to "17",
            "numeroVariacaoCarteira" to "019",
            "codigoModalidade" to 1,
            "dataEmissao" to bankSlip.issueDate.format(dateFormatter),
            "dataVencimento" to bankSlip.dueDate.format(dateFormatter),
            "valorOriginal" to bankSlip.amount.toString(),
            "valorAbatimento" to (bankSlip.discount?.value?.toString() ?: "0.00"),
            "quantidadeDiasProtesto" to 0,
            "quantidadeDiasNegativacao" to 0,
            "orgaoNegativador" to 0,
            "indicadorAceiteTituloVencido" to "N",
            "numeroDiasLimiteRecebimento" to 0,
            "codigoAceite" to "N",
            "codigoTipoTitulo" to 2,
            "descricaoTipoTitulo" to "DM",
            "indicadorPermissaoRecebimentoParcial" to "N",
            "numeroTituloBeneficiario" to bankSlip.documentNumber,
            "pagador" to mapOf(
                "tipoInscricao" to if (bankSlip.payer.documentNumber.length > 11) 2 else 1,
                "numeroInscricao" to bankSlip.payer.documentNumber.replace("[^0-9]".toRegex(), ""),
                "nome" to bankSlip.payer.name,
                "endereco" to bankSlip.payer.address.street,
                "cep" to bankSlip.payer.address.zipCode.replace("-", ""),
                "cidade" to bankSlip.payer.address.city,
                "bairro" to bankSlip.payer.address.neighborhood,
                "uf" to bankSlip.payer.address.state
            )
        )
    }

    override suspend fun cancel(bankSlip: BankSlip): Either<DomainError, Unit> {
        return try {
            logger.info(
                "Cancelling BankSlip with Banco do Brasil",
                "bankSlipId" to bankSlip.id.toString()
            )

            val token = getOAuth2Token().fold({ return it.left() }, { it })

            val client = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()

            client.post()
                .uri("/boletos/{id}/baixar", bankSlip.documentNumber)
                .retrieve()
                .awaitBody<Map<String, Any>>()

            logger.info("BankSlip cancelled successfully with BB")
            Unit.right()

        } catch (e: Exception) {
            logger.error("Failed to cancel BankSlip with BB", e)
            DomainError.UnexpectedError("BB cancellation failed: ${e.message}", e).left()
        }
    }

    override suspend fun query(bankSlip: BankSlip): Either<DomainError, BankSlipStatusResponse> {
        return try {
            logger.debug("Querying BankSlip status with BB")

            val token = getOAuth2Token().fold({ return it.left() }, { it })

            val client = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()

            val response = client.get()
                .uri("/boletos/{id}", bankSlip.documentNumber)
                .retrieve()
                .awaitBody<Map<String, Any>>()

            BankSlipStatusResponse(
                status = response["situacao"] as? String ?: "UNKNOWN",
                registrationId = response["numero"] as? String,
                lastUpdate = response["dataAtualizacao"] as? String
            ).right()

        } catch (e: Exception) {
            logger.error("Failed to query BankSlip status with BB", e)
            DomainError.UnexpectedError("BB query failed: ${e.message}", e).left()
        }
    }

    override fun supports(bankCode: String): Boolean = bankCode == this.bankCode
}

