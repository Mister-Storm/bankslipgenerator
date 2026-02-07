# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.1-SNAPSHOT] - 2026-02-06

### Added

#### Domain Layer
- `Bankslip` entity with complete lifecycle management
- `BankConfiguration` entity for dynamic bank configuration
- `CnabFile` entity for CNAB file processing
- Value objects: `Payer`, `Beneficiary`, `Address`, `Discount`, `Fine`, `Interest`
- Enums: `BankslipStatus`, `CnabVersion`, `CnabFileType`, `CnabFileStatus`
- Domain ports: `BankslipRepository`, `BankConfigurationRepository`, `CnabFileRepository`
- Service ports: `PdfGeneratorService`, `CnabService`, `FileStorageService`, `BarcodeGeneratorService`
- Complete `DomainError` sealed class hierarchy

#### Application Layer
- Generic `UseCase<T, U>` abstract class with operator invoke
- Bankslip use cases:
  - `CreateBankslipUseCase` - Create new bankslip
  - `GetBankslipUseCase` - Retrieve bankslip by ID
  - `DeleteBankslipUseCase` - Soft delete bankslip
  - `PayBankslipUseCase` - Register payment
  - `RegisterBankslipUseCase` - Register with bank
  - `GenerateBankslipPdfUseCase` - Generate PDF
- CNAB use cases:
  - `GenerateRemittanceFileUseCase` - Generate remittance file
  - `ProcessReturnFileUseCase` - Process return file
- Bank configuration use cases:
  - `CreateBankConfigurationUseCase` - Create bank configuration

#### Infrastructure Layer - Input Adapters
- `BankslipController` - REST endpoints for bankslip operations
- `BankConfigurationController` - REST endpoints for bank configuration
- `CnabController` - REST endpoints for CNAB operations
- Complete DTOs with mapping functions
- Header-based API versioning support

#### Infrastructure Layer - Output Adapters
- `BankslipRepositoryAdapter` - JPA implementation
- `BankConfigurationRepositoryAdapter` - JPA implementation
- `CnabFileRepositoryAdapter` - JPA implementation
- `S3FileStorageAdapter` - AWS S3 integration
- `ZXingBarcodeGeneratorAdapter` - Barcode generation
- `ThymeleafPdfGeneratorAdapter` - PDF generation
- `CnabServiceAdapter` - CNAB 240/400 processing

#### Persistence
- JPA entities: `BankslipEntity`, `BankConfigurationEntity`, `CnabFileEntity`
- Spring Data repositories for all entities
- Complete entity-domain mapping functions

#### Database
- Flyway migration V1: Create bankslips table
- Flyway migration V2: Create bank_configurations table
- Flyway migration V3: Create cnab_files table
- Indexes for performance optimization
- JSONB support for flexible configuration storage

#### Configuration
- `InfrastructureConfig` - S3, Thymeleaf, async processing setup
- `SecurityConfig` - OAuth2 JWT authentication
- `UseCaseConfig` - Use case bean definitions
- Complete `application.yml` with environment variables
- PostgreSQL datasource configuration
- Flyway integration
- Actuator with Prometheus metrics

#### Templates
- Thymeleaf template for bankslip PDF generation
- Barcode integration in template
- Responsive layout

#### Testing
- Unit tests for `CreateBankslipUseCase` (4 scenarios)
- Unit tests for `PayBankslipUseCase` (4 scenarios)
- Integration tests for `BankslipController` (4 scenarios)
- Test fixtures: `BankslipRepositoryFixture`, `BankConfigurationRepositoryFixture`
- AAA pattern implementation
- Backtick test names with "ensure" prefix

#### Logging
- `Logger` interface for facade pattern
- `StructuredLogger` implementation with SLF4J and MDC
- Structured logging support with varargs of Pair<String, Any>

#### DevOps & Documentation
- Docker Compose setup with PostgreSQL and LocalStack
- Dockerfile for containerized deployment
- `.gitignore` for version control
- `README.md` - Project overview
- `IMPLEMENTATION_SUMMARY.md` - Detailed implementation details
- `LOCAL_SETUP.md` - Development environment guide
- `QUICKSTART.md` - Quick start guide
- `CHANGELOG.md` - This file

### Technical Details

#### Dependencies
- Spring Boot 4.0.2
- Kotlin 2.2.21
- Arrow-kt 1.2.4 for functional programming
- kotlinx.serialization 1.7.3
- PostgreSQL driver
- Flyway for migrations
- AWS S3 SDK 2.28.24
- Thymeleaf + Flying Saucer for PDF
- ZXing 3.5.3 for barcodes
- Easy Rules 4.1.0
- Micrometer + Prometheus for metrics
- Testcontainers 1.20.4 for integration tests
- RestAssured 5.5.0 for API testing

#### Architecture
- Clean Architecture + Ports and Adapters (Hexagonal)
- Functional programming with Either for error handling
- Use Case pattern for business logic
- Repository pattern for data access
- Strategy pattern for extensibility
- Template method for PDF/CNAB generation

#### Testing Strategy
- Classist school (no mocks, only fixtures/stubs)
- AAA pattern (Arrange-Act-Assert)
- Test naming: `ensure <behavior> when <condition>`
- Separate integration test source set

### Features
- ✅ Multi-bank support with dynamic configuration
- ✅ Complete bankslip lifecycle (create, register, pay, delete)
- ✅ PDF generation with customizable templates
- ✅ Barcode generation
- ✅ CNAB 240/400 file generation and processing
- ✅ AWS S3 file storage integration
- ✅ RESTful API with proper HTTP semantics
- ✅ OAuth2 JWT authentication
- ✅ Soft delete support
- ✅ Prometheus metrics
- ✅ Health checks
- ✅ Structured logging
- ✅ Database migrations with Flyway

### Notes
- Initial implementation complete
- All core features implemented
- Production-ready architecture
- Comprehensive test coverage started
- Docker support for local development
- Ready for deployment

---

## [Unreleased]

### Planned Features
- CNAB DSL implementation for configurable layouts
- Easy Rules integration for validation
- Pre-configured banks (Itaú, Bradesco, Santander, etc.)
- Webhook support for status notifications
- Async batch processing with message queue
- Admin UI for configuration management
- OpenAPI/Swagger documentation
- CI/CD pipeline
- Extended test coverage (80%+)
- Performance optimizations
- Caching layer
- Rate limiting
- API gateway integration

---

[0.0.1-SNAPSHOT]: https://github.com/misterstorm/bankslipgenerator/releases/tag/v0.0.1-SNAPSHOT

