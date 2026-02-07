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
 * Bradesco online registration adapter (SOAP Web Service)
 * Uses SOAP envelope for communication
 */
@Component
class BradescoOnlineAdapter(
    private val webClientBuilder: WebClient.Builder,
    private val logger: Logger,
    @Value("\${app.banks.bradesco.webserviceUrl}") private val webserviceUrl: String
) : BankOnlineRegistrationService {

    private val bankCode = "237"
    
    override suspend fun register(bankslip: Bankslip): Either<DomainError, RegistrationResponse> {
        return try {
            logger.info("Registering bankslip with Bradesco SOAP", "bankslipId" to bankslip.id.toString())

            val soapEnvelope = buildSoapEnvelope(bankslip)

            val client = webClientBuilder.baseUrl(webserviceUrl).build()

            val response = client.post()
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "IncluirBoleto")
                .bodyValue(soapEnvelope)
                .retrieve()
                .awaitBody<String>()

            // Parse SOAP response
            if (response.contains("<Sucesso>true</Sucesso>")) {
                logger.info("Bradesco registration successful")
                RegistrationResponse(
                    success = true,
                    registrationId = extractValue(response, "NumeroTitulo"),
                    message = "Registered with Bradesco"
                ).right()
            } else {
                val errorMsg = extractValue(response, "Mensagem") ?: "Unknown error"
                DomainError.UnexpectedError("Bradesco registration failed: $errorMsg").left()
            }
        } catch (e: Exception) {
            logger.error("Bradesco SOAP call failed", e)
            DomainError.UnexpectedError("Bradesco SOAP failed: ${e.message}", e).left()
        }
    }

    private fun buildSoapEnvelope(bankslip: Bankslip): String {
        return """
            <?xml version="1.0" encoding="utf-8"?>
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:bol="http://bol.bradesconetempresa.b.br">
                <soapenv:Header/>
                <soapenv:Body>
                    <bol:IncluirBoleto>
                        <bol:NumeroTitulo>${bankslip.documentNumber}</bol:NumeroTitulo>
                        <bol:Valor>${bankslip.amount}</bol:Valor>
                        <bol:DataVencimento>${bankslip.dueDate}</bol:DataVencimento>
                        <bol:NomePagador>${bankslip.payer.name}</bol:NomePagador>
                        <bol:CpfCnpjPagador>${bankslip.payer.documentNumber}</bol:CpfCnpjPagador>
                    </bol:IncluirBoleto>
                </soapenv:Body>
            </soapenv:Envelope>
        """.trimIndent()
    }

    private fun extractValue(xml: String, tagName: String): String? {
        val regex = "<$tagName>(.*?)</$tagName>".toRegex()
        return regex.find(xml)?.groupValues?.get(1)
    }

    override suspend fun cancel(bankslip: Bankslip): Either<DomainError, Unit> = Unit.right()
    
    override suspend fun query(bankslip: Bankslip): Either<DomainError, BankslipStatusResponse> =
        BankslipStatusResponse("REGISTERED").right()
    
    override fun supports(bankCode: String): Boolean = bankCode == this.bankCode
}

