package br.com.misterstorm.bankslipgenerator.domain.model

/**
 * Registration type for bankslip
 */
enum class RegistrationType {
    CNAB,           // Registration via CNAB file
    ONLINE_API,     // Registration via REST API
    ONLINE_WEBSERVICE // Registration via SOAP Web Service
}

/**
 * Bank online registration configuration
 */
data class OnlineRegistrationConfig(
    val type: RegistrationType,
    val apiUrl: String? = null,
    val webserviceUrl: String? = null,
    val authUrl: String? = null,
    val authType: AuthType,
    val timeout: Int = 30000, // milliseconds
    val retryAttempts: Int = 3
)

/**
 * Authentication type for bank integration
 */
enum class AuthType {
    BASIC,
    OAUTH2,
    CERTIFICATE,
    API_KEY
}

/**
 * Bank credentials (encrypted)
 */
data class BankCredentials(
    val bankCode: String,
    val credentialType: AuthType,
    val encryptedClientId: String? = null,
    val encryptedClientSecret: String? = null,
    val encryptedApiKey: String? = null,
    val encryptedCertificate: String? = null,
    val encryptedCertificatePassword: String? = null,
    val encryptedUsername: String? = null,
    val encryptedPassword: String? = null
)

