package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.models.BrandDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Brands", description = "Public brand API for customers")
@RestController
@RequestMapping("/api/public/brands")
class PublicBrandController(
    private val brandService: BrandService,
    private val managerService: ManagerService
) {

    @GetMapping("/user/{userId}")
    fun getAllBrands(@PathVariable userId: String): ResponseEntity<List<BrandDto>> {
        // Validate user exists first - will throw 404 if not found
        managerService.getManagerById(userId)
        
        return ResponseEntity.ok(brandService.getUserBrands(userId))
    }

    @GetMapping("/user/{userId}/brand/{brandId}")
    fun getBrandById(
        @PathVariable userId: String,
        @PathVariable brandId: Long
    ): ResponseEntity<BrandDto> {
        // Validate user exists
        managerService.getManagerById(userId)
        
        return ResponseEntity.ok(brandService.getBrandById(userId, brandId))
    }
}

