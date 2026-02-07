package br.com.misterstorm.bankslipgenerator.infrastructure.config

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.templatemode.TemplateMode
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import org.springframework.beans.factory.annotation.Value

/**
 * Spring configuration for infrastructure beans
 */
@Configuration
@EnableAsync
class InfrastructureConfig {

    @Bean
    fun s3Client(@Value("\${app.storage.s3.region}") region: String): S3Client {
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()
    }

    @Bean
    fun templateResolver(): SpringResourceTemplateResolver {
        return SpringResourceTemplateResolver().apply {
            prefix = "classpath:/templates/"
            suffix = ".html"
            templateMode = TemplateMode.HTML
            characterEncoding = "UTF-8"
            isCacheable = true
        }
    }

    @Bean
    fun templateEngine(templateResolver: SpringResourceTemplateResolver): SpringTemplateEngine {
        return SpringTemplateEngine().apply {
            setTemplateResolver(templateResolver)
        }
    }

    // JDBC Row Mappers
    @Bean
    fun bankslipRowMapper(): BankslipRowMapper = BankslipRowMapper()

    @Bean
    fun bankConfigurationRowMapper(): BankConfigurationRowMapper = BankConfigurationRowMapper()

    @Bean
    fun cnabFileRowMapper(): CnabFileRowMapper = CnabFileRowMapper()

    @Bean
    fun idempotencyKeyRowMapper(): IdempotencyKeyRowMapper = IdempotencyKeyRowMapper()

    @Bean
    fun deadLetterQueueRowMapper(): DeadLetterQueueRowMapper = DeadLetterQueueRowMapper()
}

