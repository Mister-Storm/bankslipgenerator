package br.com.misterstorm.bankslipgenerator.adapter.output.persistence

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.toDomain
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.toEntity
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc.JdbcCnabFileRepository
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFile
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFileType
import br.com.misterstorm.bankslipgenerator.domain.port.CnabFileRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CnabFileRepositoryAdapter(
    private val jdbcRepository: JdbcCnabFileRepository
) : CnabFileRepository {

    override suspend fun save(cnabFile: CnabFile): Either<DomainError, CnabFile> {
        return try {
            val entity = cnabFile.toEntity()
            val saved = jdbcRepository.save(entity)
            saved.toDomain().right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to save CNAB file", e).left()
        }
    }

    override suspend fun findById(id: UUID): Either<DomainError, CnabFile> {
        val entity = jdbcRepository.findById(id)
        return if (entity != null) {
            entity.toDomain().right()
        } else {
            DomainError.CnabGenerationFailed(
                message = "CNAB file not found",
                details = mapOf("id" to id.toString())
            ).left()
        }
    }

    override suspend fun findByBankCode(
        bankCode: String,
        page: Int,
        size: Int
    ): Either<DomainError, List<CnabFile>> {
        return try {
            val allEntities = jdbcRepository.findByBankCode(bankCode)
            val paginated = allEntities.drop(page * size).take(size)
            paginated.map { it.toDomain() }.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find CNAB files by bank code", e).left()
        }
    }

    override suspend fun findByFileType(
        fileType: CnabFileType,
        page: Int,
        size: Int
    ): Either<DomainError, List<CnabFile>> {
        return try {
            val allEntities = jdbcRepository.findByFileType(fileType)
            val paginated = allEntities.drop(page * size).take(size)
            paginated.map { it.toDomain() }.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find CNAB files by type", e).left()
        }
    }

    override suspend fun update(cnabFile: CnabFile): Either<DomainError, CnabFile> {
        return try {
            val entity = cnabFile.toEntity()
            val updated = jdbcRepository.update(entity)
            updated.toDomain().right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to update CNAB file", e).left()
        }
    }
}

