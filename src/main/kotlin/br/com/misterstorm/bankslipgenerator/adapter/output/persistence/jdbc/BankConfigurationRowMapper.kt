package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.BankConfigurationEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.UUID

@Component
class BankConfigurationRowMapper : RowMapper<BankConfigurationEntity> {
    override fun mapRow(rs: ResultSet, rowNum: Int): BankConfigurationEntity {
        return BankConfigurationEntity(
            id = UUID.fromString(rs.getString("id")),
            bankCode = rs.getString("bank_code"),
            bankName = rs.getString("bank_name"),
            layoutConfiguration = Json.decodeFromString<JsonObject>(rs.getString("layout_configuration")),
            cnabConfiguration = Json.decodeFromString<JsonObject>(rs.getString("cnab_configuration")),
            validationRules = Json.decodeFromString<JsonObject>(rs.getString("validation_rules")),
            isActive = rs.getBoolean("is_active"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
        )
    }
}

