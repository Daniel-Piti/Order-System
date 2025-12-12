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
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
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

  fun generatePreSignedUploadUrl(
    basePath: String,
    imageMetadata: ImageMetadata
  ): PreSignedUploadUrlResult {
    try {
      // 1. Validate file size
      if (imageMetadata.fileSizeBytes <= 0 || imageMetadata.fileSizeBytes > MAX_FILE_SIZE_BYTES) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "File size must be between 1 byte and ${MAX_FILE_SIZE_MB}MB",
          technicalMessage = "Invalid file size: ${imageMetadata.fileSizeBytes} bytes (valid range: 1-$MAX_FILE_SIZE_BYTES bytes)",
          severity = SeverityLevel.WARN
        )
      }

      // 2. Validate MIME type and file extension match
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

      // 5. Build S3 key
      val s3Key = generateKey(basePath, imageMetadata.fileName)

      // 6. Build PutObjectRequest
      val putObjectRequest = PutObjectRequest.builder()
        .bucket(s3.bucketName)
        .key(s3Key)
        .contentType(imageMetadata.contentType)
        .contentMD5(imageMetadata.fileMd5Base64)
        .build()

      // 7. Build preSignRequest and get URL
      val preSignedUrl = s3Presigner.presignPutObject(
        PutObjectPresignRequest.builder()
          .signatureDuration(Duration.ofMinutes(5))
          .putObjectRequest(putObjectRequest)
          .build()
      )

      // 8. Return URL and S3 key
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
