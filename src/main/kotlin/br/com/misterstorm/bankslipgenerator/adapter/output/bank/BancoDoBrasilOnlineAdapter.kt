package br.com.misterstorm.bankslipgenerator.adapter.output.bank

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip
import br.com.misterstorm.bankslipgenerator.domain.port.*
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
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
    override suspend fun register(bankslip: Bankslip): Either<DomainError, RegistrationResponse> {
        return try {
            logger.info(
                "Registering bankslip with Banco do Brasil",
                "bankslipId" to bankslip.id.toString(),
                "documentNumber" to bankslip.documentNumber
            )

            // Get OAuth2 token
            val token = getOAuth2Token().fold({ return it.left() }, { it })

            val client = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()

            val request = buildRegistrationRequest(bankslip)

            val response = client.post()
                .uri("/boletos")
                .bodyValue(request)
                .retrieve()
                .awaitBody<Map<String, Any>>()

            logger.info(
                "Bankslip registered successfully with Banco do Brasil",
                "bankslipId" to bankslip.id.toString(),
                "numeroTitulo" to response["numero"].toString()
            )

            RegistrationResponse(
                success = true,
                registrationId = response["numero"].toString(),
                message = "Registered successfully with Banco do Brasil"
            ).right()

        } catch (e: Exception) {
            logger.error(
                "Failed to register bankslip with Banco do Brasil",
                e,
                "bankslipId" to bankslip.id.toString()
            )
            DomainError.UnexpectedError("BB registration failed: ${e.message}", e).left()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun registerFallback(bankslip: Bankslip, ex: Exception): Either<DomainError, RegistrationResponse> {
        logger.warn(
            "Circuit breaker activated for Banco do Brasil",
            "bankslipId" to bankslip.id.toString()
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

    private fun buildRegistrationRequest(bankslip: Bankslip): Map<String, Any> {
        return mapOf(
            "numeroConvenio" to "SEU_CONVENIO", // Should come from credentials
            "numeroCarteira" to "17",
            "numeroVariacaoCarteira" to "019",
            "codigoModalidade" to 1,
            "dataEmissao" to bankslip.issueDate.format(dateFormatter),
            "dataVencimento" to bankslip.dueDate.format(dateFormatter),
            "valorOriginal" to bankslip.amount.toString(),
            "valorAbatimento" to (bankslip.discount?.value?.toString() ?: "0.00"),
            "quantidadeDiasProtesto" to 0,
            "quantidadeDiasNegativacao" to 0,
            "orgaoNegativador" to 0,
            "indicadorAceiteTituloVencido" to "N",
            "numeroDiasLimiteRecebimento" to 0,
            "codigoAceite" to "N",
            "codigoTipoTitulo" to 2,
            "descricaoTipoTitulo" to "DM",
            "indicadorPermissaoRecebimentoParcial" to "N",
            "numeroTituloBeneficiario" to bankslip.documentNumber,
            "pagador" to mapOf(
                "tipoInscricao" to if (bankslip.payer.documentNumber.length > 11) 2 else 1,
                "numeroInscricao" to bankslip.payer.documentNumber.replace("[^0-9]".toRegex(), ""),
                "nome" to bankslip.payer.name,
                "endereco" to bankslip.payer.address.street,
                "cep" to bankslip.payer.address.zipCode.replace("-", ""),
                "cidade" to bankslip.payer.address.city,
                "bairro" to bankslip.payer.address.neighborhood,
                "uf" to bankslip.payer.address.state
            )
        )
    }

    override suspend fun cancel(bankslip: Bankslip): Either<DomainError, Unit> {
        return try {
            logger.info(
                "Cancelling bankslip with Banco do Brasil",
                "bankslipId" to bankslip.id.toString()
            )

            val token = getOAuth2Token().fold({ return it.left() }, { it })

            val client = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()

            client.post()
                .uri("/boletos/{id}/baixar", bankslip.documentNumber)
                .retrieve()
                .awaitBody<Map<String, Any>>()

            logger.info("Bankslip cancelled successfully with BB")
            Unit.right()

        } catch (e: Exception) {
            logger.error("Failed to cancel bankslip with BB", e)
            DomainError.UnexpectedError("BB cancellation failed: ${e.message}", e).left()
        }
    }

    override suspend fun query(bankslip: Bankslip): Either<DomainError, BankslipStatusResponse> {
        return try {
            logger.debug("Querying bankslip status with BB")

            val token = getOAuth2Token().fold({ return it.left() }, { it })

            val client = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()

            val response = client.get()
                .uri("/boletos/{id}", bankslip.documentNumber)
                .retrieve()
                .awaitBody<Map<String, Any>>()

            BankslipStatusResponse(
                status = response["situacao"] as? String ?: "UNKNOWN",
                registrationId = response["numero"] as? String,
                lastUpdate = response["dataAtualizacao"] as? String
            ).right()

        } catch (e: Exception) {
            logger.error("Failed to query bankslip status with BB", e)
            DomainError.UnexpectedError("BB query failed: ${e.message}", e).left()
        }
    }

    override fun supports(bankCode: String): Boolean = bankCode == this.bankCode
}

