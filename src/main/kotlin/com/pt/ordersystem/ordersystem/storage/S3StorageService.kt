package com.pt.ordersystem.ordersystem.storage

import com.pt.ordersystem.ordersystem.config.ApplicationConfig
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import jakarta.annotation.PostConstruct
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.util.*

@Service
class S3StorageService(
  private val config: ApplicationConfig
) {
  private lateinit var s3Client: S3Client
  private val s3 = config.s3

  @PostConstruct
  fun init() {
    // Use default credentials provider (IAM role, environment variables, or AWS credentials file)
    // This will work with IAM roles in ECS or AWS credentials locally
    s3Client = S3Client.builder()
      .region(Region.of(s3.region))
      .credentialsProvider(DefaultCredentialsProvider.create())
      .build()
  }

  fun uploadFile(file: MultipartFile, key: String): String {
    try {
      val putObjectRequest = PutObjectRequest.builder()
        .bucket(s3.bucketName)
        .key(key)
        .contentType(file.contentType ?: "application/octet-stream")
        .build()

      val requestBody = RequestBody.fromInputStream(file.inputStream, file.size)

      s3Client.putObject(putObjectRequest, requestBody)

      // Return the public URL
      return getPublicUrl(key) ?: throw ServiceException(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        userMessage = "Failed to upload file to storage",
        technicalMessage = "Unable to generate public URL",
        severity = SeverityLevel.ERROR
      )
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
      s3Client.deleteObject { it.bucket(s3.bucketName).key(key) }
    } catch (e: Exception) {
      // Log but don't throw - file might not exist
      println("Warning: Failed to delete file from S3: $key - ${e.message}")
    }
  }

  fun generateKey(basePath: String, fileName: String): String {
    val sanitizedFileName = fileName.replace(" ", "_").replace("[^a-zA-Z0-9._-]".toRegex(), "")
    val uuid = UUID.randomUUID().toString()
    return "$basePath/${uuid}_$sanitizedFileName"
  }

  fun getPublicUrl(s3Key: String?): String? {
    if (s3Key == null) return null
    
    // CloudFront domain is required (best practice: private bucket + CloudFront)
    require(s3.publicDomain.isNotBlank()) {
      "S3 public domain (CloudFront) must be configured. " +
      "Set config.s3.publicDomain in your configuration."
    }
    
    // Ensure domain ends with "/" for proper URL construction
    val publicDomain = s3.publicDomain.let {
      if (it.endsWith("/")) it else "$it/"
    }
    
    return "${publicDomain}${s3Key}"
  }
}

