package br.com.misterstorm.bankslipgenerator.infrastructure.logging

import org.slf4j.LoggerFactory
import org.slf4j.MDC

/**
 * Structured logger implementation using SLF4J with MDC for structured context
 */
class StructuredLogger(private val name: String) : Logger {

    private val slf4jLogger = LoggerFactory.getLogger(name)

    override fun debug(message: String, vararg context: Pair<String, Any>) {
        if (slf4jLogger.isDebugEnabled) {
            withContext(context) {
                slf4jLogger.debug(message)
            }
        }
    }

    override fun info(message: String, vararg context: Pair<String, Any>) {
        if (slf4jLogger.isInfoEnabled) {
            withContext(context) {
                slf4jLogger.info(message)
            }
        }
    }

    override fun warn(message: String, vararg context: Pair<String, Any>) {
        if (slf4jLogger.isWarnEnabled) {
            withContext(context) {
                slf4jLogger.warn(message)
            }
        }
    }

    override fun error(message: String, vararg context: Pair<String, Any>) {
        if (slf4jLogger.isErrorEnabled) {
            withContext(context) {
                slf4jLogger.error(message)
            }
        }
    }

    override fun error(message: String, throwable: Throwable, vararg context: Pair<String, Any>) {
        if (slf4jLogger.isErrorEnabled) {
            withContext(context) {
                slf4jLogger.error(message, throwable)
            }
        }
    }

    private inline fun <T> withContext(context: Array<out Pair<String, Any>>, block: () -> T): T {
        val originalContext = context.associate { it.first to MDC.get(it.first) }
        try {
            context.forEach { (key, value) ->
                MDC.put(key, value.toString())
            }
            return block()
        } finally {
            context.forEach { (key, _) ->
                originalContext[key]?.let { MDC.put(key, it) } ?: MDC.remove(key)
            }
        }
    }

    companion object {
        fun forClass(clazz: Class<*>): Logger = StructuredLogger(clazz.name)

        inline fun <reified T> forClass(): Logger = forClass(T::class.java)
    }
}

