package br.com.misterstorm.bankslipgenerator.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

/**
 * Bank configuration entity
 */
data class BankConfiguration(
    val id: UUID,
    val bankCode: String,
    val bankName: String,
    val layoutConfiguration: LayoutConfiguration,
    val cnabConfiguration: CnabConfiguration,
    val validationRules: List<ValidationRule>,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Layout configuration for PDF generation
 */
@Serializable
data class LayoutConfiguration(
    val templateId: String,
    val logoUrl: String? = null,
    val customCss: String? = null,
    val fields: Map<String, FieldConfiguration> = emptyMap()
)

@Serializable
data class FieldConfiguration(
    val label: String,
    val visible: Boolean = true,
    val format: String? = null,
    val position: FieldPosition? = null
)

@Serializable
data class FieldPosition(
    val x: Int,
    val y: Int,
    val width: Int? = null,
    val height: Int? = null
)

/**
 * CNAB Configuration
 */
@Serializable
data class CnabConfiguration(
    val version: CnabVersion,
    val headerLayout: String, // JSON DSL for header
    val detailLayout: String, // JSON DSL for detail records
    val trailerLayout: String, // JSON DSL for trailer
    val segmentConfigurations: Map<String, String> = emptyMap() // Segment-specific layouts
)

@Serializable
enum class CnabVersion {
    CNAB240,
    CNAB400
}

/**
 * Validation rule configuration
 */
@Serializable
data class ValidationRule(
    val name: String,
    val description: String,
    val priority: Int = 1,
    val condition: String, // Expression for Easy Rules
    val actions: List<String> = emptyList()
)

