package br.com.misterstorm.bankslipgenerator.infrastructure.config

import br.com.misterstorm.bankslipgenerator.infrastructure.interceptor.IdempotencyInterceptor
import br.com.misterstorm.bankslipgenerator.infrastructure.interceptor.RateLimitInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC configuration for interceptors
 */
@Configuration
class WebMvcConfig(
    private val idempotencyInterceptor: IdempotencyInterceptor,
    private val rateLimitInterceptor: RateLimitInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        // Rate limiting - applies to all API endpoints
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/**")
            .order(1)

        // Idempotency - applies only to POST/PUT/PATCH endpoints
        registry.addInterceptor(idempotencyInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/*/test") // Exclude test endpoints
            .order(2)
    }
}

