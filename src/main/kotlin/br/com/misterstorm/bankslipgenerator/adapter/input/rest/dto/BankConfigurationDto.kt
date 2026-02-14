package br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto

import br.com.misterstorm.bankslipgenerator.domain.model.CnabConfiguration
import br.com.misterstorm.bankslipgenerator.domain.model.CnabVersion
import br.com.misterstorm.bankslipgenerator.domain.model.FieldConfiguration
import br.com.misterstorm.bankslipgenerator.domain.model.FieldPosition
import br.com.misterstorm.bankslipgenerator.domain.model.LayoutConfiguration
import br.com.misterstorm.bankslipgenerator.domain.model.ValidationRule

/**
 * Request DTO for creating a bank configuration
 */
data class CreateBankConfigurationRequest(
    val bankCode: String,
    val bankName: String,
    val layoutConfiguration: LayoutConfigurationDto,
    val cnabConfiguration: CnabConfigurationDto,
    val validationRules: List<ValidationRuleDto> = emptyList()
)

data class LayoutConfigurationDto(
    val templateId: String,
    val logoUrl: String? = null,
    val customCss: String? = null,
    val fields: Map<String, FieldConfigurationDto> = emptyMap()
)

data class FieldConfigurationDto(
    val label: String,
    val visible: Boolean = true,
    val format: String? = null,
    val position: FieldPositionDto? = null
)

data class FieldPositionDto(
    val x: Int,
    val y: Int,
    val width: Int? = null,
    val height: Int? = null
)

data class CnabConfigurationDto(
    val version: CnabVersion,
    val headerLayout: String,
    val detailLayout: String,
    val trailerLayout: String,
    val segmentConfigurations: Map<String, String> = emptyMap()
)

data class ValidationRuleDto(
    val name: String,
    val description: String,
    val priority: Int = 1,
    val condition: String,
    val actions: List<String> = emptyList()
)

// Extension functions for mapping
fun LayoutConfigurationDto.toDomain(): LayoutConfiguration = LayoutConfiguration(
    templateId = templateId,
    logoUrl = logoUrl,
    customCss = customCss,
    fields = fields.mapValues { it.value.toDomain() }
)

fun FieldConfigurationDto.toDomain(): FieldConfiguration = FieldConfiguration(
    label = label,
    visible = visible,
    format = format,
    position = position?.toDomain()
)

fun FieldPositionDto.toDomain(): FieldPosition = FieldPosition(
    x = x,
    y = y,
    width = width,
    height = height
)

fun CnabConfigurationDto.toDomain(): CnabConfiguration = CnabConfiguration(
    version = version,
    headerLayout = headerLayout,
    detailLayout = detailLayout,
    trailerLayout = trailerLayout,
    segmentConfigurations = segmentConfigurations
)

fun ValidationRuleDto.toDomain(): ValidationRule = ValidationRule(
    name = name,
    description = description,
    priority = priority,
    condition = condition,
    actions = actions
)

