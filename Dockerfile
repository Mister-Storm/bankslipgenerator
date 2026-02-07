# Multi-stage Dockerfile for Bank Slip Generator
# Supports production and sandbox profiles

# Stage 1: Builder
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build application (skip tests for Docker build, tests run in CI)
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Create log directory
RUN mkdir -p /var/log/bankslipgenerator && \
    chown -R spring:spring /var/log/bankslipgenerator

# Copy the jar from builder
COPY --from=builder /app/build/libs/*.jar bankslipgenerator.jar

# Change ownership
RUN chown spring:spring bankslipgenerator.jar

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Default JVM options (can be overridden)
ENV JAVA_OPTS="-Xmx1g -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Default profile (can be overridden via environment variable)
ENV SPRING_PROFILES_ACTIVE=production

# Entry point
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar bankslipgenerator.jar"]


