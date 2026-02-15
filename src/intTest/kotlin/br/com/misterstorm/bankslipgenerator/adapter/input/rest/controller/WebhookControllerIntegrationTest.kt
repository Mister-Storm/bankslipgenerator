package br.com.misterstorm.bankslipgenerator.adapter.input.rest.controller

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Integration tests for WebhookController
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class WebhookControllerIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    companion object {
        @Container
        @JvmStatic
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("bankslip_test")
            withUsername("test")
            withPassword("test")
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.flyway.enabled") { "true" }
        }
    }

    @BeforeEach
    fun setup() {
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Test
    fun `ensure creates webhook config successfully`() {
        // Arrange
        val request = mapOf(
            "client-id" to "test-client",
            "url" to "https://webhook.example.com/events",
            "secret" to "super-secret-key-123",
            "events" to listOf("BANKSLIP_CREATED", "BANKSLIP_PAID"),
            "max-retries" to 3,
            "retry-delay" to 5000
        )

        // Act & Assert
        Given {
            contentType(ContentType.JSON)
            body(request)
            header("API-Version", "v1")
        } When {
            post("/api/webhooks")
        } Then {
            statusCode(201)
            body("id", notNullValue())
            body("client-id", equalTo("test-client"))
            body("url", equalTo("https://webhook.example.com/events"))
            body("events", hasSize<Any>(2))
            body("is-active", equalTo(true))
            body("max-retries", equalTo(3))
            body("retry-delay", equalTo(5000))
            body("created-at", notNullValue())
        }
    }

    @Test
    fun `ensure gets webhook config by id`() {
        // Arrange - Create webhook first
        val createRequest = mapOf(
            "client-id" to "get-test-client",
            "url" to "https://webhook.test.com/notify",
            "secret" to "secret-123",
            "events" to listOf("BANKSLIP_REGISTERED")
        )

        val webhookId: String = Given {
            contentType(ContentType.JSON)
            body(createRequest)
        } When {
            post("/api/webhooks")
        } Extract {
            path("id")
        }

        // Act & Assert
        Given {
            header("API-Version", "v1")
        } When {
            get("/api/webhooks/$webhookId")
        } Then {
            statusCode(200)
            body("id", equalTo(webhookId))
            body("client-id", equalTo("get-test-client"))
            body("url", equalTo("https://webhook.test.com/notify"))
        }
    }

    @Test
    fun `ensure returns 404 for non-existent webhook`() {
        Given {
            header("API-Version", "v1")
        } When {
            get("/api/webhooks/00000000-0000-0000-0000-000000000000")
        } Then {
            statusCode(404)
            body("error", notNullValue())
        }
    }

    @Test
    fun `ensure deletes webhook successfully`() {
        // Arrange - Create webhook first
        val createRequest = mapOf(
            "client-id" to "delete-test",
            "url" to "https://delete.test.com",
            "secret" to "secret",
            "events" to listOf("BANKSLIP_CANCELLED")
        )

        val webhookId: String = Given {
            contentType(ContentType.JSON)
            body(createRequest)
        } When {
            post("/api/webhooks")
        } Extract {
            path("id")
        }

        // Act & Assert - Delete
        Given {
            header("API-Version", "v1")
        } When {
            delete("/api/webhooks/$webhookId")
        } Then {
            statusCode(204)
        }

        // Verify deleted
        Given {
            header("API-Version", "v1")
        } When {
            get("/api/webhooks/$webhookId")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `ensure tests webhook delivery`() {
        // Arrange - Create webhook first
        val createRequest = mapOf(
            "client-id" to "test-delivery",
            "url" to "https://httpbin.org/post", // Public test endpoint
            "secret" to "test-secret",
            "events" to listOf("BANKSLIP_CREATED")
        )

        val webhookId: String = Given {
            contentType(ContentType.JSON)
            body(createRequest)
        } When {
            post("/api/webhooks")
        } Extract {
            path("id")
        }

        // Act & Assert - Test delivery
        Given {
            header("API-Version", "v1")
        } When {
            post("/api/webhooks/$webhookId/test")
        } Then {
            statusCode(200)
            body("message", equalTo("Test webhook sent successfully"))
            body("delivery-id", notNullValue())
            body("status-code", notNullValue())
        }
    }

    @Test
    fun `ensure validates webhook request fields`() {
        // Arrange - Invalid request (missing required fields)
        val invalidRequest = mapOf(
            "client-id" to "test",
            "url" to "not-a-valid-url",
            "events" to emptyList<String>()
        )

        // Act & Assert
        Given {
            contentType(ContentType.JSON)
            body(invalidRequest)
            header("API-Version", "v1")
        } When {
            post("/api/webhooks")
        } Then {
            statusCode(400)
            body("error", notNullValue())
        }
    }

    @Test
    fun `ensure supports multiple event types`() {
        // Arrange
        val request = mapOf(
            "client-id" to "multi-event-client",
            "url" to "https://multi.test.com/webhook",
            "secret" to "multi-secret",
            "events" to listOf(
                "BANKSLIP_CREATED",
                "BANKSLIP_REGISTERED",
                "BANKSLIP_PAID",
                "BANKSLIP_CANCELLED",
                "BANKSLIP_EXPIRED"
            )
        )

        // Act & Assert
        Given {
            contentType(ContentType.JSON)
            body(request)
            header("API-Version", "v1")
        } When {
            post("/api/webhooks")
        } Then {
            statusCode(201)
            body("events", hasSize<Any>(5))
            body("events", hasItems(
                "BANKSLIP_CREATED",
                "BANKSLIP_PAID",
                "BANKSLIP_CANCELLED"
            ))
        }
    }
}

