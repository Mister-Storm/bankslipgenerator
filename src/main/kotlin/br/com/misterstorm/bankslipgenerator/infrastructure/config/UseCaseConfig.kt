package br.com.misterstorm.bankslipgenerator.infrastructure.config

import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.*
import br.com.misterstorm.bankslipgenerator.application.usecase.bankconfiguration.CreateBankConfigurationUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.cnab.GenerateRemittanceFileUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.cnab.ProcessReturnFileUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.webhook.*
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEventPublisher
import br.com.misterstorm.bankslipgenerator.domain.port.*
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.StructuredLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Spring configuration for application use cases
 */
@Configuration
class UseCaseConfig {

    @Bean
    fun logger(): Logger = StructuredLogger("BankslipGenerator")

    // Bankslip Use Cases
    @Bean
    fun createBankslipUseCase(
        bankslipRepository: BankslipRepository,
        bankConfigurationRepository: BankConfigurationRepository,
        eventPublisher: DomainEventPublisher,
        logger: Logger
    ): CreateBankslipUseCase {
        return CreateBankslipUseCase(bankslipRepository, bankConfigurationRepository, eventPublisher, logger)
    }

    @Bean
    fun getBankslipUseCase(
        bankslipRepository: BankslipRepository,
        logger: Logger
    ): GetBankslipUseCase {
        return GetBankslipUseCase(bankslipRepository, logger)
    }

    @Bean
    fun deleteBankslipUseCase(
        bankslipRepository: BankslipRepository,
        logger: Logger
    ): DeleteBankslipUseCase {
        return DeleteBankslipUseCase(bankslipRepository, logger)
    }

    @Bean
    fun payBankslipUseCase(
        bankslipRepository: BankslipRepository,
        eventPublisher: DomainEventPublisher,
        logger: Logger
    ): PayBankslipUseCase {
        return PayBankslipUseCase(bankslipRepository, eventPublisher, logger)
    }

    @Bean
    fun registerBankslipUseCase(
        bankslipRepository: BankslipRepository,
        logger: Logger
    ): RegisterBankslipUseCase {
        return RegisterBankslipUseCase(bankslipRepository, logger)
    }

    @Bean
    fun generateBankslipPdfUseCase(
        bankslipRepository: BankslipRepository,
        bankConfigurationRepository: BankConfigurationRepository,
        pdfGeneratorService: PdfGeneratorService,
        fileStorageService: FileStorageService,
        logger: Logger
    ): GenerateBankslipPdfUseCase {
        return GenerateBankslipPdfUseCase(
            bankslipRepository,
            bankConfigurationRepository,
            pdfGeneratorService,
            fileStorageService,
            logger
        )
    }

    // CNAB Use Cases
    @Bean
    fun generateRemittanceFileUseCase(
        bankslipRepository: BankslipRepository,
        bankConfigurationRepository: BankConfigurationRepository,
        cnabService: CnabService,
        cnabFileRepository: CnabFileRepository,
        fileStorageService: FileStorageService,
        logger: Logger
    ): GenerateRemittanceFileUseCase {
        return GenerateRemittanceFileUseCase(
            bankslipRepository,
            bankConfigurationRepository,
            cnabService,
            cnabFileRepository,
            fileStorageService,
            logger
        )
    }

    @Bean
    fun processReturnFileUseCase(
        bankConfigurationRepository: BankConfigurationRepository,
        cnabService: CnabService,
        cnabFileRepository: CnabFileRepository,
        logger: Logger
    ): ProcessReturnFileUseCase {
        return ProcessReturnFileUseCase(
            bankConfigurationRepository,
            cnabService,
            cnabFileRepository,
            logger
        )
    }

    // Bank Configuration Use Cases
    @Bean
    fun createBankConfigurationUseCase(
        bankConfigurationRepository: BankConfigurationRepository,
        logger: Logger
    ): CreateBankConfigurationUseCase {
        return CreateBankConfigurationUseCase(bankConfigurationRepository, logger)
    }

    // Webhook Use Cases
    @Bean
    fun createWebhookConfigUseCase(
        webhookRepository: WebhookRepository,
        logger: Logger
    ): CreateWebhookConfigUseCase {
        return CreateWebhookConfigUseCase(webhookRepository, logger)
    }

    @Bean
    fun getWebhookConfigUseCase(
        webhookRepository: WebhookRepository,
        logger: Logger
    ): GetWebhookConfigUseCase {
        return GetWebhookConfigUseCase(webhookRepository, logger)
    }

    @Bean
    fun deleteWebhookConfigUseCase(
        webhookRepository: WebhookRepository,
        logger: Logger
    ): DeleteWebhookConfigUseCase {
        return DeleteWebhookConfigUseCase(webhookRepository, logger)
    }

    @Bean
    fun testWebhookUseCase(
        webhookRepository: WebhookRepository,
        webhookDeliveryService: br.com.misterstorm.bankslipgenerator.adapter.output.webhook.WebhookDeliveryService,
        logger: Logger
    ): TestWebhookUseCase {
        return TestWebhookUseCase(webhookRepository, webhookDeliveryService, logger)
    }

    // Online Registration Use Case
    @Bean
    fun registerBankslipOnlineUseCase(
        bankslipRepository: BankslipRepository,
        onlineServices: List<BankOnlineRegistrationService>,
        eventPublisher: DomainEventPublisher,
        logger: Logger
    ): RegisterBankslipOnlineUseCase {
        return RegisterBankslipOnlineUseCase(bankslipRepository, onlineServices, eventPublisher, logger)
    }
}

