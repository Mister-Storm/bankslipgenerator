package br.com.misterstorm.bankslipgenerator.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for Jackson kebab-case configuration
 */
@SpringBootTest
class JacksonConfigTest {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    data class TestDto(
        val userId: String,
        val userName: String,
        val maxRetries: Int,
        val isActive: Boolean
    )

    @Test
    fun `ensure serializes to kebab-case`() {
        // Arrange
        val dto = TestDto(
            userId = "123",
            userName = "John Doe",
            maxRetries = 3,
            isActive = true
        )

        // Act
        val json = objectMapper.writeValueAsString(dto)

        // Assert
        assertTrue(json.contains("\"user-id\""))
        assertTrue(json.contains("\"user-name\""))
        assertTrue(json.contains("\"max-retries\""))
        assertTrue(json.contains("\"is-active\""))
    }

    @Test
    fun `ensure deserializes from kebab-case`() {
        // Arrange
        val json = """
            {
                "user-id": "123",
                "user-name": "John Doe",
                "max-retries": 3,
                "is-active": true
            }
        """.trimIndent()

        // Act
        val dto = objectMapper.readValue(json, TestDto::class.java)

        // Assert
        assertEquals("123", dto.userId)
        assertEquals("John Doe", dto.userName)
        assertEquals(3, dto.maxRetries)
        assertEquals(true, dto.isActive)
    }

    @Test
    fun `ensure handles null values correctly`() {
        // Arrange
        data class NullableDto(
            val userId: String?,
            val userName: String?
        )

        val dto = NullableDto(
            userId = "123",
            userName = null
        )

        // Act
        val json = objectMapper.writeValueAsString(dto)

        // Assert
        assertTrue(json.contains("\"user-id\""))
        assertTrue(!json.contains("user-name")) // Null values excluded
    }
}

