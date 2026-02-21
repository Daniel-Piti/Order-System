package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.models.BrandDto
import com.pt.ordersystem.ordersystem.domains.brand.models.toDto
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
        managerService.validateManagerExists(managerId)
        val brands = brandService.getManagerBrands(managerId)
        return ResponseEntity.ok(brands.map { it.toDto() })
    }

    @GetMapping("/manager/{managerId}/brand/{brandId}")
    fun getBrandById(
        @PathVariable managerId: String,
        @PathVariable brandId: Long
    ): ResponseEntity<BrandDto> {
        managerService.validateManagerExists(managerId)
        val brand = brandService.getBrandById(managerId, brandId)
        return ResponseEntity.ok(brand.toDto())
    }
}

