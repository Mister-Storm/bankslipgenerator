package br.com.misterstorm.bankslipgenerator.adapter.output.bank

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip
import br.com.misterstorm.bankslipgenerator.domain.port.*
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

/**
 * Santander online registration adapter
 * Uses API Key authentication
 */
@Component
class SantanderOnlineAdapter(
    private val webClientBuilder: WebClient.Builder,
    private val logger: Logger,
    @Value("\${app.banks.santander.apiUrl}") private val apiUrl: String,
    @Value("\${app.banks.santander.apiKey:#{null}}") private val apiKey: String?
) : BankOnlineRegistrationService {

    private val bankCode = "033"

    override suspend fun register(bankslip: Bankslip): Either<DomainError, RegistrationResponse> {
        return try {
            if (apiKey == null) {
                return DomainError.UnexpectedError("Santander API key not configured").left()
            }

            logger.info("Registering bankslip with Santander", "bankslipId" to bankslip.id.toString())

            val client = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader("X-API-Key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build()

            val request = mapOf(
                "nsu" to bankslip.documentNumber,
                "valor" to bankslip.amount.toString(),
                "data_vencimento" to bankslip.dueDate.toString(),
                "pagador" to mapOf(
                    "nome" to bankslip.payer.name,
                    "documento" to mapOf(
                        "tipo" to if (bankslip.payer.documentNumber.length > 11) "CNPJ" else "CPF",
                        "numero" to bankslip.payer.documentNumber
                    ),
                    "endereco" to mapOf(
                        "logradouro" to bankslip.payer.address.street,
                        "numero" to bankslip.payer.address.number,
                        "bairro" to bankslip.payer.address.neighborhood,
                        "cidade" to bankslip.payer.address.city,
                        "uf" to bankslip.payer.address.state,
                        "cep" to bankslip.payer.address.zipCode
                    )
                ),
                "beneficiario" to mapOf(
                    "documento" to bankslip.beneficiary.documentNumber,
                    "agencia" to bankslip.beneficiary.agencyNumber,
                    "conta" to bankslip.beneficiary.accountNumber
                )
            )

            val response = client.post()
                .uri("/boletos")
                .bodyValue(request)
                .retrieve()
                .awaitBody<Map<String, Any>>()

            logger.info("Santander registration successful")

            RegistrationResponse(
                success = true,
                registrationId = response["nosso_numero"] as? String,
                message = "Registered with Santander"
            ).right()
        } catch (e: Exception) {
            logger.error("Santander registration failed", e)
            DomainError.UnexpectedError("Santander registration failed: ${e.message}", e).left()
        }
    }

    override suspend fun cancel(bankslip: Bankslip): Either<DomainError, Unit> = Unit.right()

    override suspend fun query(bankslip: Bankslip): Either<DomainError, BankslipStatusResponse> =
        BankslipStatusResponse("REGISTERED").right()

    override fun supports(bankCode: String): Boolean = bankCode == this.bankCode
}

