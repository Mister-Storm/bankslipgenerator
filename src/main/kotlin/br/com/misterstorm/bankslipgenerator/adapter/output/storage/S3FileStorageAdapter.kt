package br.com.misterstorm.bankslipgenerator.adapter.output.storage

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.port.FileStorageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.ByteArrayOutputStream

/**
 * S3 implementation of FileStorageService
 */
@Component
class S3FileStorageAdapter(
    private val s3Client: S3Client,
    @Value("\${app.storage.s3.bucket}") private val bucketName: String,
    @Value("\${app.storage.s3.region}") private val region: String
) : FileStorageService {

    override suspend fun upload(
        fileName: String,
        content: ByteArray,
        contentType: String,
        metadata: Map<String, String>
    ): Either<DomainError, String> {
        return try {
            val putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .metadata(metadata)
                .build()

            s3Client.putObject(putRequest, RequestBody.fromBytes(content))

            val url = "https://$bucketName.s3.$region.amazonaws.com/$fileName"
            url.right()
        } catch (e: S3Exception) {
            DomainError.FileStorageFailed(
                message = "Failed to upload file to S3: ${e.message}",
                details = mapOf("fileName" to fileName, "error" to e.awsErrorDetails().errorMessage())
            ).left()
        } catch (e: Exception) {
            DomainError.FileStorageFailed(
                message = "Failed to upload file: ${e.message}",
                details = mapOf("fileName" to fileName)
            ).left()
        }
    }

    override suspend fun download(fileUrl: String): Either<DomainError, ByteArray> {
        return try {
            val key = extractKeyFromUrl(fileUrl)

            val getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()

            val responseBytes = s3Client.getObjectAsBytes(getRequest)
            responseBytes.asByteArray().right()
        } catch (e: S3Exception) {
            DomainError.FileStorageFailed(
                message = "Failed to download file from S3: ${e.message}",
                details = mapOf("fileUrl" to fileUrl, "error" to e.awsErrorDetails().errorMessage())
            ).left()
        } catch (e: Exception) {
            DomainError.FileStorageFailed(
                message = "Failed to download file: ${e.message}",
                details = mapOf("fileUrl" to fileUrl)
            ).left()
        }
    }

    override suspend fun delete(fileUrl: String): Either<DomainError, Unit> {
        return try {
            val key = extractKeyFromUrl(fileUrl)

            val deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()

            s3Client.deleteObject(deleteRequest)
            Unit.right()
        } catch (e: S3Exception) {
            DomainError.FileStorageFailed(
                message = "Failed to delete file from S3: ${e.message}",
                details = mapOf("fileUrl" to fileUrl, "error" to e.awsErrorDetails().errorMessage())
            ).left()
        } catch (e: Exception) {
            DomainError.FileStorageFailed(
                message = "Failed to delete file: ${e.message}",
                details = mapOf("fileUrl" to fileUrl)
            ).left()
        }
    }

    private fun extractKeyFromUrl(url: String): String {
        // Extract key from S3 URL format: https://bucket.s3.region.amazonaws.com/key
        return url.substringAfterLast("/")
    }
}

