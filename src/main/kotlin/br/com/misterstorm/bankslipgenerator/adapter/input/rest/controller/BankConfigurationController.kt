package br.com.misterstorm.bankslipgenerator.adapter.input.rest.controller

import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.CreateBankConfigurationRequest
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.toDomain
import br.com.misterstorm.bankslipgenerator.application.usecase.bankconfiguration.CreateBankConfigurationUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for bank configuration operations
 */
@RestController
@RequestMapping("/api/bank-configurations")
class BankConfigurationController(
    private val createBankConfigurationUseCase: CreateBankConfigurationUseCase
) {

    @PostMapping
    @Suppress("UNUSED_PARAMETER")
    suspend fun createBankConfiguration(
        @RequestBody request: CreateBankConfigurationRequest,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<Map<String, Any>> {
        val input = CreateBankConfigurationUseCase.Input(
            bankCode = request.bankCode,
            bankName = request.bankName,
            layoutConfiguration = request.layoutConfiguration.toDomain(),
            cnabConfiguration = request.cnabConfiguration.toDomain(),
            validationRules = request.validationRules.map { it.toDomain() }
        )

        return createBankConfigurationUseCase(input).fold(
            { error -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("error" to error.message, "details" to error.details)
            ) },
            { config -> ResponseEntity.status(HttpStatus.CREATED).body(
                mapOf(
                    "id" to config.id.toString(),
                    "bankCode" to config.bankCode,
                    "bankName" to config.bankName,
                    "isActive" to config.isActive
                )
            ) }
        )
    }
}

