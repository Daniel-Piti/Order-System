package com.pt.ordersystem.ordersystem.domains.category.controllers

import com.pt.ordersystem.ordersystem.domains.category.CategoryService
import com.pt.ordersystem.ordersystem.domains.category.models.CategoryDto
import com.pt.ordersystem.ordersystem.domains.category.models.toDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Categories", description = "Public category API for customers")
@RestController
@RequestMapping("/api/public/categories")
class PublicCategoryController(
    private val categoryService: CategoryService,
    private val managerService: ManagerService,
) {

    @GetMapping("/manager/{managerId}")
    fun getAllCategories(@PathVariable managerId: String): ResponseEntity<List<CategoryDto>> {
        managerService.validateManagerExists(managerId)
        return ResponseEntity.ok(categoryService.getManagerCategories(managerId).map { it.toDto() })
    }

    @GetMapping("/manager/{managerId}/category/{categoryId}")
    fun getCategoryById(
        @PathVariable managerId: String,
        @PathVariable categoryId: Long
    ): ResponseEntity<CategoryDto> {
        managerService.validateManagerExists(managerId)
        return ResponseEntity.ok(categoryService.getCategoryById(managerId, categoryId).toDto())
    }
}
