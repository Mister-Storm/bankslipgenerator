package br.com.misterstorm.bankslipgenerator.adapter.input.rest.controller

import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.GenerateRemittanceRequest
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.ProcessReturnRequest
import br.com.misterstorm.bankslipgenerator.application.usecase.cnab.GenerateRemittanceFileUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.cnab.ProcessReturnFileUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for CNAB file operations
 */
@RestController
@RequestMapping("/api/cnab")
class CnabController(
    private val generateRemittanceFileUseCase: GenerateRemittanceFileUseCase,
    private val processReturnFileUseCase: ProcessReturnFileUseCase
) {

    @PostMapping("/remittance")
    suspend fun generateRemittanceFile(
        @RequestBody request: GenerateRemittanceRequest,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<Map<String, Any>> {
        val input = GenerateRemittanceFileUseCase.Input(
            bankCode = request.bankCode,
            version = request.version,
            startDate = request.startDate,
            endDate = request.endDate
        )

        return generateRemittanceFileUseCase(input).fold(
            { error -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("error" to error.message, "details" to error.details)
            ) },
            { cnabFile -> ResponseEntity.status(HttpStatus.CREATED).body(
                mapOf(
                    "id" to cnabFile.id.toString(),
                    "fileName" to cnabFile.fileName,
                    "fileUrl" to (cnabFile.fileUrl ?: ""),
                    "totalRecords" to cnabFile.totalRecords,
                    "status" to cnabFile.status.name
                )
            ) }
        )
    }

    @PostMapping("/return")
    suspend fun processReturnFile(
        @RequestBody request: ProcessReturnRequest,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<Map<String, Any>> {
        val input = ProcessReturnFileUseCase.Input(
            bankCode = request.bankCode,
            version = request.version,
            fileContent = request.fileContent
        )

        return processReturnFileUseCase(input).fold(
            { error -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("error" to error.message, "details" to error.details)
            ) },
            { cnabFile -> ResponseEntity.ok(
                mapOf(
                    "id" to cnabFile.id.toString(),
                    "fileName" to cnabFile.fileName,
                    "totalRecords" to cnabFile.totalRecords,
                    "processedRecords" to cnabFile.processedRecords,
                    "errorRecords" to cnabFile.errorRecords,
                    "status" to cnabFile.status.name,
                    "errors" to cnabFile.errors.map { mapOf(
                        "recordNumber" to it.recordNumber,
                        "errorCode" to it.errorCode,
                        "errorMessage" to it.errorMessage
                    )}
                )
            ) }
        )
    }
}

