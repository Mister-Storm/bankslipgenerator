#!/bin/bash
# Production Deployment Script
# This script deploys the application to production environment

set -e

echo "🚀 Starting Production Deployment..."

# Set environment variables
export SPRING_PROFILES_ACTIVE=production
export JAVA_OPTS="-Xmx2g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/app"

# Database configuration
export DB_HOST=${PROD_DB_HOST:?ERROR: PROD_DB_HOST is required}
export DB_PORT=${PROD_DB_PORT:-5432}
export DB_NAME=${PROD_DB_NAME:-bankslip_production}
export DB_USERNAME=${PROD_DB_USERNAME:?ERROR: PROD_DB_USERNAME is required}
export DB_PASSWORD=${PROD_DB_PASSWORD:?ERROR: PROD_DB_PASSWORD is required}

# Redis configuration
export REDIS_HOST=${PROD_REDIS_HOST:?ERROR: PROD_REDIS_HOST is required}
export REDIS_PORT=${PROD_REDIS_PORT:-6379}
export REDIS_PASSWORD=${PROD_REDIS_PASSWORD:-}
export REDIS_SSL=${PROD_REDIS_SSL:-true}

# JWT configuration
export JWT_ISSUER_URI=${PROD_JWT_ISSUER_URI:?ERROR: PROD_JWT_ISSUER_URI is required}
export JWT_JWK_SET_URI=${PROD_JWT_JWK_SET_URI:?ERROR: PROD_JWT_JWK_SET_URI is required}

# AWS configuration
export AWS_REGION=${PROD_AWS_REGION:-us-east-1}
export S3_BUCKET=${PROD_S3_BUCKET:-bankslip-production}

# Banking API credentials
export BB_API_URL=${PROD_BB_API_URL:-https://api.bb.com.br/cobrancas/v2}
export BB_CLIENT_ID=${PROD_BB_CLIENT_ID:?ERROR: PROD_BB_CLIENT_ID is required}
export BB_CLIENT_SECRET=${PROD_BB_CLIENT_SECRET:?ERROR: PROD_BB_CLIENT_SECRET is required}

export ITAU_API_URL=${PROD_ITAU_API_URL:-https://secure.api.itau/cash_management_ext/1.1.0}
export ITAU_CLIENT_ID=${PROD_ITAU_CLIENT_ID:?ERROR: PROD_ITAU_CLIENT_ID is required}
export ITAU_CLIENT_SECRET=${PROD_ITAU_CLIENT_SECRET:?ERROR: PROD_ITAU_CLIENT_SECRET is required}

export BRADESCO_API_URL=${PROD_BRADESCO_API_URL:-https://proxy.api.prebanco.com.br/v1}
export BRADESCO_CLIENT_ID=${PROD_BRADESCO_CLIENT_ID:?ERROR: PROD_BRADESCO_CLIENT_ID is required}
export BRADESCO_CLIENT_SECRET=${PROD_BRADESCO_CLIENT_SECRET:?ERROR: PROD_BRADESCO_CLIENT_SECRET is required}

export CAIXA_API_URL=${PROD_CAIXA_API_URL:-https://api.caixa.gov.br}
export CAIXA_CLIENT_ID=${PROD_CAIXA_CLIENT_ID:?ERROR: PROD_CAIXA_CLIENT_ID is required}
export CAIXA_CLIENT_SECRET=${PROD_CAIXA_CLIENT_SECRET:?ERROR: PROD_CAIXA_CLIENT_SECRET is required}

export SANTANDER_API_URL=${PROD_SANTANDER_API_URL:-https://trust-open.api.santander.com.br}
export SANTANDER_CLIENT_ID=${PROD_SANTANDER_CLIENT_ID:?ERROR: PROD_SANTANDER_CLIENT_ID is required}
export SANTANDER_CLIENT_SECRET=${PROD_SANTANDER_CLIENT_SECRET:?ERROR: PROD_SANTANDER_CLIENT_SECRET is required}

# Encryption key
export ENCRYPTION_KEY=${PROD_ENCRYPTION_KEY:?ERROR: PROD_ENCRYPTION_KEY is required}

# Application port
export SERVER_PORT=${PROD_SERVER_PORT:-8080}

echo "✅ Environment variables configured"
echo "📦 Application will start on port: ${SERVER_PORT}"
echo "🗄️  Database: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "☁️  AWS Region: ${AWS_REGION}"
echo "🪣  S3 Bucket: ${S3_BUCKET}"

# Run the application
java ${JAVA_OPTS} -jar /app/bankslipgenerator.jar

echo "✅ Production deployment completed"

