package br.com.misterstorm.bankslipgenerator.infrastructure.config

import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.CreateBankSlipUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.DeleteBankSlipUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.GenerateBankSlipPdfUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.GetBankSlipUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.PayBankSlipUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.RegisterBankSlipOnlineUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankslip.RegisterBankSlipUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.bankconfiguration.CreateBankConfigurationUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.cnab.GenerateRemittanceFileUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.cnab.ProcessReturnFileUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.webhook.CreateWebhookConfigUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.webhook.DeleteWebhookConfigUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.webhook.GetWebhookConfigUseCase
import br.com.misterstorm.bankslipgenerator.application.usecase.webhook.TestWebhookUseCase
import br.com.misterstorm.bankslipgenerator.adapter.output.webhook.WebhookDeliveryService
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEventPublisher
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.domain.port.BankOnlineRegistrationService
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipRepository
import br.com.misterstorm.bankslipgenerator.domain.port.CnabFileRepository
import br.com.misterstorm.bankslipgenerator.domain.port.CnabService
import br.com.misterstorm.bankslipgenerator.domain.port.FileStorageService
import br.com.misterstorm.bankslipgenerator.domain.port.PdfGeneratorService
import br.com.misterstorm.bankslipgenerator.domain.port.WebhookRepository
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
    fun logger(): Logger = StructuredLogger("BankSlipGenerator")

    // BankSlip Use Cases
    @Bean
    fun createBankSlipUseCase(
        bankSlipRepository: BankSlipRepository,
        bankConfigurationRepository: BankConfigurationRepository,
        eventPublisher: DomainEventPublisher,
        logger: Logger
    ): CreateBankSlipUseCase {
        return CreateBankSlipUseCase(bankSlipRepository, bankConfigurationRepository, eventPublisher, logger)
    }

    @Bean
    fun getBankSlipUseCase(
        bankSlipRepository: BankSlipRepository,
        logger: Logger
    ): GetBankSlipUseCase {
        return GetBankSlipUseCase(bankSlipRepository, logger)
    }

    @Bean
    fun deleteBankSlipUseCase(
        bankSlipRepository: BankSlipRepository,
        logger: Logger
    ): DeleteBankSlipUseCase {
        return DeleteBankSlipUseCase(bankSlipRepository, logger)
    }

    @Bean
    fun payBankSlipUseCase(
        bankSlipRepository: BankSlipRepository,
        eventPublisher: DomainEventPublisher,
        logger: Logger
    ): PayBankSlipUseCase {
        return PayBankSlipUseCase(bankSlipRepository, eventPublisher, logger)
    }

    @Bean
    fun registerBankSlipUseCase(
        bankSlipRepository: BankSlipRepository,
        logger: Logger
    ): RegisterBankSlipUseCase {
        return RegisterBankSlipUseCase(bankSlipRepository, logger)
    }

    @Bean
    fun generateBankSlipPdfUseCase(
        bankSlipRepository: BankSlipRepository,
        bankConfigurationRepository: BankConfigurationRepository,
        pdfGeneratorService: PdfGeneratorService,
        fileStorageService: FileStorageService,
        logger: Logger
    ): GenerateBankSlipPdfUseCase {
        return GenerateBankSlipPdfUseCase(
            bankSlipRepository,
            bankConfigurationRepository,
            pdfGeneratorService,
            fileStorageService,
            logger
        )
    }

    // CNAB Use Cases
    @Bean
    fun generateRemittanceFileUseCase(
        bankSlipRepository: BankSlipRepository,
        bankConfigurationRepository: BankConfigurationRepository,
        cnabService: CnabService,
        cnabFileRepository: CnabFileRepository,
        fileStorageService: FileStorageService,
        logger: Logger
    ): GenerateRemittanceFileUseCase {
        return GenerateRemittanceFileUseCase(
            bankSlipRepository,
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
        webhookDeliveryService: WebhookDeliveryService,
        logger: Logger
    ): TestWebhookUseCase {
        return TestWebhookUseCase(webhookRepository, webhookDeliveryService, logger)
    }

    // Online Registration Use Case
    @Bean
    fun registerBankSlipOnlineUseCase(
        bankSlipRepository: BankSlipRepository,
        onlineServices: List<BankOnlineRegistrationService>,
        eventPublisher: DomainEventPublisher,
        logger: Logger
    ): RegisterBankSlipOnlineUseCase {
        return RegisterBankSlipOnlineUseCase(bankSlipRepository, onlineServices, eventPublisher, logger)
    }
}
