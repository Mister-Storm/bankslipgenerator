package br.com.misterstorm.bankslipgenerator.adapter.output.cnab

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankConfiguration
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFile
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFileStatus
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFileType
import br.com.misterstorm.bankslipgenerator.domain.model.CnabVersion
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.domain.port.CnabService
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

/**
 * CNAB service implementation with DSL-based layout configuration
 */
@Component
class CnabServiceAdapter(
    private val bankConfigurationRepository: BankConfigurationRepository
) : CnabService {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateRemittanceFile(
        bankSlips: List<BankSlip>,
        bankCode: String,
        version: CnabVersion
    ): Either<DomainError, CnabFile> {
        return try {
            val bankConfig = bankConfigurationRepository.findByBankCode(bankCode)
                .fold({ return it.left() }, { it })

            val content = when (version) {
                CnabVersion.CNAB240 -> generateCnab240Remittance(bankSlips, bankConfig)
                CnabVersion.CNAB400 -> generateCnab400Remittance(bankSlips, bankConfig)
            }

            val fileName = "REM_${bankCode}_${System.currentTimeMillis()}.txt"

            CnabFile(
                id = UUID.randomUUID(),
                bankCode = bankCode,
                fileType = CnabFileType.REMITTANCE,
                version = version,
                fileName = fileName,
                fileContent = content,
                status = CnabFileStatus.PENDING,
                totalRecords = bankSlips.size + 2, // header + details + trailer
                createdAt = LocalDateTime.now()
            ).right()
        } catch (e: Exception) {
            DomainError.CnabGenerationFailed(
                message = "Failed to generate CNAB remittance: ${e.message}",
                details = mapOf("bankCode" to bankCode, "version" to version.name)
            ).left()
        }
    }

    override suspend fun processReturnFile(
        fileContent: String,
        bankCode: String,
        version: CnabVersion
    ): Either<DomainError, CnabFile> {
        return try {
            val bankConfig = bankConfigurationRepository.findByBankCode(bankCode)
                .fold({ return it.left() }, { it })

            val lines = fileContent.lines()
            val processedRecords = when (version) {
                CnabVersion.CNAB240 -> processCnab240Return(lines, bankConfig)
                CnabVersion.CNAB400 -> processCnab400Return(lines, bankConfig)
            }

            val fileName = "RET_${bankCode}_${System.currentTimeMillis()}.txt"

            CnabFile(
                id = UUID.randomUUID(),
                bankCode = bankCode,
                fileType = CnabFileType.RETURN,
                version = version,
                fileName = fileName,
                fileContent = fileContent,
                status = CnabFileStatus.PROCESSED,
                totalRecords = lines.size,
                processedRecords = processedRecords,
                createdAt = LocalDateTime.now(),
                processedAt = LocalDateTime.now()
            ).right()
        } catch (e: Exception) {
            DomainError.CnabProcessingFailed(
                message = "Failed to process CNAB return: ${e.message}",
                details = mapOf("bankCode" to bankCode, "version" to version.name)
            ).left()
        }
    }

    private fun generateCnab240Remittance(bankSlips: List<BankSlip>, config: BankConfiguration): String {
        val lines = mutableListOf<String>()

        // Header do arquivo (registro tipo 0)
        lines.add(generateCnab240FileHeader(config))

        // Header do lote (registro tipo 1)
        lines.add(generateCnab240LotHeader(config))

        // Detalhes (registros tipo 3 - segmentos P, Q, R, etc.)
        bankSlips.forEachIndexed { index, bankSlip ->
            lines.add(generateCnab240SegmentP(bankSlip, config, index + 1))
            lines.add(generateCnab240SegmentQ(bankSlip, config, index + 1))
        }

        // Trailer do lote (registro tipo 5)
        lines.add(generateCnab240LotTrailer(bankSlips.size * 2))

        // Trailer do arquivo (registro tipo 9)
        lines.add(generateCnab240FileTrailer())

        return lines.joinToString("\n")
    }

    private fun generateCnab400Remittance(bankSlips: List<BankSlip>, config: BankConfiguration): String {
        val lines = mutableListOf<String>()

        // Header (registro tipo 0)
        lines.add(generateCnab400Header(config))

        // Detalhes (registro tipo 1)
        bankSlips.forEach { bankSlip ->
            lines.add(generateCnab400Detail(bankSlip, config))
        }

        // Trailer (registro tipo 9)
        lines.add(generateCnab400Trailer(bankSlips.size))

        return lines.joinToString("\n")
    }

    @Suppress("UNUSED_PARAMETER")
    private fun processCnab240Return(lines: List<String>, config: BankConfiguration): Int {
        var processedCount = 0
        lines.forEach { line ->
            if (line.startsWith("3")) { // Detail record
                processedCount++
            }
        }
        return processedCount
    }

    @Suppress("UNUSED_PARAMETER")
    private fun processCnab400Return(lines: List<String>, config: BankConfiguration): Int {
        var processedCount = 0
        lines.forEach { line ->
            if (line.startsWith("1")) { // Detail record
                processedCount++
            }
        }
        return processedCount
    }

    // CNAB 240 generators (simplified - real implementation needs exact specifications)
    private fun generateCnab240FileHeader(config: BankConfiguration): String {
        return buildString {
            append(config.bankCode.padStart(3, '0')) // Código do banco
            append("0000") // Lote de serviço
            append("0") // Tipo de registro
            append(" ".repeat(9)) // Uso FEBRABAN
            append("2") // Tipo de inscrição da empresa
            append(config.bankName.take(14).padEnd(14, ' '))
            append(" ".repeat(186)) // Campos complementares
        }.padEnd(240, ' ')
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateCnab240LotHeader(config: BankConfiguration): String {
        return "1".padEnd(240, ' ')
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateCnab240SegmentP(bankSlip: BankSlip, config: BankConfiguration, sequenceNumber: Int): String {
        return buildString {
            append(config.bankCode.padStart(3, '0'))
            append("0001") // Lote
            append("3") // Tipo de registro
            append(sequenceNumber.toString().padStart(5, '0'))
            append("P") // Código do segmento
            append(" ") // Uso exclusivo FEBRABAN
            append("01") // Código de movimento
            append(bankSlip.beneficiary.agencyNumber.padStart(5, '0'))
            append(" ")
            append(bankSlip.beneficiary.accountNumber.padStart(12, '0'))
            append(bankSlip.beneficiary.accountDigit)
            append(" ")
            append(bankSlip.documentNumber.padStart(20, '0'))
            append(" ".repeat(175)) // Campos complementares
        }.padEnd(240, ' ')
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateCnab240SegmentQ(bankSlip: BankSlip, config: BankConfiguration, sequenceNumber: Int): String {
        return buildString {
            append(config.bankCode.padStart(3, '0'))
            append("0001") // Lote
            append("3") // Tipo de registro
            append(sequenceNumber.toString().padStart(5, '0'))
            append("Q") // Código do segmento
            append(" ") // Uso exclusivo FEBRABAN
            append("01") // Código de movimento
            append(if (bankSlip.payer.documentNumber.length > 11) "2" else "1") // Tipo de inscrição
            append(bankSlip.payer.documentNumber.padStart(15, '0'))
            append(bankSlip.payer.name.take(40).padEnd(40, ' '))
            append(" ".repeat(165)) // Campos complementares
        }.padEnd(240, ' ')
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateCnab240LotTrailer(recordCount: Int): String {
        return "5".padEnd(240, ' ')
    }

    private fun generateCnab240FileTrailer(): String {
        return "9".padEnd(240, ' ')
    }

    // CNAB 400 generators (simplified)
    @Suppress("UNUSED_PARAMETER")
    private fun generateCnab400Header(config: BankConfiguration): String {
        return "0".padEnd(400, ' ')
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateCnab400Detail(bankSlip: BankSlip, config: BankConfiguration): String {
        return buildString {
            append("1") // Tipo de registro
            append(if (bankSlip.payer.documentNumber.length > 11) "02" else "01")
            append(bankSlip.payer.documentNumber.padStart(14, '0'))
            append(bankSlip.documentNumber.padStart(25, '0'))
            append(bankSlip.amount.toString().replace(".", "").padStart(13, '0'))
            append(" ".repeat(343)) // Campos complementares
        }.padEnd(400, ' ')
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateCnab400Trailer(recordCount: Int): String {
        return "9".padEnd(400, ' ')
    }
}
