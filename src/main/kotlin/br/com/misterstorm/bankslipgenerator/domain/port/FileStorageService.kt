package br.com.misterstorm.bankslipgenerator.domain.port

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError

/**
 * Service port for file storage (S3, GCS, etc.)
 */
interface FileStorageService {
    suspend fun upload(
        fileName: String,
        content: ByteArray,
        contentType: String,
        metadata: Map<String, String> = emptyMap()
    ): Either<DomainError, String> // Returns URL

    suspend fun download(fileUrl: String): Either<DomainError, ByteArray>

    suspend fun delete(fileUrl: String): Either<DomainError, Unit>
}

