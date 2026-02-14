package br.com.misterstorm.bankslipgenerator.adapter.input.rest.controller

import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.BankSlipResponse
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.CreateBankSlipRequest
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.PayBankSlipRequest
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.toBeneficiary
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.toDomain
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.toPayer
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.toResponse
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.CreateBankSlipUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.DeleteBankSlipUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.GenerateBankSlipPdfUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.GetBankSlipUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.PayBankSlipUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.RegisterBankSlipOnlineUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.RegisterBankSlipUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * REST controller for BankSlip operations
 * API Version: v1 (header-based versioning)
 */
@RestController
@RequestMapping("/api/bankslips")
class BankSlipController(
    private val createBankSlipUseCase: CreateBankSlipUseCase,
    private val getBankSlipUseCase: GetBankSlipUseCase,
    private val deleteBankSlipUseCase: DeleteBankSlipUseCase,
    private val payBankSlipUseCase: PayBankSlipUseCase,
    private val registerBankSlipUseCase: RegisterBankSlipUseCase,
    private val registerBankSlipOnlineUseCase: RegisterBankSlipOnlineUseCase,
    private val generateBankSlipPdfUseCase: GenerateBankSlipPdfUseCase
) {

    @PostMapping
    @Suppress("UNUSED_PARAMETER")
    suspend fun createBankSlip(
        @RequestBody request: CreateBankSlipRequest,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<BankSlipResponse> {
        val input = CreateBankSlipUseCase.Input(
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

        return createBankSlipUseCase(input).fold(
            { _ -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null) },
            { bankSlip -> ResponseEntity.status(HttpStatus.CREATED).body(bankSlip.toResponse()) }
        )
    }

    @GetMapping("/{id}")
    @Suppress("UNUSED_PARAMETER")
    suspend fun getBankSlip(
        @PathVariable id: UUID,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<BankSlipResponse> {
        return getBankSlipUseCase(id).fold(
            { _ -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null) },
            { bankSlip -> ResponseEntity.ok(bankSlip.toResponse()) }
        )
    }

    @DeleteMapping("/{id}")
    @Suppress("UNUSED_PARAMETER")
    suspend fun deleteBankSlip(
        @PathVariable id: UUID,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<Unit> {
        return deleteBankSlipUseCase(id).fold(
            { _ -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build() },
            { ResponseEntity.noContent().build() }
        )
    }

    @PostMapping("/{id}/pay")
    @Suppress("UNUSED_PARAMETER")
    suspend fun payBankSlip(
        @PathVariable id: UUID,
        @RequestBody request: PayBankSlipRequest,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<BankSlipResponse> {
        val input = PayBankSlipUseCase.Input(
            bankSlipId = id,
            paymentDate = request.paymentDate,
            paidAmount = request.paidAmount
        )

        return payBankSlipUseCase(input).fold(
            { _ -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null) },
            { bankSlip -> ResponseEntity.ok(bankSlip.toResponse()) }
        )
    }

    @PostMapping("/{id}/register")
    @Suppress("UNUSED_PARAMETER")
    suspend fun registerBankSlip(
        @PathVariable id: UUID,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<BankSlipResponse> {
        return registerBankSlipUseCase(id).fold(
            { _ -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null) },
            { bankSlip -> ResponseEntity.ok(bankSlip.toResponse()) }
        )
    }

    @PostMapping("/{id}/register-online")
    @Suppress("UNUSED_PARAMETER")
    suspend fun registerBankSlipOnline(
        @PathVariable id: UUID,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<BankSlipResponse> {
        return registerBankSlipOnlineUseCase(id).fold(
            { _ -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null) },
            { bankSlip -> ResponseEntity.ok(bankSlip.toResponse()) }
        )
    }

    @GetMapping("/{id}/pdf")
    @Suppress("UNUSED_PARAMETER")
    suspend fun generatePdf(
        @PathVariable id: UUID,
        @RequestHeader("API-Version", defaultValue = "v1") apiVersion: String
    ): ResponseEntity<String> {
        return generateBankSlipPdfUseCase(id).fold(
            { _ -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null) },
            { url -> ResponseEntity.ok(url) }
        )
    }
}
