package com.pt.ordersystem.ordersystem.storage

import com.pt.ordersystem.ordersystem.config.ApplicationConfig
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
import com.pt.ordersystem.ordersystem.storage.models.PreSignedUploadUrlResult
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.*

@Service
class S3StorageService(
  private val config: ApplicationConfig
) {
  companion object {
    // Maximum file upload size in MB
    const val MAX_FILE_SIZE_MB = 5
    const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024L * 1024L
    
    // Allowed image types: content type -> allowed extensions
    private val ALLOWED_IMAGE_TYPES = mapOf(
      "image/jpeg" to setOf("jpg", "jpeg"),
      "image/jpg" to setOf("jpg", "jpeg"),
      "image/png" to setOf("png"),
      "image/webp" to setOf("webp")
    )
    private val ALLOWED_EXTENSIONS_LIST = listOf("jpg", "jpeg", "png", "webp")
  }
  
  private lateinit var s3Client: S3Client
  private lateinit var s3Presigner: S3Presigner
  private val s3 = config.s3

  @PostConstruct
  fun init() {
    val credentialsProvider = DefaultCredentialsProvider.create()
    val region = Region.of(s3.region)
    
    // Use default credentials provider (IAM role, environment variables, or AWS credentials file)
    // This will work with IAM roles in ECS or AWS credentials locally
    s3Client = S3Client.builder()
      .region(region)
      .credentialsProvider(credentialsProvider)
      .build()
    
    // Initialize presigner for generating presigned URLs
    s3Presigner = S3Presigner.builder()
      .region(region)
      .credentialsProvider(credentialsProvider)
      .build()
  }

  @PreDestroy
  fun cleanup() {
    if (::s3Presigner.isInitialized) {
      s3Presigner.close()
    }
    if (::s3Client.isInitialized) {
      s3Client.close()
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

  fun validateImageMetadata(imageMetadata: ImageMetadata) {
    // Validate file name
    if (imageMetadata.fileName.isBlank()) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "File name cannot be empty",
        technicalMessage = "File name is blank",
        severity = SeverityLevel.WARN
      )
    }

    // Validate file size
    if (imageMetadata.fileSizeBytes <= 0 || imageMetadata.fileSizeBytes > MAX_FILE_SIZE_BYTES) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "File size must be between 1 byte and ${MAX_FILE_SIZE_MB}MB",
        technicalMessage = "Invalid file size: ${imageMetadata.fileSizeBytes} bytes (valid range: 1-$MAX_FILE_SIZE_BYTES bytes)",
        severity = SeverityLevel.WARN
      )
    }

    // Validate MIME type and file extension match
    val normalizedContentType = imageMetadata.contentType.lowercase()
    val extension = imageMetadata.fileName.substringAfterLast(".", "").lowercase()
    val allowedExtensions = ALLOWED_IMAGE_TYPES[normalizedContentType]
    
    if (allowedExtensions == null || !allowedExtensions.contains(extension)) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Only $ALLOWED_EXTENSIONS_LIST images are allowed",
        technicalMessage = "Invalid MIME type: ${imageMetadata.contentType} or extension: $extension",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun generatePreSignedUploadUrl(
    basePath: String,
    imageMetadata: ImageMetadata
  ): PreSignedUploadUrlResult {
    try {
      // Validate image metadata (safety check)
      validateImageMetadata(imageMetadata)

      // Build S3 key
      val s3Key = generateKey(basePath, imageMetadata.fileName)

      // Build PutObjectRequest
      val putObjectRequest = PutObjectRequest.builder()
        .bucket(s3.bucketName)
        .key(s3Key)
        .contentType(imageMetadata.contentType)
        .contentMD5(imageMetadata.fileMd5Base64)
        .build()

      // Build preSignRequest and get URL
      val preSignedUrl = s3Presigner.presignPutObject(
        PutObjectPresignRequest.builder()
          .signatureDuration(Duration.ofMinutes(5))
          .putObjectRequest(putObjectRequest)
          .build()
      )

      // Return URL and S3 key
      return PreSignedUploadUrlResult(
        preSignedUrl = preSignedUrl.url().toString(),
        s3Key = s3Key
      )
    } catch (e: ServiceException) {
      throw e
    } catch (e: Exception) {
      throw ServiceException(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        userMessage = "Failed to generate upload URL",
        technicalMessage = "Error generating presigned URL: ${e.message}",
        severity = SeverityLevel.ERROR
      )
    }
  }
}
