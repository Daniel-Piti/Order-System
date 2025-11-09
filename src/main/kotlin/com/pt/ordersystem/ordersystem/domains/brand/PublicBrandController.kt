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

    @GetMapping("/manager/{managerId}")
    fun getAllBrands(@PathVariable managerId: String): ResponseEntity<List<BrandDto>> {
        // Validate manager exists first - will throw 404 if not found
        managerService.getManagerById(managerId)
        
        return ResponseEntity.ok(brandService.getManagerBrands(managerId))
    }

    @GetMapping("/manager/{managerId}/brand/{brandId}")
    fun getBrandById(
        @PathVariable managerId: String,
        @PathVariable brandId: Long
    ): ResponseEntity<BrandDto> {
        // Validate manager exists
        managerService.getManagerById(managerId)
        
        return ResponseEntity.ok(brandService.getBrandById(managerId, brandId))
    }
}

