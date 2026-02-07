package br.com.misterstorm.bankslipgenerator.domain.port

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError

/**
 * Port for encryption/decryption of sensitive data
 */
interface EncryptionService {
    suspend fun encrypt(plainText: String): Either<DomainError, String>
    suspend fun decrypt(encryptedText: String): Either<DomainError, String>
    suspend fun hash(text: String): String
    suspend fun verifyHash(text: String, hash: String): Boolean
}

