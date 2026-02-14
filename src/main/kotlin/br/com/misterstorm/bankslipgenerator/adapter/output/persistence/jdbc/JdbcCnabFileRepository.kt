package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.CnabFileEntity
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFileStatus
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFileType
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class JdbcCnabFileRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val rowMapper: CnabFileRowMapper
) {
    fun save(entity: CnabFileEntity): CnabFileEntity {
        val now = LocalDateTime.now()

        jdbcTemplate.update(
            CnabFileSql.INSERT,
            entity.id,
            entity.bankCode,
            entity.fileType.name,
            entity.version.name,
            entity.fileName,
            entity.fileContent,
            entity.fileUrl,
            entity.status.name,
            entity.totalRecords,
            entity.processedRecords,
            entity.errorRecords,
            entity.errors,
            now
        )

        return entity.copy(createdAt = now)
    }

    fun update(entity: CnabFileEntity): CnabFileEntity {
        jdbcTemplate.update(
            CnabFileSql.UPDATE,
            entity.status.name,
            entity.processedRecords,
            entity.errorRecords,
            entity.errors,
            entity.processedAt,
            entity.id
        )

        return entity
    }

    fun findById(id: UUID): CnabFileEntity? {
        return try {
            jdbcTemplate.queryForObject(CnabFileSql.FIND_BY_ID, rowMapper, id)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    fun findByBankCode(bankCode: String): List<CnabFileEntity> {
        return jdbcTemplate.query(CnabFileSql.FIND_BY_BANK_CODE, rowMapper, bankCode)
    }

    fun findByFileType(fileType: CnabFileType): List<CnabFileEntity> {
        return jdbcTemplate.query(CnabFileSql.FIND_BY_FILE_TYPE, rowMapper, fileType.name)
    }

    fun findByStatus(status: CnabFileStatus): List<CnabFileEntity> {
        return jdbcTemplate.query(CnabFileSql.FIND_BY_STATUS, rowMapper, status.name)
    }

    fun findAll(): List<CnabFileEntity> {
        return jdbcTemplate.query(CnabFileSql.FIND_ALL, rowMapper)
    }

    fun markAsProcessed(id: UUID) {
        val now = LocalDateTime.now()
        jdbcTemplate.update(CnabFileSql.MARK_PROCESSED, now, id)
    }
}

