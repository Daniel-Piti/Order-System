package com.pt.ordersystem.ordersystem.domains.brand.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.brand.BrandService
import com.pt.ordersystem.ordersystem.domains.brand.models.CreateBrandRequest
import com.pt.ordersystem.ordersystem.domains.brand.models.UpdateBrandRequest
import com.pt.ordersystem.ordersystem.domains.brand.models.CreateBrandResponse
import com.pt.ordersystem.ordersystem.domains.brand.models.UpdateBrandImageResponse
import com.pt.ordersystem.ordersystem.domains.brand.models.UpdateBrandNameResponse
import com.pt.ordersystem.ordersystem.domains.brand.models.toDto
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
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
    ): ResponseEntity<CreateBrandResponse> {
        val normalizedRequest = request.normalize()
        val createBrandResponse = brandService.createBrand(manager.id, normalizedRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createBrandResponse)
    }

    @PutMapping("/{brandId}")
    fun updateBrandName(
        @PathVariable brandId: Long,
        @RequestBody request: UpdateBrandRequest,
        @AuthenticationPrincipal manager: AuthUser
    ): ResponseEntity<UpdateBrandNameResponse> {
        val normalizedRequest = request.normalize()
        val updatedBrand = brandService.updateBrandName(manager.id, brandId, normalizedRequest)
        val updateBrandNameResponse = UpdateBrandNameResponse(updatedBrand.toDto())
        return ResponseEntity.ok(updateBrandNameResponse)
    }

    @PostMapping("/{brandId}/image")
    fun setBrandImage(
        @PathVariable brandId: Long,
        @RequestBody imageMetadata: ImageMetadata,
        @AuthenticationPrincipal manager: AuthUser
    ): ResponseEntity<UpdateBrandImageResponse> {
        val updateBrandImageResponse = brandService.setBrandImage(manager.id, brandId, imageMetadata)
        return ResponseEntity.ok(updateBrandImageResponse)
    }

    @DeleteMapping("/{brandId}/image")
    fun removeBrandImage(
        @PathVariable brandId: Long,
        @AuthenticationPrincipal manager: AuthUser
    ): ResponseEntity<Void> {
        brandService.removeBrandImage(manager.id, brandId)
        return ResponseEntity.noContent().build()
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

