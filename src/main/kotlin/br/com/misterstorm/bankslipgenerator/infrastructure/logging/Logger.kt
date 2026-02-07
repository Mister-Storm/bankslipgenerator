package br.com.misterstorm.bankslipgenerator.infrastructure.logging

/**
 * Logging facade interface supporting structured logging with context
 */
interface Logger {
    fun debug(message: String, vararg context: Pair<String, Any>)
    fun info(message: String, vararg context: Pair<String, Any>)
    fun warn(message: String, vararg context: Pair<String, Any>)
    fun error(message: String, vararg context: Pair<String, Any>)
    fun error(message: String, throwable: Throwable, vararg context: Pair<String, Any>)
}

