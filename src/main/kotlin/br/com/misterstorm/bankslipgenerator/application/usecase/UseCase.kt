package br.com.misterstorm.bankslipgenerator.application.usecase

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger

/**
 * Base abstract class for all use cases following functional approach with Either
 */
abstract class UseCase<T, U>(
    protected val logger: Logger
) {
    /**
     * Execute the use case with input T and return Either<DomainError, U>
     */
    protected abstract suspend fun execute(input: T): Either<DomainError, U>

    /**
     * Invoke operator for functional style invocation
     */
    suspend operator fun invoke(input: T): Either<DomainError, U> {
        logger.info(
            "Executing use case",
            "useCase" to this::class.simpleName.orEmpty(),
            "input" to input.toString()
        )
        
        return try {
            execute(input).also { result ->
                result.fold(
                    { error ->
                        logger.error(
                            "Use case execution failed",
                            "useCase" to this::class.simpleName.orEmpty(),
                            "error" to error.message,
                            "details" to error.details
                        )
                    },
                    { _ ->
                        logger.info(
                            "Use case execution succeeded",
                            "useCase" to this::class.simpleName.orEmpty()
                        )
                    }
                )
            }
        } catch (e: Exception) {
            logger.error(
                "Unexpected error during use case execution",
                e,
                "useCase" to this::class.simpleName.orEmpty()
            )
            Either.Left(DomainError.UnexpectedError(e.message ?: "Unknown error", e))
        }
    }
}

