# Bank Slip Generator

A flexible and extensible system for generating bank slips (boletos bancários) for multiple banks in Brazil.

## Features

- ✅ Multi-bank support with customizable configurations
- ✅ Complete bankslip lifecycle management (creation, registration, payment, cancellation)
- ✅ PDF generation with customizable templates using Thymeleaf
- ✅ CNAB 240/400 file generation and processing (remittance and return)
- ✅ Barcode generation using ZXing
- ✅ File storage integration (S3-compatible)
- ✅ RESTful API with header-based versioning
- ✅ JWT/OAuth2 authentication
- ✅ Async processing for CNAB files
- ✅ Metrics and monitoring with Prometheus
- ✅ Soft delete support
- ✅ Validation rules engine with Easy Rules

## Architecture

The project follows **Clean Architecture** combined with **Ports and Adapters (Hexagonal Architecture)**:

```
├── domain/                  # Core business logic
│   ├── model/              # Domain entities
│   ├── port/               # Interfaces (ports)
│   └── error/              # Domain errors
├── application/            # Use cases
│   └── usecase/            # Business use cases
├── adapter/                # Adapters
│   ├── input/             # Inbound adapters (REST controllers)
│   └── output/            # Outbound adapters (repositories, services)
└── infrastructure/         # Infrastructure concerns
    ├── config/            # Spring configurations
    └── logging/           # Logging implementations
```

### Key Design Patterns

- **Use Case Pattern**: All business logic encapsulated in use cases
- **Repository Pattern**: Data access abstraction
- **Functional Programming**: Using Arrow's `Either` for error handling
- **Strategy Pattern**: Pluggable bank configurations and layouts
- **Template Method**: Extensible PDF and CNAB generation

## Technology Stack

- **Language**: Kotlin 2.2.21
- **Framework**: Spring Boot 4.0.2
- **Database**: PostgreSQL with Flyway migrations
- **Security**: OAuth2 Resource Server with JWT
- **PDF Generation**: Thymeleaf + Flying Saucer
- **Barcode**: ZXing (Google)
- **File Storage**: AWS S3 SDK
- **Rules Engine**: Easy Rules
- **Serialization**: kotlinx.serialization
- **Functional Programming**: Arrow-kt
- **Testing**: JUnit 5, Kotlin Test, Testcontainers

## Getting Started

### Prerequisites

- JDK 24+
- PostgreSQL 15+
- AWS credentials (for S3) or compatible storage

### Configuration

Create an `application-local.yml` or set environment variables:

```yaml
DB_HOST=localhost
DB_PORT=5432
DB_NAME=bankslipgenerator
DB_USERNAME=postgres
DB_PASSWORD=postgres

S3_BUCKET=your-bucket-name
S3_REGION=us-east-1

JWT_ISSUER_URI=http://your-auth-server/auth/realms/bankslip
JWT_JWK_SET_URI=http://your-auth-server/auth/realms/bankslip/protocol/openid-connect/certs
```

### Running the Application

```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Run tests
./gradlew test

# Run integration tests
./gradlew integrationTest
```

## API Endpoints

### Bankslips

- `POST /api/bankslips` - Create a new bankslip
- `GET /api/bankslips/{id}` - Get bankslip by ID
- `DELETE /api/bankslips/{id}` - Soft delete a bankslip
- `POST /api/bankslips/{id}/register` - Register bankslip with bank
- `POST /api/bankslips/{id}/pay` - Mark bankslip as paid
- `GET /api/bankslips/{id}/pdf` - Generate PDF

### Bank Configurations

- `POST /api/bank-configurations` - Create bank configuration

### CNAB Files

- `POST /api/cnab/remittance` - Generate remittance file
- `POST /api/cnab/return` - Process return file

## Testing

The project follows the **Classist School** of testing with **fixtures** (stubs) instead of mocks:

```kotlin
@Test
fun `ensure creates bankslip successfully when valid input is provided`() = runTest {
    // Arrange
    val repository = BankslipRepositoryFixture()
    val useCase = CreateBankslipUseCase(repository, logger)
    val input = createTestInput()

    // Act
    val result = useCase(input)

    // Assert
    assertTrue(result.isRight())
}
```

Test naming convention: `ensure <expected behavior> when <condition>`

## Bank Configuration

Banks can be configured dynamically via the API with customizable:

- **Layout Configuration**: PDF templates, logos, CSS, field positions
- **CNAB Configuration**: Header, detail, and trailer layouts for CNAB files
- **Validation Rules**: Custom business rules using Easy Rules engine

Example:

```json
{
  "bankCode": "001",
  "bankName": "Banco do Brasil",
  "layoutConfiguration": {
    "templateId": "bb-default",
    "logoUrl": "https://example.com/bb-logo.png",
    "customCss": "body { font-family: 'Arial'; }"
  },
  "cnabConfiguration": {
    "version": "CNAB240",
    "headerLayout": "{ ... }",
    "detailLayout": "{ ... }",
    "trailerLayout": "{ ... }"
  },
  "validationRules": []
}
```

## Monitoring

The application exposes metrics via Spring Actuator:

- `/actuator/health` - Health check
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

## License

Proprietary - All rights reserved

## Contributing

1. Follow the existing code style
2. Write tests for all new features (AAA pattern, fixtures only)
3. Use `ensure` prefix for test names with backticks
4. All code in English
5. Use functional approach with `Either` for error handling

