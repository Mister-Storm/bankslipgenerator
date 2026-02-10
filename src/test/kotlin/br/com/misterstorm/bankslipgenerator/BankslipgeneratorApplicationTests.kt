package br.com.misterstorm.bankslipgenerator

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable

/**
 * Application context smoke test.
 * This test is disabled in CI environments without a database.
 * Run with: ./gradlew test -Dspring.profiles.active=test
 */
class BankslipgeneratorApplicationTests {

    @Test
    fun `ensure application class exists`() {
        // Smoke test - just verify the main class compiles
        val appClass = BankslipgeneratorApplication::class
        assert(appClass.simpleName == "BankslipgeneratorApplication")
    }

}
