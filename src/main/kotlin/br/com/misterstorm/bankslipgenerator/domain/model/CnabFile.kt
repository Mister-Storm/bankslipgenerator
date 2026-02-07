package br.com.misterstorm.bankslipgenerator.domain.model

import java.time.LocalDateTime
import java.util.*

/**
 * CNAB file types
 */
enum class CnabFileType {
    REMITTANCE, // Arquivo de remessa (envio ao banco)
    RETURN      // Arquivo de retorno (recebimento do banco)
}

/**
 * CNAB file status
 */
enum class CnabFileStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED
}

/**
 * CNAB file entity
 */
data class CnabFile(
    val id: UUID,
    val bankCode: String,
    val fileType: CnabFileType,
    val version: CnabVersion,
    val fileName: String,
    val fileContent: String,
    val fileUrl: String? = null,
    val status: CnabFileStatus,
    val totalRecords: Int,
    val processedRecords: Int = 0,
    val errorRecords: Int = 0,
    val errors: List<CnabProcessingError> = emptyList(),
    val createdAt: LocalDateTime,
    val processedAt: LocalDateTime? = null
)

/**
 * CNAB processing error
 */
data class CnabProcessingError(
    val recordNumber: Int,
    val errorCode: String,
    val errorMessage: String,
    val details: Map<String, Any> = emptyMap()
)

/**
 * CNAB record types for CNAB 240
 */
enum class Cnab240RecordType(val code: String) {
    HEADER_FILE("0"),
    HEADER_LOT("1"),
    DETAIL_SEGMENT_P("3P"),
    DETAIL_SEGMENT_Q("3Q"),
    DETAIL_SEGMENT_R("3R"),
    DETAIL_SEGMENT_S("3S"),
    TRAILER_LOT("5"),
    TRAILER_FILE("9")
}

/**
 * CNAB record types for CNAB 400
 */
enum class Cnab400RecordType(val code: String) {
    HEADER("0"),
    DETAIL("1"),
    TRAILER("9")
}

