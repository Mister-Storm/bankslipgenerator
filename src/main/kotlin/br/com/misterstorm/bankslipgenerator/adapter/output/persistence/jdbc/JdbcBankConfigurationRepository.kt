package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.BankConfigurationEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class JdbcBankConfigurationRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val rowMapper: BankConfigurationRowMapper
) {
    fun save(entity: BankConfigurationEntity): BankConfigurationEntity {
        val now = LocalDateTime.now()
        val layoutConfigJson = Json.encodeToString(JsonObject.serializer(), entity.layoutConfiguration)
        val cnabConfigJson = Json.encodeToString(JsonObject.serializer(), entity.cnabConfiguration)
        val validationRulesJson = Json.encodeToString(JsonObject.serializer(), entity.validationRules)

        jdbcTemplate.update(
            BankConfigurationSql.INSERT,
            entity.id,
            entity.bankCode,
            entity.bankName,
            layoutConfigJson,
            cnabConfigJson,
            validationRulesJson,
            entity.isActive,
            now,
            now
        )

        return entity.copy(createdAt = now, updatedAt = now)
    }

    fun update(entity: BankConfigurationEntity): BankConfigurationEntity {
        val now = LocalDateTime.now()
        val layoutConfigJson = Json.encodeToString(JsonObject.serializer(), entity.layoutConfiguration)
        val cnabConfigJson = Json.encodeToString(JsonObject.serializer(), entity.cnabConfiguration)
        val validationRulesJson = Json.encodeToString(JsonObject.serializer(), entity.validationRules)

        jdbcTemplate.update(
            BankConfigurationSql.UPDATE,
            entity.bankName,
            layoutConfigJson,
            cnabConfigJson,
            validationRulesJson,
            entity.isActive,
            now,
            entity.id
        )

        return entity.copy(updatedAt = now)
    }

    fun findById(id: UUID): BankConfigurationEntity? {
        return try {
            jdbcTemplate.queryForObject(BankConfigurationSql.FIND_BY_ID, rowMapper, id)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    fun findByBankCode(bankCode: String): BankConfigurationEntity? {
        return try {
            jdbcTemplate.queryForObject(BankConfigurationSql.FIND_BY_BANK_CODE, rowMapper, bankCode)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    fun findAll(): List<BankConfigurationEntity> {
        return jdbcTemplate.query(BankConfigurationSql.FIND_ALL, rowMapper)
    }

    fun findAllActive(): List<BankConfigurationEntity> {
        return jdbcTemplate.query(BankConfigurationSql.FIND_ALL_ACTIVE, rowMapper)
    }

    fun softDelete(id: UUID) {
        jdbcTemplate.update(BankConfigurationSql.SOFT_DELETE, LocalDateTime.now(), id)
    }

    fun existsById(id: UUID): Boolean {
        return jdbcTemplate.queryForObject(BankConfigurationSql.EXISTS_BY_ID, Boolean::class.java, id) ?: false
    }

    fun existsByBankCode(bankCode: String): Boolean {
        return jdbcTemplate.queryForObject(BankConfigurationSql.EXISTS_BY_BANK_CODE, Boolean::class.java, bankCode) ?: false
    }
}

