package br.com.misterstorm.bankslipgenerator.adapter.output.barcode

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.port.BarcodeGeneratorService
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.oned.Code128Writer
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * ZXing implementation of BarcodeGeneratorService
 */
@Component
class ZXingBarcodeGeneratorAdapter : BarcodeGeneratorService {

    override suspend fun generateBarcode(digitableLine: String): Either<DomainError, ByteArray> {
        return try {
            val writer = Code128Writer()
            val bitMatrix = writer.encode(
                digitableLine.replace(".", "").replace(" ", ""),
                BarcodeFormat.CODE_128,
                400,
                100
            )

            val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
            val outputStream = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "PNG", outputStream)

            outputStream.toByteArray().right()
        } catch (e: Exception) {
            DomainError.ValidationFailed(
                violations = listOf("Failed to generate barcode: ${e.message}")
            ).left()
        }
    }
}

