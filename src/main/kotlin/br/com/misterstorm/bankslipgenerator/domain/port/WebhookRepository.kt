package br.com.misterstorm.bankslipgenerator.domain.port
import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookConfig
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookDelivery
import java.util.UUID

interface WebhookRepository {
    suspend fun saveConfig(config: WebhookConfig): Either<DomainError, WebhookConfig>
    suspend fun findConfigById(id: UUID): Either<DomainError, WebhookConfig>
    suspend fun findActiveConfigs(): Either<DomainError, List<WebhookConfig>>
    suspend fun saveDelivery(delivery: WebhookDelivery): Either<DomainError, WebhookDelivery>
    suspend fun findDeliveriesByConfig(configId: UUID, page: Int, size: Int): Either<DomainError, List<WebhookDelivery>>
    suspend fun findFailedDeliveries(maxAttempts: Int): Either<DomainError, List<WebhookDelivery>>
    suspend fun deleteConfig(id: UUID): Either<DomainError, Unit>
}
