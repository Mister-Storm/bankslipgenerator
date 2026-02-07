package br.com.misterstorm.bankslipgenerator.adapter.input.rest.controller

import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.*
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.*
import br.com.misterstorm.bankslipgenerator.domain.model.BankslipStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

/**
 * REST controller for bankslip operations
 * API Version: v1 (header-based versioning)
 */
@RestController
@RequestMapping("/api/bankslips")
class BankslipController(
    private val createBankslipUseCase: CreateBankslipUseCase,
    private val getBankslipUseCase: GetBankslipUseCase,
    private val deleteBankslipUseCase: DeleteBankslipUseCase,
    private val payBankslipUseCase: PayBankslipUseCase,
    private val registerBankslipUseCase: RegisterBankslipUseCase,
    private val registerBankslipOnlineUseCase: RegisterBankslipOnlineUseCase,
    private val generateBankslipPdfUseCase: GenerateBankslipPdfUseCase
) {

    @PostMapping
    suspend fun createBankslip(
        @RequestBody request: CreateBankslipRequest,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<BankslipResponse> {
        val input = CreateBankslipUseCase.Input(
            bankCode = request.bankCode,
            amount = request.amount,
            dueDate = request.dueDate,
            payer = request.toPayer(),
            beneficiary = request.toBeneficiary(),
            instructions = request.instructions,
            discount = request.discount?.toDomain(),
            fine = request.fine?.toDomain(),
            interest = request.interest?.toDomain()
        )

        return createBankslipUseCase(input).fold(
            { error -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null) },
            { bankslip -> ResponseEntity.status(HttpStatus.CREATED).body(bankslip.toResponse()) }
        )
    }

    @GetMapping("/{id}")
    suspend fun getBankslip(
        @PathVariable id: UUID,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<BankslipResponse> {
        return getBankslipUseCase(id).fold(
            { error -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null) },
            { bankslip -> ResponseEntity.ok(bankslip.toResponse()) }
        )
    }

    @DeleteMapping("/{id}")
    suspend fun deleteBankslip(
        @PathVariable id: UUID,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<Unit> {
        return deleteBankslipUseCase(id).fold(
            { error -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build() },
            { ResponseEntity.noContent().build() }
        )
    }

    @PostMapping("/{id}/pay")
    suspend fun payBankslip(
        @PathVariable id: UUID,
        @RequestBody request: PayBankslipRequest,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<BankslipResponse> {
        val input = PayBankslipUseCase.Input(
            bankslipId = id,
            paymentDate = request.paymentDate,
            paidAmount = request.paidAmount
        )

        return payBankslipUseCase(input).fold(
            { error -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null) },
            { bankslip -> ResponseEntity.ok(bankslip.toResponse()) }
        )
    }

    @PostMapping("/{id}/register")
    suspend fun registerBankslip(
        @PathVariable id: UUID,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<BankslipResponse> {
        return registerBankslipUseCase(id).fold(
            { error -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null) },
            { bankslip -> ResponseEntity.ok(bankslip.toResponse()) }
        )
    }

    @PostMapping("/{id}/register-online")
    suspend fun registerBankslipOnline(
        @PathVariable id: UUID,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<BankslipResponse> {
        return registerBankslipOnlineUseCase(id).fold(
            { error -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null) },
            { bankslip -> ResponseEntity.ok(bankslip.toResponse()) }
        )
    }

    @GetMapping("/{id}/pdf")
    suspend fun generatePdf(
        @PathVariable id: UUID,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<String> {
        return generateBankslipPdfUseCase(id).fold(
            { error -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null) },
            { url -> ResponseEntity.ok(url) }
        )
    }
}

