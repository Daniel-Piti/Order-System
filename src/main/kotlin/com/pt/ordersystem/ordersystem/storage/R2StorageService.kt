package com.pt.ordersystem.ordersystem.storage

import com.pt.ordersystem.ordersystem.config.ConfigProvider
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import jakarta.annotation.PostConstruct
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.util.*

@Service
class R2StorageService(
  private val configProvider: ConfigProvider
) {
  private lateinit var s3Client: S3Client
  private val r2 = configProvider.r2

  @PostConstruct
  fun init() {
    val credentials = AwsBasicCredentials.create(r2.accessKey, r2.secretKey)
    val credentialsProvider = StaticCredentialsProvider.create(credentials)

    // For R2, we use a custom endpoint
    val endpoint = "https://${r2.accountId}.r2.cloudflarestorage.com"

    s3Client = S3Client.builder()
      .region(Region.of(r2.region))
      .credentialsProvider(credentialsProvider)
      .endpointOverride(java.net.URI.create(endpoint))
      .forcePathStyle(true)
      .build()
  }

  fun uploadFile(file: MultipartFile, key: String): String {
    try {
      val putObjectRequest = PutObjectRequest.builder()
        .bucket(r2.bucketName)
        .key(key)
        .contentType(file.contentType ?: "application/octet-stream")
        .build()

      val requestBody = RequestBody.fromInputStream(file.inputStream, file.size)
      s3Client.putObject(putObjectRequest, requestBody)

      // Return the public URL
      return if (r2.publicDomain.endsWith("/")) {
        "${r2.publicDomain}$key"
      } else {
        "${r2.publicDomain}/$key"
      }
    } catch (e: S3Exception) {
      throw ServiceException(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        userMessage = "Failed to upload file to storage",
        technicalMessage = "S3 error: ${e.message}",
        severity = SeverityLevel.ERROR
      )
    } catch (e: Exception) {
      throw ServiceException(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        userMessage = "Failed to upload file",
        technicalMessage = "Upload error: ${e.message}",
        severity = SeverityLevel.ERROR
      )
    }
  }

  fun deleteFile(key: String) {
    try {
      s3Client.deleteObject { it.bucket(r2.bucketName).key(key) }
    } catch (e: Exception) {
      // Log but don't throw - file might not exist
      println("Warning: Failed to delete file from R2: $key - ${e.message}")
    }
  }

  fun generateKey(basePath: String, fileName: String): String {
    val sanitizedFileName = fileName.replace(" ", "_").replace("[^a-zA-Z0-9._-]".toRegex(), "")
    val uuid = UUID.randomUUID().toString()
    return "$basePath/${uuid}_$sanitizedFileName"
  }
}

