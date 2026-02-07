package br.com.misterstorm.bankslipgenerator.domain.port

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip

/**
 * Port for online bank registration services
 */
interface BankOnlineRegistrationService {
    suspend fun register(bankslip: Bankslip): Either<DomainError, RegistrationResponse>
    suspend fun cancel(bankslip: Bankslip): Either<DomainError, Unit>
    suspend fun query(bankslip: Bankslip): Either<DomainError, BankslipStatusResponse>
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
 * Bankslip status from bank query
 */
data class BankslipStatusResponse(
    val status: String,
    val registrationId: String? = null,
    val lastUpdate: String? = null,
    val paid: Boolean = false,
    val paidAmount: String? = null
)

