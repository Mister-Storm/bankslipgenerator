# 🚀 Deployment and Testing Guide

## 📋 Índice

1. [Profiles de Ambiente](#profiles-de-ambiente)
2. [Scripts de Deploy](#scripts-de-deploy)
3. [Docker Compose](#docker-compose)
4. [Testes e Cobertura](#testes-e-cobertura)
5. [Variáveis de Ambiente](#variáveis-de-ambiente)

---

## 🌍 Profiles de Ambiente

O projeto suporta dois profiles principais:

### 🏭 Production
- **Profile:** `production`
- **Uso:** Ambiente de produção
- **Características:**
  - Pool de conexões DB: 50 (máx) / 20 (min)
  - Redis SSL habilitado
  - Logs em nível INFO
  - Rate limit: 1000 req/min
  - Timeout APIs bancárias: 30s
  - Circuit breaker configurado

### 🧪 Sandbox
- **Profile:** `sandbox`
- **Uso:** Ambiente de homologação/testes
- **Características:**
  - Pool de conexões DB: 20 (máx) / 5 (min)
  - Redis sem SSL
  - Logs em nível DEBUG
  - Rate limit: 100 req/min
  - Timeout APIs bancárias: 60s
  - Circuit breaker mais tolerante

---

## 🛠️ Scripts de Deploy

### 1. Deploy Production

```bash
./scripts/deploy-production.sh
```

**Variáveis Obrigatórias:**
```bash
export PROD_DB_HOST=your-db-host
export PROD_DB_USERNAME=your-db-user
export PROD_DB_PASSWORD=your-db-password
export PROD_REDIS_HOST=your-redis-host
export PROD_JWT_ISSUER_URI=your-jwt-issuer
export PROD_JWT_JWK_SET_URI=your-jwk-uri
export PROD_BB_CLIENT_ID=your-bb-client-id
export PROD_BB_CLIENT_SECRET=your-bb-secret
# ... (repetir para outros bancos)
export PROD_ENCRYPTION_KEY=your-32-char-encryption-key
```

**Configurações JVM:**
- Heap máximo: 2GB
- Heap inicial: 512MB
- GC: G1
- Heap dump automático em OOM

### 2. Deploy Sandbox

```bash
./scripts/deploy-sandbox.sh
```

**Variáveis Opcionais (com defaults):**
```bash
export SANDBOX_DB_HOST=sandbox-db.example.com
export SANDBOX_DB_PASSWORD=sandbox-password
# Outras variáveis têm valores padrão para sandbox
```

---

## 🐳 Docker Compose

### Production (apenas app)

```bash
# Definir variáveis no arquivo .env.production
docker-compose -f docker-compose.production.yml up -d
```

**Exemplo `.env.production`:**
```env
PROD_DB_HOST=prod-db.mycompany.com
PROD_DB_USERNAME=bankslip_prod
PROD_DB_PASSWORD=secure-password-here
PROD_REDIS_HOST=prod-redis.mycompany.com
PROD_REDIS_PASSWORD=redis-password
PROD_JWT_ISSUER_URI=https://auth.mycompany.com/realms/bankslip
PROD_JWT_JWK_SET_URI=https://auth.mycompany.com/realms/bankslip/protocol/openid-connect/certs
PROD_BB_CLIENT_ID=bb-client-id
PROD_BB_CLIENT_SECRET=bb-client-secret
# ... outras credenciais
PROD_ENCRYPTION_KEY=your-very-secure-32-character-key
```

### Sandbox (completo com DB, Redis, LocalStack)

```bash
# Subir ambiente completo de sandbox
docker-compose -f docker-compose.sandbox.yml up -d

# Ver logs
docker-compose -f docker-compose.sandbox.yml logs -f app

# Parar ambiente
docker-compose -f docker-compose.sandbox.yml down

# Limpar volumes (CUIDADO: apaga dados)
docker-compose -f docker-compose.sandbox.yml down -v
```

**Serviços incluídos:**
- ✅ PostgreSQL 16
- ✅ Redis 7
- ✅ LocalStack (S3, KMS)
- ✅ Aplicação (profile sandbox)

**Portas expostas:**
- `8080` - API REST
- `5432` - PostgreSQL
- `6379` - Redis
- `4566` - LocalStack

---

## 🧪 Testes e Cobertura

### Executar Todos os Testes e Relatórios

```bash
./scripts/run-test-reports.sh
```

Este script executa:
1. 🧹 Limpeza de relatórios anteriores
2. 🔬 Testes unitários com cobertura (Kover)
3. 🔗 Testes de integração
4. 🧬 Testes de mutação (PITest)
5. 📊 Geração de relatórios HTML e XML

### Comandos Individuais

```bash
# Apenas testes unitários
./gradlew test

# Apenas testes de integração
./gradlew integrationTest

# Cobertura de código
./gradlew koverHtmlReport

# Testes de mutação (demora mais)
./gradlew pitest

# Verificar cobertura mínima
./gradlew koverVerify
```

### Relatórios Gerados

```
build/reports/
├── kover/html/index.html          # Cobertura de código
├── pitest/index.html              # Testes de mutação
├── tests/test/index.html          # Resultados testes unitários
└── tests/integrationTest/index.html  # Resultados testes integração
```

**Ver relatórios no navegador:**
```bash
xdg-open build/reports/kover/html/index.html
xdg-open build/reports/pitest/index.html
```

---

## 🔐 Variáveis de Ambiente

### Database
| Variável | Obrigatória | Default | Descrição |
|----------|-------------|---------|-----------|
| `DB_HOST` | ✅ | - | Host do PostgreSQL |
| `DB_PORT` | ❌ | 5432 | Porta do PostgreSQL |
| `DB_NAME` | ❌ | bankslip_production | Nome do banco |
| `DB_USERNAME` | ✅ | - | Usuário do banco |
| `DB_PASSWORD` | ✅ | - | Senha do banco |

### Redis
| Variável | Obrigatória | Default | Descrição |
|----------|-------------|---------|-----------|
| `REDIS_HOST` | ✅ | - | Host do Redis |
| `REDIS_PORT` | ❌ | 6379 | Porta do Redis |
| `REDIS_PASSWORD` | ❌ | - | Senha do Redis |
| `REDIS_SSL` | ❌ | true (prod) / false (sandbox) | Habilitar SSL |

### JWT/OAuth2
| Variável | Obrigatória | Default | Descrição |
|----------|-------------|---------|-----------|
| `JWT_ISSUER_URI` | ✅ | - | URI do emissor JWT |
| `JWT_JWK_SET_URI` | ✅ | - | URI do JWK Set |

### AWS
| Variável | Obrigatória | Default | Descrição |
|----------|-------------|---------|-----------|
| `AWS_REGION` | ❌ | us-east-1 | Região AWS |
| `S3_BUCKET` | ❌ | bankslip-production | Bucket S3 |
| `AWS_ACCESS_KEY_ID` | ✅* | - | Access Key (*prod) |
| `AWS_SECRET_ACCESS_KEY` | ✅* | - | Secret Key (*prod) |

### Banking APIs

#### Banco do Brasil
| Variável | Obrigatória | Default |
|----------|-------------|---------|
| `BB_API_URL` | ❌ | https://api.bb.com.br/cobrancas/v2 |
| `BB_CLIENT_ID` | ✅ | - |
| `BB_CLIENT_SECRET` | ✅ | - |
| `BB_CERT_PATH` | ❌ | - |

#### Itaú
| Variável | Obrigatória | Default |
|----------|-------------|---------|
| `ITAU_API_URL` | ❌ | https://secure.api.itau/... |
| `ITAU_CLIENT_ID` | ✅ | - |
| `ITAU_CLIENT_SECRET` | ✅ | - |
| `ITAU_CERT_PATH` | ❌ | - |

#### Bradesco
| Variável | Obrigatória | Default |
|----------|-------------|---------|
| `BRADESCO_API_URL` | ❌ | https://proxy.api.prebanco.com.br/v1 |
| `BRADESCO_CLIENT_ID` | ✅ | - |
| `BRADESCO_CLIENT_SECRET` | ✅ | - |
| `BRADESCO_CERT_PATH` | ❌ | - |

#### Caixa Econômica
| Variável | Obrigatória | Default |
|----------|-------------|---------|
| `CAIXA_API_URL` | ❌ | https://api.caixa.gov.br |
| `CAIXA_CLIENT_ID` | ✅ | - |
| `CAIXA_CLIENT_SECRET` | ✅ | - |
| `CAIXA_CERT_PATH` | ❌ | - |

#### Santander
| Variável | Obrigatória | Default |
|----------|-------------|---------|
| `SANTANDER_API_URL` | ❌ | https://trust-open.api.santander.com.br |
| `SANTANDER_CLIENT_ID` | ✅ | - |
| `SANTANDER_CLIENT_SECRET` | ✅ | - |
| `SANTANDER_CERT_PATH` | ❌ | - |

### Security
| Variável | Obrigatória | Default | Descrição |
|----------|-------------|---------|-----------|
| `ENCRYPTION_KEY` | ✅ | - | Chave AES 256-bit (32 chars) |

---

## 📊 Health Check

Verificar saúde da aplicação:

```bash
curl http://localhost:8080/actuator/health
```

Resposta esperada:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

---

## 🚨 Troubleshooting

### Problema: Aplicação não conecta ao banco

**Solução:**
```bash
# Verificar se o PostgreSQL está acessível
docker exec -it bankslipgenerator-postgres-sandbox psql -U bankslip_sandbox -d bankslip_sandbox

# Verificar logs da aplicação
docker logs bankslipgenerator-sandbox
```

### Problema: Testes de mutação muito lentos

**Solução:**
Editar `build.gradle.kts`:
```kotlin
pitest {
    threads.set(2)  // Reduzir threads
    timeoutConstInMillis.set(5000)  // Reduzir timeout
}
```

### Problema: Redis não conecta

**Solução:**
```bash
# Verificar se Redis está rodando
docker exec -it bankslipgenerator-redis-sandbox redis-cli ping

# Deve retornar: PONG
```

---

## 📚 Documentação Adicional

- [TEST_COVERAGE_REPORT.md](TEST_COVERAGE_REPORT.md) - Relatório detalhado de cobertura
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Resumo da implementação
- [README.md](README.md) - Documentação principal do projeto

---

**Última atualização:** 06/02/2026

