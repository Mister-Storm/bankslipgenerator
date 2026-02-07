package br.com.misterstorm.bankslipgenerator.domain.port

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankConfiguration
import java.util.*

/**
 * Repository port for Bank Configuration persistence
 */
interface BankConfigurationRepository {
    suspend fun save(configuration: BankConfiguration): Either<DomainError, BankConfiguration>
    suspend fun findById(id: UUID): Either<DomainError, BankConfiguration>
    suspend fun findByBankCode(bankCode: String): Either<DomainError, BankConfiguration>
    suspend fun findAll(): Either<DomainError, List<BankConfiguration>>
    suspend fun findAllActive(): Either<DomainError, List<BankConfiguration>>
    suspend fun update(configuration: BankConfiguration): Either<DomainError, BankConfiguration>
    suspend fun delete(id: UUID): Either<DomainError, Unit>
}

