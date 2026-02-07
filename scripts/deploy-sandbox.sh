#!/bin/bash
# Sandbox Deployment Script
# This script deploys the application to sandbox/homologation environment

set -e

echo "🧪 Starting Sandbox Deployment..."

# Set environment variables
export SPRING_PROFILES_ACTIVE=sandbox
export JAVA_OPTS="-Xmx1g -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Database configuration
export DB_HOST=${SANDBOX_DB_HOST:-sandbox-db.example.com}
export DB_PORT=${SANDBOX_DB_PORT:-5432}
export DB_NAME=${SANDBOX_DB_NAME:-bankslip_sandbox}
export DB_USERNAME=${SANDBOX_DB_USERNAME:-bankslip_sandbox}
export DB_PASSWORD=${SANDBOX_DB_PASSWORD:?ERROR: SANDBOX_DB_PASSWORD is required}

# Redis configuration
export REDIS_HOST=${SANDBOX_REDIS_HOST:-sandbox-redis.example.com}
export REDIS_PORT=${SANDBOX_REDIS_PORT:-6379}
export REDIS_PASSWORD=${SANDBOX_REDIS_PASSWORD:-}
export REDIS_SSL=${SANDBOX_REDIS_SSL:-false}

# JWT configuration
export JWT_ISSUER_URI=${SANDBOX_JWT_ISSUER_URI:-http://sandbox-keycloak.example.com/auth/realms/bankslip}
export JWT_JWK_SET_URI=${SANDBOX_JWT_JWK_SET_URI:-http://sandbox-keycloak.example.com/auth/realms/bankslip/protocol/openid-connect/certs}

# AWS configuration
export AWS_REGION=${SANDBOX_AWS_REGION:-us-east-1}
export S3_BUCKET=${SANDBOX_S3_BUCKET:-bankslip-sandbox}

# Banking API credentials (sandbox endpoints)
export BB_API_URL=${SANDBOX_BB_API_URL:-https://api.hm.bb.com.br/cobrancas/v2}
export BB_CLIENT_ID=${SANDBOX_BB_CLIENT_ID:-sandbox-client-id}
export BB_CLIENT_SECRET=${SANDBOX_BB_CLIENT_SECRET:-sandbox-secret}

export ITAU_API_URL=${SANDBOX_ITAU_API_URL:-https://sandbox-api.itau.com.br/cash_management_ext/1.1.0}
export ITAU_CLIENT_ID=${SANDBOX_ITAU_CLIENT_ID:-sandbox-client-id}
export ITAU_CLIENT_SECRET=${SANDBOX_ITAU_CLIENT_SECRET:-sandbox-secret}

export BRADESCO_API_URL=${SANDBOX_BRADESCO_API_URL:-https://sandbox.bradesco.com.br/v1}
export BRADESCO_CLIENT_ID=${SANDBOX_BRADESCO_CLIENT_ID:-sandbox-client-id}
export BRADESCO_CLIENT_SECRET=${SANDBOX_BRADESCO_CLIENT_SECRET:-sandbox-secret}

export CAIXA_API_URL=${SANDBOX_CAIXA_API_URL:-https://sandbox.caixa.gov.br}
export CAIXA_CLIENT_ID=${SANDBOX_CAIXA_CLIENT_ID:-sandbox-client-id}
export CAIXA_CLIENT_SECRET=${SANDBOX_CAIXA_CLIENT_SECRET:-sandbox-secret}

export SANTANDER_API_URL=${SANDBOX_SANTANDER_API_URL:-https://sandbox-trust-open.api.santander.com.br}
export SANTANDER_CLIENT_ID=${SANDBOX_SANTANDER_CLIENT_ID:-sandbox-client-id}
export SANTANDER_CLIENT_SECRET=${SANDBOX_SANTANDER_CLIENT_SECRET:-sandbox-secret}

# Encryption key (use a different key for sandbox)
export ENCRYPTION_KEY=${SANDBOX_ENCRYPTION_KEY:-sandbox-encryption-key-32chars!!}

# Application port
export SERVER_PORT=${SANDBOX_SERVER_PORT:-8080}

echo "✅ Environment variables configured"
echo "📦 Application will start on port: ${SERVER_PORT}"
echo "🗄️  Database: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "☁️  AWS Region: ${AWS_REGION}"
echo "🪣  S3 Bucket: ${S3_BUCKET}"

# Run the application
java ${JAVA_OPTS} -jar /app/bankslipgenerator.jar

echo "✅ Sandbox deployment completed"

