package br.com.misterstorm.bankslipgenerator.infrastructure.interceptor

import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * Rate limiting interceptor using Bucket4j
 * Limits requests per client based on API key or IP
 */
@Component
class RateLimitInterceptor(
    private val logger: Logger
) : HandlerInterceptor {

    private val buckets = ConcurrentHashMap<String, Bucket>()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val clientId = extractClientId(request)
        val bucket = buckets.computeIfAbsent(clientId) { createBucket() }

        val probe = bucket.tryConsumeAndReturnRemaining(1)

        if (probe.isConsumed) {
            response.addHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())
            return true
        }

        // Rate limit exceeded
        val waitForRefill = probe.nanosToWaitForRefill / 1_000_000_000

        logger.warn(
            "Rate limit exceeded",
            "clientId" to clientId,
            "endpoint" to request.requestURI,
            "retryAfter" to waitForRefill
        )

        response.status = 429 // Too Many Requests
        response.contentType = "application/json"
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", waitForRefill.toString())
        response.addHeader("Retry-After", waitForRefill.toString())

        response.writer.write(
            """
            {
                "error": "rate-limit-exceeded",
                "message": "Too many requests. Please retry after $waitForRefill seconds.",
                "retry-after-seconds": $waitForRefill
            }
            """.trimIndent()
        )

        return false
    }

    private fun extractClientId(request: HttpServletRequest): String {
        // Try API key first
        request.getHeader("X-API-Key")?.let { return "api-key:$it" }

        // Try client-id header
        request.getHeader("X-Client-Id")?.let { return "client:$it" }

        // Fall back to IP address
        val ip = request.getHeader("X-Forwarded-For")
            ?: request.getHeader("X-Real-IP")
            ?: request.remoteAddr

        return "ip:$ip"
    }

    private fun createBucket(): Bucket {
        // 100 requests per minute with 10 tokens refilled every 6 seconds
        val refill = Refill.intervally(10, Duration.ofSeconds(6))
        val bandwidth = Bandwidth.classic(100, refill)

        return Bucket.builder()
            .addLimit(bandwidth)
            .build()
    }

    /**
     * Create bucket with custom limits
     */
    fun createBucketWithLimits(capacity: Long, refillTokens: Long, refillPeriod: Duration): Bucket {
        val refill = Refill.intervally(refillTokens, refillPeriod)
        val bandwidth = Bandwidth.classic(capacity, refill)

        return Bucket.builder()
            .addLimit(bandwidth)
            .build()
    }
}

