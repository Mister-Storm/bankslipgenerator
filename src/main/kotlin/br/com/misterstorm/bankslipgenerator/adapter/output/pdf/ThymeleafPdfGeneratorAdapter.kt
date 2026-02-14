package br.com.misterstorm.bankslipgenerator.adapter.output.pdf

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.port.BarcodeGeneratorService
import br.com.misterstorm.bankslipgenerator.domain.port.PdfGeneratorService
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.ByteArrayOutputStream
import java.util.Base64

/**
 * Thymeleaf + Flying Saucer implementation of PdfGeneratorService
 */
@Component
class ThymeleafPdfGeneratorAdapter(
    private val templateEngine: TemplateEngine,
    private val barcodeGeneratorService: BarcodeGeneratorService
) : PdfGeneratorService {

    override suspend fun generatePdf(bankSlip: BankSlip): Either<DomainError, ByteArray> {
        return try {
            // Generate barcode image
            val barcodeBytes = barcodeGeneratorService.generateBarcode(bankSlip.digitableLine)
                .fold({ return it.left() }, { it })

            val barcodeBase64 = Base64.getEncoder().encodeToString(barcodeBytes)

            // Prepare template context
            val context = Context().apply {
                setVariable("bankSlip", bankSlip)
                setVariable("barcodeImage", "data:image/png;base64,$barcodeBase64")
            }

            // Process template
            val html = templateEngine.process("bankslip/default", context)

            // Generate PDF
            val outputStream = ByteArrayOutputStream()
            val renderer = ITextRenderer()
            renderer.setDocumentFromString(html)
            renderer.layout()
            renderer.createPDF(outputStream)

            outputStream.toByteArray().right()
        } catch (e: Exception) {
            DomainError.PdfGenerationFailed(
                message = "Failed to generate PDF: ${e.message}",
                details = mapOf("bankSlipId" to bankSlip.id.toString())
            ).left()
        }
    }
}
