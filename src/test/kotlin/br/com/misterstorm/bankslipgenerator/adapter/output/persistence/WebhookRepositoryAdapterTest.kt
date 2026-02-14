package br.com.misterstorm.bankslipgenerator.adapter.output.persistence

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc.JdbcWebhookRepository
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.WebhookConfigEntity
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookConfig
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookEventType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * Unit tests for WebhookRepositoryAdapter
 */
class WebhookRepositoryAdapterTest {

    private lateinit var jdbcRepository: JdbcWebhookRepository
    private lateinit var adapter: WebhookRepositoryAdapter

    @BeforeEach
    fun setup() {
        jdbcRepository = mockk()
        adapter = WebhookRepositoryAdapter(jdbcRepository)
    }

    @Test
    fun `ensure saveConfig saves webhook configuration successfully`() = runTest {
        // Arrange
        val webhookConfig = WebhookConfig(
            id = UUID.randomUUID(),
            clientId = "test-client",
            url = "https://test.com/webhook",
            secret = "secret",
            events = listOf(WebhookEventType.BANKSLIP_CREATED),
            isActive = true,
            maxRetries = 3,
            retryDelay = 5000,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val entity = WebhookConfigEntity(
            id = webhookConfig.id,
            clientId = webhookConfig.clientId,
            url = webhookConfig.url,
            secret = webhookConfig.secret,
            events = """["BANKSLIP_CREATED"]""",
            isActive = webhookConfig.isActive,
            maxRetries = webhookConfig.maxRetries,
            retryDelay = webhookConfig.retryDelay,
            createdAt = webhookConfig.createdAt,
            updatedAt = webhookConfig.updatedAt
        )

        every { jdbcRepository.saveConfig(any()) } returns entity

        // Act
        val result = adapter.saveConfig(webhookConfig)

        // Assert
        assertTrue(result.isRight())
        result.onRight { saved ->
            assertEquals(webhookConfig.id, saved.id)
            assertEquals(webhookConfig.clientId, saved.clientId)
            assertEquals(webhookConfig.url, saved.url)
        }
        verify(exactly = 1) { jdbcRepository.saveConfig(any()) }
    }

    @Test
    fun `ensure findConfigById returns webhook when exists`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val entity = WebhookConfigEntity(
            id = id,
            clientId = "test-client",
            url = "https://test.com",
            secret = "secret",
            events = """["BANKSLIP_PAID"]""",
            isActive = true,
            maxRetries = 3,
            retryDelay = 5000,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { jdbcRepository.findConfigById(id) } returns entity

        // Act
        val result = adapter.findConfigById(id)

        // Assert
        assertTrue(result.isRight())
        result.onRight { config ->
            assertEquals(id, config.id)
            assertEquals("test-client", config.clientId)
        }
    }

    @Test
    fun `ensure findConfigById returns error when not found`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        every { jdbcRepository.findConfigById(id) } returns null

        // Act
        val result = adapter.findConfigById(id)

        // Assert
        assertTrue(result.isLeft())
    }

    @Test
    fun `ensure findActiveConfigs returns all active webhooks`() = runTest {
        // Arrange
        val entities = listOf(
            WebhookConfigEntity(
                id = UUID.randomUUID(),
                clientId = "client1",
                url = "https://test1.com",
                secret = "secret1",
                events = """["BANKSLIP_CREATED"]""",
                isActive = true,
                maxRetries = 3,
                retryDelay = 5000,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            WebhookConfigEntity(
                id = UUID.randomUUID(),
                clientId = "client2",
                url = "https://test2.com",
                secret = "secret2",
                events = """["BANKSLIP_PAID"]""",
                isActive = true,
                maxRetries = 3,
                retryDelay = 5000,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        every { jdbcRepository.findActiveConfigs() } returns entities

        // Act
        val result = adapter.findActiveConfigs()

        // Assert
        assertTrue(result.isRight())
        result.onRight { configs ->
            assertEquals(2, configs.size)
            assertEquals("client1", configs[0].clientId)
            assertEquals("client2", configs[1].clientId)
        }
    }

    @Test
    fun `ensure deleteConfig soft deletes webhook`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        every { jdbcRepository.softDeleteConfig(id) } returns Unit

        // Act
        val result = adapter.deleteConfig(id)

        // Assert
        assertTrue(result.isRight())
        verify(exactly = 1) { jdbcRepository.softDeleteConfig(id) }
    }
}

