package com.pt.ordersystem.ordersystem.domains.productImage

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.productImage.models.ProductImageDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Product Images", description = "Product image management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/products/{productId}/images")
@PreAuthorize(AUTH_USER)
class ProductImageController(
  private val productImageService: ProductImageService
) {

  @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  fun uploadImage(
    @PathVariable productId: String,
    @RequestPart("image") image: MultipartFile,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    val imageDto = productImageService.validateAndUploadImageForProduct(user.userId, productId, image)
    return ResponseEntity.status(HttpStatus.CREATED).body(imageDto.url)
  }

  @DeleteMapping("/{imageId}")
  fun deleteImage(
    @PathVariable productId: String,
    @PathVariable imageId: Long,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    productImageService.deleteImage(user.userId, imageId)
    return ResponseEntity.ok("Image deleted successfully")
  }
}

