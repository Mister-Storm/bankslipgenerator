package br.com.misterstorm.bankslipgenerator.adapter.output.persistence

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.toDomain
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.toEntity
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc.JdbcWebhookRepository
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookConfig
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookDelivery
import br.com.misterstorm.bankslipgenerator.domain.port.WebhookRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class WebhookRepositoryAdapter(
    private val jdbcRepository: JdbcWebhookRepository
) : WebhookRepository {

    override suspend fun saveConfig(config: WebhookConfig): Either<DomainError, WebhookConfig> {
        return try {
            val entity = config.toEntity()
            val saved = jdbcRepository.saveConfig(entity)
            saved.toDomain().right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to save webhook config", e).left()
        }
    }

    override suspend fun findConfigById(id: UUID): Either<DomainError, WebhookConfig> {
        val entity = jdbcRepository.findConfigById(id)
        return if (entity != null) {
            entity.toDomain().right()
        } else {
            DomainError.WebhookNotFound(id.toString()).left()
        }
    }

    override suspend fun findActiveConfigs(): Either<DomainError, List<WebhookConfig>> {
        return try {
            val entities = jdbcRepository.findActiveConfigs()
            entities.map { it.toDomain() }.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find active webhook configs", e).left()
        }
    }

    override suspend fun saveDelivery(delivery: WebhookDelivery): Either<DomainError, WebhookDelivery> {
        return try {
            val entity = delivery.toEntity()
            val saved = jdbcRepository.saveDelivery(entity)
            saved.toDomain().right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to save webhook delivery", e).left()
        }
    }

    override suspend fun findDeliveriesByConfig(
        configId: UUID,
        page: Int,
        size: Int
    ): Either<DomainError, List<WebhookDelivery>> {
        return try {
            val allEntities = jdbcRepository.findDeliveriesByConfigId(configId)
            val paginated = allEntities.drop(page * size).take(size)
            paginated.map { it.toDomain() }.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find webhook deliveries", e).left()
        }
    }

    override suspend fun findFailedDeliveries(maxAttempts: Int): Either<DomainError, List<WebhookDelivery>> {
        return try {
            val entities = jdbcRepository.findFailedDeliveries(maxAttempts)
            entities.map { it.toDomain() }.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find failed webhook deliveries", e).left()
        }
    }

    override suspend fun deleteConfig(id: UUID): Either<DomainError, Unit> {
        return try {
            jdbcRepository.softDeleteConfig(id)
            Unit.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to delete webhook config", e).left()
        }
    }
}
