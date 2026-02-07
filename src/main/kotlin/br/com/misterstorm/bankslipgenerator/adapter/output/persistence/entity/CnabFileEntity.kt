package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity

import br.com.misterstorm.bankslipgenerator.domain.model.CnabFile
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFileStatus
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFileType
import br.com.misterstorm.bankslipgenerator.domain.model.CnabProcessingError
import br.com.misterstorm.bankslipgenerator.domain.model.CnabVersion
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.util.*

/**
 * Entity for CNAB file persistence (JDBC - no JPA annotations)
 */
data class CnabFileEntity(
    val id: UUID = UUID.randomUUID(),
    val bankCode: String,
    val fileType: CnabFileType,
    val version: CnabVersion,
    val fileName: String,
    val fileContent: String,
    val fileUrl: String?,
    val status: CnabFileStatus,
    val totalRecords: Int,
    val processedRecords: Int = 0,
    val errorRecords: Int = 0,
    val errors: String?, // JSON array of errors
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val processedAt: LocalDateTime? = null
)

// Extension functions for mapping between domain and entity
fun CnabFileEntity.toDomain(): CnabFile {
    val errorList = if (this.errors != null) {
        Json.decodeFromString<List<CnabProcessingError>>(this.errors)
    } else {
        emptyList()
    }

    return CnabFile(
        id = this.id,
        bankCode = this.bankCode,
        fileType = this.fileType,
        version = this.version,
        fileName = this.fileName,
        fileContent = this.fileContent,
        fileUrl = this.fileUrl,
        status = this.status,
        totalRecords = this.totalRecords,
        processedRecords = this.processedRecords,
        errorRecords = this.errorRecords,
        errors = errorList,
        createdAt = this.createdAt,
        processedAt = this.processedAt
    )
}

fun CnabFile.toEntity(): CnabFileEntity {
    val errorsJson = if (this.errors.isNotEmpty()) {
        Json.encodeToString(this.errors)
    } else {
        null
    }

    return CnabFileEntity(
        id = this.id,
        bankCode = this.bankCode,
        fileType = this.fileType,
        version = this.version,
        fileName = this.fileName,
        fileContent = this.fileContent,
        fileUrl = this.fileUrl,
        status = this.status,
        totalRecords = this.totalRecords,
        processedRecords = this.processedRecords,
        errorRecords = this.errorRecords,
        errors = errorsJson,
        createdAt = this.createdAt,
        processedAt = this.processedAt
    )
}

