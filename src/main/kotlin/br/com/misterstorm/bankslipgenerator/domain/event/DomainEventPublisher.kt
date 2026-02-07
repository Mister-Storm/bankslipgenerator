package br.com.misterstorm.bankslipgenerator.domain.event

/**
 * Publisher interface for domain events
 */
interface DomainEventPublisher {
    suspend fun publish(event: DomainEvent)
    suspend fun publishAll(events: List<DomainEvent>)
}

/**
 * Handler interface for domain events
 */
interface DomainEventHandler<T : DomainEvent> {
    suspend fun handle(event: T)
    fun canHandle(event: DomainEvent): Boolean
}

