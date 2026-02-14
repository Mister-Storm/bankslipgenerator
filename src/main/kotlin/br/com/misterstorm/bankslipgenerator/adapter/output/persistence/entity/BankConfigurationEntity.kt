package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity

import br.com.misterstorm.bankslipgenerator.domain.model.BankConfiguration
import br.com.misterstorm.bankslipgenerator.domain.model.LayoutConfiguration
import br.com.misterstorm.bankslipgenerator.domain.model.CnabConfiguration
import br.com.misterstorm.bankslipgenerator.domain.model.ValidationRule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.builtins.ListSerializer
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity for bank configuration persistence (JDBC - no JPA annotations)
 */
data class BankConfigurationEntity(
    val id: UUID = UUID.randomUUID(),
    val bankCode: String,
    val bankName: String,
    val layoutConfiguration: JsonObject,
    val cnabConfiguration: JsonObject,
    val validationRules: JsonObject,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null
)

// Extension functions for mapping between domain and entity
fun BankConfigurationEntity.toDomain(): BankConfiguration {
    val json = Json { ignoreUnknownKeys = true }

    return BankConfiguration(
        id = this.id,
        bankCode = this.bankCode,
        bankName = this.bankName,
        layoutConfiguration = json.decodeFromJsonElement(LayoutConfiguration.serializer(), this.layoutConfiguration),
        cnabConfiguration = json.decodeFromJsonElement(CnabConfiguration.serializer(), this.cnabConfiguration),
        validationRules = json.decodeFromJsonElement(ListSerializer(ValidationRule.serializer()), this.validationRules),
        isActive = this.isActive,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun BankConfiguration.toEntity(): BankConfigurationEntity {
    val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    return BankConfigurationEntity(
        id = this.id,
        bankCode = this.bankCode,
        bankName = this.bankName,
        layoutConfiguration = json.encodeToJsonElement(LayoutConfiguration.serializer(), this.layoutConfiguration) as JsonObject,
        cnabConfiguration = json.encodeToJsonElement(CnabConfiguration.serializer(), this.cnabConfiguration) as JsonObject,
        validationRules = json.encodeToJsonElement(ListSerializer(ValidationRule.serializer()), this.validationRules) as JsonObject,
        isActive = this.isActive,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
