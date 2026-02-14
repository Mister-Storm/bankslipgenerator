package br.com.misterstorm.bankslipgenerator.adapter.output.persistence

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.toDomain
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.toEntity
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc.JdbcBankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankConfiguration
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BankConfigurationRepositoryAdapter(
    private val jdbcRepository: JdbcBankConfigurationRepository
) : BankConfigurationRepository {

    override suspend fun save(configuration: BankConfiguration): Either<DomainError, BankConfiguration> {
        return try {
            val entity = configuration.toEntity()
            val saved = jdbcRepository.save(entity)
            saved.toDomain().right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to save bank configuration", e).left()
        }
    }

    override suspend fun findById(id: UUID): Either<DomainError, BankConfiguration> {
        val entity = jdbcRepository.findById(id)
        return if (entity != null) {
            entity.toDomain().right()
        } else {
            DomainError.BankConfigurationNotFound(id.toString()).left()
        }
    }

    override suspend fun findByBankCode(bankCode: String): Either<DomainError, BankConfiguration> {
        val entity = jdbcRepository.findByBankCode(bankCode)
        return if (entity != null) {
            entity.toDomain().right()
        } else {
            DomainError.BankConfigurationNotFound(bankCode).left()
        }
    }

    override suspend fun findAll(): Either<DomainError, List<BankConfiguration>> {
        return try {
            val entities = jdbcRepository.findAll()
            entities.map { it.toDomain() }.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find all bank configurations", e).left()
        }
    }

    override suspend fun findAllActive(): Either<DomainError, List<BankConfiguration>> {
        return try {
            val entities = jdbcRepository.findAllActive()
            entities.map { it.toDomain() }.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find active bank configurations", e).left()
        }
    }

    override suspend fun update(configuration: BankConfiguration): Either<DomainError, BankConfiguration> {
        return try {
            val entity = configuration.toEntity()
            val updated = jdbcRepository.update(entity)
            updated.toDomain().right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to update bank configuration", e).left()
        }
    }

    override suspend fun delete(id: UUID): Either<DomainError, Unit> {
        return try {
            jdbcRepository.softDelete(id)
            Unit.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to delete bank configuration", e).left()
        }
    }
}
