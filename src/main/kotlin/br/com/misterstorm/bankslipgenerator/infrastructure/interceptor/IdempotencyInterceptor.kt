package br.com.misterstorm.bankslipgenerator.infrastructure.interceptor

import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.ContentCachingResponseWrapper
import java.security.MessageDigest
import java.time.LocalDateTime

/**
 * Interceptor for idempotent request handling
 * Uses Idempotency-Key header to prevent duplicate operations
 */
@Component
class IdempotencyInterceptor(
    private val jdbcTemplate: JdbcTemplate,
    private val logger: Logger
) : HandlerInterceptor {

    private data class IdempotencyRecord(
        val responseBody: String,
        val statusCode: Int
    )

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val key = request.getHeader("Idempotency-Key") ?: return true

        try {
            // Check if key exists and not expired
            val cached = jdbcTemplate.queryForObject(
                """
                SELECT response_body, status_code 
                FROM idempotency_keys 
                WHERE key = ? AND expires_at > NOW()
                """,
                { rs, _ ->
                    IdempotencyRecord(
                        responseBody = rs.getString("response_body"),
                        statusCode = rs.getInt("status_code")
                    )
                },
                key
            )

            // If we reach here, a record was found
            if (cached != null) {
                logger.info(
                    "Returning cached response for idempotency key",
                    "key" to key,
                    "statusCode" to cached.statusCode
                )

                response.status = cached.statusCode
                response.contentType = "application/json"
                response.writer.write(cached.responseBody)
                return false
            }
        } catch (e: Exception) {
            // Key not found or expired, continue with request
            logger.debug("No cached response for idempotency key", "key" to key)
        }

        // Store key in request for afterCompletion
        request.setAttribute("idempotency-key", key)
        request.setAttribute("idempotency-endpoint", request.requestURI)

        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val key = request.getAttribute("idempotency-key") as? String ?: return
        val endpoint = request.getAttribute("idempotency-endpoint") as? String ?: return

        // Only cache successful responses (2xx)
        if (response.status in 200..299) {
            try {
                val responseWrapper = response as? ContentCachingResponseWrapper
                val responseBody = responseWrapper?.contentAsByteArray?.toString(Charsets.UTF_8) ?: ""
                val requestHash = hashRequest(request)

                val expiresAt = LocalDateTime.now().plusHours(24)

                jdbcTemplate.update(
                    """
                    INSERT INTO idempotency_keys (key, endpoint, request_hash, response_body, status_code, expires_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON CONFLICT (key) DO NOTHING
                    """,
                    key,
                    endpoint,
                    requestHash,
                    responseBody,
                    response.status,
                    expiresAt
                )

                logger.info(
                    "Cached response for idempotency key",
                    "key" to key,
                    "statusCode" to response.status
                )
            } catch (e: Exception) {
                logger.error("Failed to cache idempotency response", e, "key" to key)
            }
        }
    }

    private fun hashRequest(request: HttpServletRequest): String {
        val data = "${request.method}:${request.requestURI}:${request.queryString ?: ""}"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}

