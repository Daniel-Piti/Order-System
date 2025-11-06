package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.brand.models.BrandDto
import com.pt.ordersystem.ordersystem.domains.brand.models.CreateBrandRequest
import com.pt.ordersystem.ordersystem.domains.brand.models.UpdateBrandRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Brands", description = "User brand management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/brands")
@PreAuthorize(AUTH_USER)
class BrandController(
    private val brandService: BrandService
) {

    @GetMapping
    fun getAllBrands(
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<List<BrandDto>> {
        return ResponseEntity.ok(brandService.getUserBrands(user.userId))
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createBrand(
        @RequestParam("name") name: String,
        @RequestPart(value = "image", required = false) image: MultipartFile?,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<Long> {
        val request = CreateBrandRequest(name = name)
        val brandId = brandService.createBrand(user.userId, request, image)
        return ResponseEntity.status(HttpStatus.CREATED).body(brandId)
    }

    @PutMapping("/{brandId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateBrand(
        @PathVariable brandId: Long,
        @RequestParam("name") name: String,
        @RequestPart(value = "image", required = false) image: MultipartFile?,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<Long> {
        val request = UpdateBrandRequest(name = name)
        val updatedBrandId = brandService.updateBrand(user.userId, brandId, request, image)
        return ResponseEntity.ok(updatedBrandId)
    }

    @DeleteMapping("/{brandId}")
    fun deleteBrand(
        @PathVariable brandId: Long,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<String> {
        brandService.deleteBrand(user.userId, brandId)
        return ResponseEntity.ok("Brand deleted successfully")
    }
}
