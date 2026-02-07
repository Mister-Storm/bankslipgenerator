plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
    id("org.sonarqube") version "4.4.1.3373"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
    // PITest temporarily disabled due to Gradle 9.3 compatibility issues
    // Will be re-enabled when compatible version is available
}

group = "br.com.misterstorm"
version = "0.0.1-SNAPSHOT"
description = "bankslipgenerator"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

// Configure integration test source set
sourceSets {
    create("intTest") {
        kotlin {
            srcDir("src/intTest/kotlin")
            compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
            runtimeClasspath += output + compileClasspath
        }
        resources {
            srcDir("src/intTest/resources")
        }
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.9.0")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Arrow (Functional Programming)
    implementation("io.arrow-kt:arrow-core:1.2.4")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.4")

    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("com.zaxxer:HikariCP") // Connection pooling (already included but explicit)

    // Rate Limiting
    implementation("com.bucket4j:bucket4j-core:8.10.1")
    implementation("com.bucket4j:bucket4j-redis:8.10.1")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // AWS SDK for S3
    implementation("software.amazon.awssdk:s3:2.28.24")
    implementation("software.amazon.awssdk:sts:2.28.24")

    // PDF Generation (Flying Saucer + Thymeleaf)
    implementation("org.xhtmlrenderer:flying-saucer-pdf:9.11.1")
    implementation("org.xhtmlrenderer:flying-saucer-core:9.11.1")

    // Barcode Generation
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")

    // Easy Rules
    implementation("org.jeasy:easy-rules-core:4.1.0")
    implementation("org.jeasy:easy-rules-support:4.1.0")

    // Micrometer for Metrics
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // Resilience4j for Circuit Breaker and Retry
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.2.0")
    implementation("io.github.resilience4j:resilience4j-retry:2.2.0")
    implementation("io.github.resilience4j:resilience4j-ratelimiter:2.2.0")
    implementation("io.github.resilience4j:resilience4j-kotlin:2.2.0")

    // WebFlux for reactive HTTP client
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // AWS KMS for encryption key management
    implementation("software.amazon.awssdk:kms:2.28.24")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test Dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Integration Test Dependencies
    intTestImplementation("org.springframework.boot:spring-boot-testcontainers")
    intTestImplementation("org.testcontainers:testcontainers:1.20.4")
    intTestImplementation("org.testcontainers:postgresql:1.20.4")
    intTestImplementation("org.testcontainers:localstack:1.20.4")
    intTestImplementation("io.rest-assured:rest-assured:5.5.0")
    intTestImplementation("io.rest-assured:kotlin-extensions:5.5.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Register integration test task
val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath

    shouldRunAfter(tasks.test)

    useJUnitPlatform()
}

tasks.check {
    dependsOn(integrationTest)
}

// JaCoCo configuration
jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.65".toBigDecimal() // 65% minimum coverage (current state)
            }
        }
    }
}

// SonarQube configuration
sonarqube {
    properties {
        property("sonar.projectKey", "bankslipgenerator")
        property("sonar.projectName", "Bank Slip Generator")
        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "http://localhost:9000")
        property("sonar.login", System.getenv("SONAR_TOKEN") ?: "")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.junit.reportPaths", "build/test-results/test")
        property("sonar.gradle.skipCompile", "true")
    }
}

// PITest configuration for mutation testing
// Temporarily disabled due to Gradle 9.3 compatibility issues
// TODO: Re-enable when pitest-gradle plugin supports Gradle 9.3+
/*
pitest {
    targetClasses.set(setOf("br.com.misterstorm.bankslipgenerator.*"))
    targetTests.set(setOf("br.com.misterstorm.bankslipgenerator.*"))
    excludedClasses.set(setOf(
        "br.com.misterstorm.bankslipgenerator.*Config",
        "br.com.misterstorm.bankslipgenerator.*Application*",
        "br.com.misterstorm.bankslipgenerator.*Sql",
        "br.com.misterstorm.bankslipgenerator.*Mapper",
        "br.com.misterstorm.bankslipgenerator.adapter.output.persistence.sql.*",
        "br.com.misterstorm.bankslipgenerator.adapter.output.persistence.mapper.*"
    ))
    threads.set(Runtime.getRuntime().availableProcessors())
    outputFormats.set(setOf("HTML", "XML"))
    timestampedReports.set(false)
    mutators.set(setOf("STRONGER"))
    timeoutConstInMillis.set(10000)
    verbose.set(false)
    junit5PluginVersion.set("1.2.1")
    reportDir.set(file("${project.buildDir}/reports/pitest"))
}
*/

// Kover configuration for code coverage
kover {
}

koverReport {
    filters {
        excludes {
            classes(
                "*Config",
                "*Application*",
                "*Sql",
                "*Mapper",
                "br.com.misterstorm.bankslipgenerator.adapter.output.persistence.sql.*",
                "br.com.misterstorm.bankslipgenerator.adapter.output.persistence.mapper.*",
                "br.com.misterstorm.bankslipgenerator.infrastructure.config.*"
            )
        }
    }

    defaults {
        html {
            onCheck = true
        }
        xml {
            onCheck = true
        }
    }

    verify {
        rule {
            minBound(65)
        }
    }
}

tasks.test {
    finalizedBy(tasks.named("koverHtmlReport"), tasks.named("koverXmlReport"))
}


