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

    override suspend fun register(bankSlip: BankSlip): Either<DomainError, RegistrationResponse> {
        return try {
            if (apiKey == null) {
                return DomainError.UnexpectedError("Santander API key not configured").left()
            }

            logger.info("Registering BankSlip with Santander", "bankSlipId" to bankSlip.id.toString())

            val client = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader("X-API-Key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build()

            val request = mapOf(
                "nsu" to bankSlip.documentNumber,
                "valor" to bankSlip.amount.toString(),
                "data_vencimento" to bankSlip.dueDate.toString(),
                "pagador" to mapOf(
                    "nome" to bankSlip.payer.name,
                    "documento" to mapOf(
                        "tipo" to if (bankSlip.payer.documentNumber.length > 11) "CNPJ" else "CPF",
                        "numero" to bankSlip.payer.documentNumber
                    ),
                    "endereco" to mapOf(
                        "logradouro" to bankSlip.payer.address.street,
                        "numero" to bankSlip.payer.address.number,
                        "bairro" to bankSlip.payer.address.neighborhood,
                        "cidade" to bankSlip.payer.address.city,
                        "uf" to bankSlip.payer.address.state,
                        "cep" to bankSlip.payer.address.zipCode
                    )
                ),
                "beneficiario" to mapOf(
                    "documento" to bankSlip.beneficiary.documentNumber,
                    "agencia" to bankSlip.beneficiary.agencyNumber,
                    "conta" to bankSlip.beneficiary.accountNumber
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

    override suspend fun cancel(bankSlip: BankSlip): Either<DomainError, Unit> = Unit.right()

    override suspend fun query(bankSlip: BankSlip): Either<DomainError, BankSlipStatusResponse> =
        BankSlipStatusResponse("REGISTERED").right()

    override fun supports(bankCode: String): Boolean = bankCode == this.bankCode
}
