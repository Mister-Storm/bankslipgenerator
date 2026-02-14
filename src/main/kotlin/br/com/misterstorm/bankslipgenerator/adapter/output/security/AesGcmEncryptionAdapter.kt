package br.com.misterstorm.bankslipgenerator.adapter.output.security

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.port.EncryptionService
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-GCM encryption implementation
 * Uses AES-256-GCM for encryption with authentication
 */
@Component
class AesGcmEncryptionAdapter(
    @Value("\${app.security.encryption.key:}") private val base64Key: String,
    private val logger: Logger
) : EncryptionService {

    private val algorithm = "AES/GCM/NoPadding"
    private val gcmTagLength = 128
    private val ivLength = 12 // 96 bits recommended for GCM

    private val secretKey: SecretKey by lazy {
        if (base64Key.isBlank()) {
            logger.warn("No encryption key configured, generating temporary key")
            generateKey()
        } else {
            val decodedKey = Base64.getDecoder().decode(base64Key)
            SecretKeySpec(decodedKey, "AES")
        }
    }

    override suspend fun encrypt(plainText: String): Either<DomainError, String> {
        return try {
            val cipher = Cipher.getInstance(algorithm)
            val iv = ByteArray(ivLength)
            SecureRandom().nextBytes(iv)

            val gcmSpec = GCMParameterSpec(gcmTagLength, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // Combine IV and encrypted data
            val combined = iv + encryptedBytes
            Base64.getEncoder().encodeToString(combined).right()

        } catch (e: Exception) {
            logger.error("Encryption failed", e)
            DomainError.UnexpectedError("Encryption failed: ${e.message}", e).left()
        }
    }

    override suspend fun decrypt(encryptedText: String): Either<DomainError, String> {
        return try {
            val combined = Base64.getDecoder().decode(encryptedText)

            // Extract IV and encrypted data
            val iv = combined.sliceArray(0 until ivLength)
            val encryptedBytes = combined.sliceArray(ivLength until combined.size)

            val cipher = Cipher.getInstance(algorithm)
            val gcmSpec = GCMParameterSpec(gcmTagLength, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8).right()

        } catch (e: Exception) {
            logger.error("Decryption failed", e)
            DomainError.UnexpectedError("Decryption failed: ${e.message}", e).left()
        }
    }

    override suspend fun hash(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    override suspend fun verifyHash(text: String, hash: String): Boolean {
        val computedHash = hash(text)
        return computedHash == hash
    }

    private fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256, SecureRandom())
        return keyGen.generateKey()
    }

    companion object {
        /**
         * Utility function to generate a new encryption key
         * Run this once and store the key securely
         */
        fun generateNewKey(): String {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256, SecureRandom())
            val key = keyGen.generateKey()
            return Base64.getEncoder().encodeToString(key.encoded)
        }
    }
}

