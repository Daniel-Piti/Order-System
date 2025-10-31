package com.pt.ordersystem.ordersystem.storage

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "File Upload", description = "File upload API for JPEG and PNG images")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/files")
@PreAuthorize(AUTH_USER)
class FileUploadController(
  private val r2StorageService: R2StorageService
) {

  @Operation(
    summary = "Upload an image file",
    description = "Upload an image file to cloud storage. Supports JPEG and PNG only. Max size configured in application settings."
  )
  @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  fun uploadFile(
    @RequestParam("file") file: MultipartFile,
    @RequestParam(value = "folder", defaultValue = "uploads") folder: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<FileUploadResponse> {
    
    // Upload file to R2
    val fileUrl = r2StorageService.uploadFile(file, folder)
    
    // Return response with file URL
    val response = FileUploadResponse(
      url = fileUrl,
      filename = file.originalFilename ?: "unknown",
      size = file.size,
      contentType = file.contentType ?: "unknown"
    )
    
    return ResponseEntity.status(HttpStatus.CREATED).body(response)
  }

  @Operation(
    summary = "Delete a file",
    description = "Delete a file from cloud storage by its URL"
  )
  @DeleteMapping
  fun deleteFile(
    @RequestParam("url") fileUrl: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<Map<String, String>> {
    
    r2StorageService.deleteFile(fileUrl)
    
    return ResponseEntity.ok(mapOf("message" to "File deleted successfully"))
  }
}

data class FileUploadResponse(
  val url: String,
  val filename: String,
  val size: Long,
  val contentType: String
)


