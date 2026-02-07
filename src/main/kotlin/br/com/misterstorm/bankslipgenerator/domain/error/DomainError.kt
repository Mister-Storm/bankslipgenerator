package br.com.misterstorm.bankslipgenerator.domain.error

/**
 * Base sealed class for all domain errors
 */
sealed class DomainError(
    open val message: String,
    open val details: Map<String, Any> = emptyMap()
) {
    data class BankslipNotFound(
        val bankslipId: String,
        override val message: String = "Bankslip not found",
        override val details: Map<String, Any> = mapOf("bankslipId" to bankslipId)
    ) : DomainError(message, details)

    data class InvalidBarcode(
        override val message: String = "Invalid barcode",
        override val details: Map<String, Any> = emptyMap()
    ) : DomainError(message, details)

    data class InvalidDueDate(
        val dueDate: String,
        override val message: String = "Invalid due date",
        override val details: Map<String, Any> = mapOf("dueDate" to dueDate)
    ) : DomainError(message, details)

    data class InvalidAmount(
        val amount: String,
        override val message: String = "Invalid amount",
        override val details: Map<String, Any> = mapOf("amount" to amount)
    ) : DomainError(message, details)

    data class BankslipAlreadyPaid(
        val bankslipId: String,
        override val message: String = "Bankslip already paid",
        override val details: Map<String, Any> = mapOf("bankslipId" to bankslipId)
    ) : DomainError(message, details)

    data class BankslipAlreadyCancelled(
        val bankslipId: String,
        override val message: String = "Bankslip already cancelled",
        override val details: Map<String, Any> = mapOf("bankslipId" to bankslipId)
    ) : DomainError(message, details)

    data class InvalidStatusTransition(
        val from: String,
        val to: String,
        override val message: String = "Invalid status transition",
        override val details: Map<String, Any> = mapOf("from" to from, "to" to to)
    ) : DomainError(message, details)

    data class BankConfigurationNotFound(
        val bankCode: String,
        override val message: String = "Bank configuration not found",
        override val details: Map<String, Any> = mapOf("bankCode" to bankCode)
    ) : DomainError(message, details)

    data class LayoutTemplateNotFound(
        val templateId: String,
        override val message: String = "Layout template not found",
        override val details: Map<String, Any> = mapOf("templateId" to templateId)
    ) : DomainError(message, details)

    data class CnabGenerationFailed(
        override val message: String = "CNAB generation failed",
        override val details: Map<String, Any> = emptyMap()
    ) : DomainError(message, details)

    data class CnabProcessingFailed(
        override val message: String = "CNAB processing failed",
        override val details: Map<String, Any> = emptyMap()
    ) : DomainError(message, details)

    data class PdfGenerationFailed(
        override val message: String = "PDF generation failed",
        override val details: Map<String, Any> = emptyMap()
    ) : DomainError(message, details)

    data class ValidationFailed(
        val violations: List<String>,
        override val message: String = "Validation failed",
        override val details: Map<String, Any> = mapOf("violations" to violations)
    ) : DomainError(message, details)

    data class FileStorageFailed(
        override val message: String = "File storage failed",
        override val details: Map<String, Any> = emptyMap()
    ) : DomainError(message, details)

    data class WebhookConfigNotFound(
        val webhookId: String,
        override val message: String = "Webhook configuration not found",
        override val details: Map<String, Any> = mapOf("webhookId" to webhookId)
    ) : DomainError(message, details)

    data class WebhookNotFound(
        val webhookId: String,
        override val message: String = "Webhook not found",
        override val details: Map<String, Any> = mapOf("webhookId" to webhookId)
    ) : DomainError(message, details)

    data class UnexpectedError(
        override val message: String = "An unexpected error occurred",
        val throwable: Throwable? = null,
        override val details: Map<String, Any> = emptyMap()
    ) : DomainError(message, details) {
        constructor(message: String, throwable: Throwable) : this(
            message = message,
            throwable = throwable,
            details = mapOf("exception" to throwable::class.simpleName.orEmpty())
        )
    }
}

