package com.pt.ordersystem.ordersystem.storage

import com.pt.ordersystem.ordersystem.config.ConfigProvider
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI
import java.util.*

@Service
class R2StorageService(
  private val configProvider: ConfigProvider
) {

  companion object {
    // Allowed image MIME types
    private val ALLOWED_IMAGE_TYPES = setOf(
      "image/jpeg",
      "image/jpg", 
      "image/png"
    )
    
    // Allowed file extensions
    private val ALLOWED_EXTENSIONS = setOf(
      "jpg", "jpeg", "png"
    )
  }

  private val s3Client: S3Client by lazy {
    val r2Config = configProvider.r2
    
    // Validate configuration
    require(r2Config.accountId.isNotBlank()) { "R2 account ID is not configured" }
    require(r2Config.accessKey.isNotBlank()) { "R2 access key is not configured" }
    require(r2Config.secretKey.isNotBlank()) { "R2 secret key is not configured" }
    require(r2Config.bucketName.isNotBlank()) { "R2 bucket name is not configured" }

    // Build Cloudflare R2 endpoint URL
    val r2Endpoint = "https://${r2Config.accountId}.r2.cloudflarestorage.com"

    // Create AWS credentials
    val credentials = AwsBasicCredentials.create(
      r2Config.accessKey,
      r2Config.secretKey
    )

    // Build S3 client configured for R2
    S3Client.builder()
      .region(Region.of(r2Config.region)) // R2 uses 'auto' but any value works
      .endpointOverride(URI.create(r2Endpoint))
      .credentialsProvider(StaticCredentialsProvider.create(credentials))
      .build()
  }

  /**
   * Upload a file to R2 storage
   * @param file The file to upload
   * @param folder Optional folder path (e.g., "products", "users")
   * @return The public URL of the uploaded file
   */
  fun uploadFile(file: MultipartFile, folder: String = "uploads"): String {
    try {
      // Validate file is not empty
      if (file.isEmpty) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "File is empty",
          technicalMessage = "Attempted to upload empty file",
          severity = SeverityLevel.WARN
        )
      }

      // Validate file size
      val maxSizeInBytes = configProvider.maxFileSizeMb * 1024L * 1024L
      if (file.size > maxSizeInBytes) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "File size exceeds maximum allowed size of ${configProvider.maxFileSizeMb}MB",
          technicalMessage = "File size: ${file.size} bytes, max: $maxSizeInBytes bytes",
          severity = SeverityLevel.WARN
        )
      }

      // Validate file type (must be an image)
      val contentType = file.contentType?.lowercase()
      if (contentType == null || contentType !in ALLOWED_IMAGE_TYPES) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "Only JPEG and PNG images are allowed",
          technicalMessage = "Invalid content type: $contentType",
          severity = SeverityLevel.WARN
        )
      }

      // Validate file extension
      val originalFilename = file.originalFilename ?: "file"
      val extension = originalFilename.substringAfterLast(".", "").lowercase()
      if (extension.isEmpty() || extension !in ALLOWED_EXTENSIONS) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "Invalid file extension. Allowed: jpg, jpeg, png",
          technicalMessage = "Invalid extension: $extension",
          severity = SeverityLevel.WARN
        )
      }

      // Generate unique filename to avoid collisions
      val uniqueFilename = "${UUID.randomUUID()}.$extension"
      
      // Construct S3 key (path in bucket)
      val s3Key = if (folder.isNotBlank()) "$folder/$uniqueFilename" else uniqueFilename

      // Prepare upload request
      val putObjectRequest = PutObjectRequest.builder()
        .bucket(configProvider.r2.bucketName)
        .key(s3Key)
        .contentType(file.contentType ?: "application/octet-stream")
        .build()

      // Upload file to R2
      s3Client.putObject(
        putObjectRequest,
        RequestBody.fromInputStream(file.inputStream, file.size)
      )

      // Return public URL
      // Format: https://<bucket>.<account-id>.r2.cloudflarestorage.com/<key>
      // OR if you have a custom domain configured, use that instead
      return buildPublicUrl(s3Key)

    } catch (e: ServiceException) {
      throw e
    } catch (e: Exception) {
      throw ServiceException(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        userMessage = "Failed to upload file",
        technicalMessage = "R2 upload failed: ${e.message}",
        severity = SeverityLevel.ERROR
      )
    }
  }

  /**
   * Delete a file from R2 storage
   * @param fileUrl The public URL of the file to delete
   */
  fun deleteFile(fileUrl: String) {
    try {
      // Extract S3 key from URL
      val s3Key = extractS3KeyFromUrl(fileUrl)

      val deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(configProvider.r2.bucketName)
        .key(s3Key)
        .build()

      s3Client.deleteObject(deleteObjectRequest)

    } catch (e: Exception) {
      // Log but don't fail - file might already be deleted
      println("[WARN] Failed to delete file from R2: ${e.message}")
    }
  }

  /**
   * Build public URL for uploaded file
   */
  private fun buildPublicUrl(s3Key: String): String {
    val r2Config = configProvider.r2
    
    // Use configured public domain
    require(r2Config.publicDomain.isNotBlank()) { "R2 public domain is not configured" }
    
    val baseUrl = r2Config.publicDomain.trimEnd('/')
    return "$baseUrl/$s3Key"
  }

  /**
   * Extract S3 key from public URL
   */
  private fun extractS3KeyFromUrl(fileUrl: String): String {
    // Example URL: https://pub-xxxxx.r2.dev/products/file.jpg
    // Extract: products/file.jpg
    val uri = URI.create(fileUrl)
    return uri.path.removePrefix("/")
  }
}

