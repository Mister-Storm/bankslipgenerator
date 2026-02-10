package br.com.misterstorm.bankslipgenerator.infrastructure.config

import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.services.s3.S3Client

/**
 * Test configuration that provides mock beans for external services
 */
@TestConfiguration
@Profile("test")
class TestInfrastructureConfig {

    @Bean
    @Primary
    fun s3Client(): S3Client = mockk(relaxed = true)
}

