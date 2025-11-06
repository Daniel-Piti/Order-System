package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.brand.models.BrandDto
import com.pt.ordersystem.ordersystem.domains.brand.models.CreateBrandRequest
import com.pt.ordersystem.ordersystem.domains.brand.models.UpdateBrandRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

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

    @PostMapping
    fun createBrand(
        @RequestBody request: CreateBrandRequest,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<Long> {
        val brandId = brandService.createBrand(user.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(brandId)
    }

    @PutMapping("/{brandId}")
    fun updateBrand(
        @PathVariable brandId: Long,
        @RequestBody request: UpdateBrandRequest,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<Long> {
        val updatedBrandId = brandService.updateBrand(user.userId, brandId, request)
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
