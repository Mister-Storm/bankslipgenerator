package br.com.misterstorm.bankslipgenerator.domain.port

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip

/**
 * Port for online bank registration services
 */
interface BankOnlineRegistrationService {
    suspend fun register(bankSlip: BankSlip): Either<DomainError, RegistrationResponse>
    suspend fun cancel(bankSlip: BankSlip): Either<DomainError, Unit>
    suspend fun query(bankSlip: BankSlip): Either<DomainError, BankSlipStatusResponse>
    fun supports(bankCode: String): Boolean
}

/**
 * Response from bank registration
 */
data class RegistrationResponse(
    val success: Boolean,
    val registrationId: String? = null,
    val message: String? = null,
    val errorCode: String? = null
)

/**
 * BankSlip status from bank query
 */
data class BankSlipStatusResponse(
    val status: String,
    val registrationId: String? = null,
    val lastUpdate: String? = null,
    val paid: Boolean = false,
    val paidAmount: String? = null
)
