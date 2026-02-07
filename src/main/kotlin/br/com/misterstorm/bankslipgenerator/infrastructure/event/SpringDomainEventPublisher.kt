package br.com.misterstorm.bankslipgenerator.infrastructure.event

import br.com.misterstorm.bankslipgenerator.domain.event.DomainEvent
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEventPublisher
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * Spring implementation of DomainEventPublisher using ApplicationEventPublisher
 */
@Component
class SpringDomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val logger: Logger
) : DomainEventPublisher {

    @Async
    override suspend fun publish(event: DomainEvent) {
        withContext(Dispatchers.IO) {
            try {
                logger.info(
                    "Publishing domain event",
                    "eventId" to event.eventId.toString(),
                    "eventType" to event::class.simpleName.orEmpty(),
                    "aggregateId" to event.aggregateId.toString(),
                    "occurredOn" to event.occurredOn.toString()
                )
                applicationEventPublisher.publishEvent(event)

                logger.debug(
                    "Domain event published successfully",
                    "eventId" to event.eventId.toString()
                )
            } catch (e: Exception) {
                logger.error(
                    "Failed to publish domain event",
                    e,
                    "eventId" to event.eventId.toString(),
                    "eventType" to event::class.simpleName.orEmpty()
                )
                throw e
            }
        }
    }

    override suspend fun publishAll(events: List<DomainEvent>) {
        logger.info(
            "Publishing multiple domain events",
            "eventCount" to events.size
        )
        events.forEach { publish(it) }
    }
}

