package br.com.misterstorm.bankslipgenerator.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for Jackson kebab-case configuration
 */
class JacksonConfigTest {

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        // Configure ObjectMapper the same way as JacksonConfig
        objectMapper = jacksonObjectMapper().apply {
            propertyNamingStrategy = PropertyNamingStrategies.KEBAB_CASE
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
        }
    }

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
        // Null values are included with kebab-case naming
        assertTrue(json.contains("\"user-name\":null"))
    }
}

