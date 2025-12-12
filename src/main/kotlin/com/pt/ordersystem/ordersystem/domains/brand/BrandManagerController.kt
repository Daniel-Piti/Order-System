package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.brand.models.CreateBrandRequest
import com.pt.ordersystem.ordersystem.domains.brand.models.UpdateBrandRequest
import com.pt.ordersystem.ordersystem.domains.brand.models.BrandCreateResponse
import com.pt.ordersystem.ordersystem.domains.brand.models.BrandUpdateResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Brands", description = "Manager brand management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/brands")
@PreAuthorize(AUTH_MANAGER)
class BrandManagerController(
    private val brandService: BrandService
) {

    @PostMapping
    fun createBrand(
        @RequestBody request: CreateBrandRequest,
        @AuthenticationPrincipal manager: AuthUser
    ): ResponseEntity<BrandCreateResponse> {
        val response = brandService.createBrand(manager.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{brandId}")
    fun updateBrand(
        @PathVariable brandId: Long,
        @RequestBody request: UpdateBrandRequest,
        @AuthenticationPrincipal manager: AuthUser
    ): ResponseEntity<BrandUpdateResponse> {
        val response = brandService.updateBrand(manager.id, brandId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{brandId}")
    fun deleteBrand(
        @PathVariable brandId: Long,
        @AuthenticationPrincipal manager: AuthUser
    ): ResponseEntity<String> {
        brandService.deleteBrand(manager.id, brandId)
        return ResponseEntity.ok("Brand deleted successfully")
    }
}

