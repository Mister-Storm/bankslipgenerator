package br.com.misterstorm.bankslipgenerator.adapter.input.rest.controller

import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.CreateWebhookConfigRequest
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.toResponse
import br.com.misterstorm.bankslipgenerator.application.usecase.webhook.CreateWebhookConfigUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.webhook.DeleteWebhookConfigUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.webhook.GetWebhookConfigUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.webhook.TestWebhookUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * REST Controller for webhook management
 */
@RestController
@RequestMapping("/api/webhooks")
class WebhookController(
    private val createWebhookUseCase: CreateWebhookConfigUseCase,
    private val getWebhookUseCase: GetWebhookConfigUseCase,
    private val deleteWebhookUseCase: DeleteWebhookConfigUseCase,
    private val testWebhookUseCase: TestWebhookUseCase
) {

    @PostMapping
    suspend fun createWebhook(@RequestBody request: CreateWebhookConfigRequest): ResponseEntity<Any> {
        val input = CreateWebhookConfigUseCase.Input(
            clientId = request.clientId,
            url = request.url,
            secret = request.secret,
            events = request.events,
            maxRetries = request.maxRetries,
            retryDelay = request.retryDelay
        )

        return createWebhookUseCase(input).fold(
            { error ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    mapOf(
                        "error" to error.message,
                        "details" to error.details
                    )
                )
            },
            { webhook ->
                ResponseEntity.status(HttpStatus.CREATED).body(webhook.toResponse())
            }
        )
    }

    @GetMapping("/{id}")
    suspend fun getWebhook(@PathVariable id: UUID): ResponseEntity<Any> {
        return getWebhookUseCase(id).fold(
            { error ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "error" to error.message,
                        "details" to error.details
                    )
                )
            },
            { webhook ->
                ResponseEntity.ok(webhook.toResponse())
            }
        )
    }

    @DeleteMapping("/{id}")
    suspend fun deleteWebhook(@PathVariable id: UUID): ResponseEntity<Any> {
        return deleteWebhookUseCase(id).fold(
            { error ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "error" to error.message,
                        "details" to error.details
                    )
                )
            },
            {
                ResponseEntity.noContent().build()
            }
        )
    }

    @PostMapping("/{id}/test")
    suspend fun testWebhook(@PathVariable id: UUID): ResponseEntity<Any> {
        return testWebhookUseCase(id).fold(
            { error ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    mapOf(
                        "error" to error.message,
                        "details" to error.details,
                        "message" to "Failed to deliver test webhook"
                    )
                )
            },
            { delivery ->
                ResponseEntity.ok(
                    mapOf(
                        "message" to "Test webhook sent successfully",
                        "delivery-id" to delivery.id,
                        "status-code" to delivery.statusCode,
                        "delivered-at" to delivery.deliveredAt,
                        "attempt-number" to delivery.attemptNumber
                    )
                )
            }
        )
    }
}

