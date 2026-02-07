# Bank Slip Generator - Local Development Setup

## Quick Start with Docker Compose

### 1. Start all services
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- LocalStack (S3 mock) on port 4566
- Application on port 8080

### 2. Initialize LocalStack S3 bucket
```bash
# Wait for LocalStack to be ready
sleep 10

# Create S3 bucket
aws --endpoint-url=http://localhost:4566 s3 mb s3://bankslip-files
```

### 3. Access the application
The application will be available at: http://localhost:8080

### 4. Check health
```bash
curl http://localhost:8080/actuator/health
```

### 5. Stop services
```bash
docker-compose down
```

### 6. Stop and remove volumes
```bash
docker-compose down -v
```

## Manual Setup (without Docker)

### Prerequisites
- JDK 24+
- PostgreSQL 15+
- AWS credentials or LocalStack for S3

### 1. Start PostgreSQL
```bash
# Using Docker
docker run -d \
  --name bankslip-postgres \
  -e POSTGRES_DB=bankslipgenerator \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine
```

### 2. Start LocalStack (optional for local S3)
```bash
docker run -d \
  --name bankslip-localstack \
  -e SERVICES=s3 \
  -p 4566:4566 \
  localstack/localstack
```

### 3. Run the application
```bash
./gradlew bootRun
```

## Environment Variables

Create a `.env` file in the project root:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=bankslipgenerator
DB_USERNAME=postgres
DB_PASSWORD=postgres

S3_BUCKET=bankslip-files
S3_REGION=us-east-1

# For LocalStack
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test
AWS_ENDPOINT_URL=http://localhost:4566

# Optional
LOG_LEVEL=DEBUG
SHOW_SQL=true
```

## Testing

### Run unit tests
```bash
./gradlew test
```

### Run integration tests
```bash
./gradlew integrationTest
```

### Run all tests
```bash
./gradlew check
```

## API Examples

### 1. Create a bank configuration
```bash
curl -X POST http://localhost:8080/api/bank-configurations \
  -H "Content-Type: application/json" \
  -H "API-Version: v1" \
  -d '{
    "bankCode": "001",
    "bankName": "Banco do Brasil",
    "layoutConfiguration": {
      "templateId": "default"
    },
    "cnabConfiguration": {
      "version": "CNAB240",
      "headerLayout": "{}",
      "detailLayout": "{}",
      "trailerLayout": "{}"
    },
    "validationRules": []
  }'
```

### 2. Create a bankslip
```bash
curl -X POST http://localhost:8080/api/bankslips \
  -H "Content-Type: application/json" \
  -H "API-Version: v1" \
  -d '{
    "bankCode": "001",
    "amount": 100.00,
    "dueDate": "2026-03-15",
    "payer": {
      "name": "John Doe",
      "documentNumber": "12345678900",
      "address": {
        "street": "Main St",
        "number": "123",
        "neighborhood": "Downtown",
        "city": "São Paulo",
        "state": "SP",
        "zipCode": "01234567"
      }
    },
    "beneficiary": {
      "name": "Company Inc",
      "documentNumber": "12345678000190",
      "address": {
        "street": "Corporate Ave",
        "number": "456",
        "neighborhood": "Business District",
        "city": "São Paulo",
        "state": "SP",
        "zipCode": "01234567"
      },
      "agencyNumber": "1234",
      "accountNumber": "567890",
      "accountDigit": "1"
    },
    "instructions": ["Não receber após vencimento"]
  }'
```

### 3. Get a bankslip
```bash
curl http://localhost:8080/api/bankslips/{id} \
  -H "API-Version: v1"
```

### 4. Generate PDF
```bash
curl http://localhost:8080/api/bankslips/{id}/pdf \
  -H "API-Version: v1"
```

## Troubleshooting

### Database connection issues
Check if PostgreSQL is running:
```bash
docker ps | grep postgres
```

Check logs:
```bash
docker logs bankslip-postgres
```

### LocalStack S3 issues
Verify LocalStack is running:
```bash
docker ps | grep localstack
```

List buckets:
```bash
aws --endpoint-url=http://localhost:4566 s3 ls
```

### Application logs
```bash
docker logs bankslip-app -f
```

## Database Migrations

Flyway migrations run automatically on startup. To check migration status:

```bash
./gradlew flywayInfo
```

To manually run migrations:
```bash
./gradlew flywayMigrate
```

## Monitoring

### Prometheus metrics
http://localhost:8080/actuator/prometheus

### Health check
http://localhost:8080/actuator/health

### Application info
http://localhost:8080/actuator/info

