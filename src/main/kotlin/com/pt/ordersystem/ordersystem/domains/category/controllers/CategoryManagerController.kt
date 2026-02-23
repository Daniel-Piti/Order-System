package com.pt.ordersystem.ordersystem.domains.category.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.category.CategoryService
import com.pt.ordersystem.ordersystem.domains.category.models.CategoryDto
import com.pt.ordersystem.ordersystem.domains.category.models.CreateCategoryRequest
import com.pt.ordersystem.ordersystem.domains.category.models.UpdateCategoryRequest
import com.pt.ordersystem.ordersystem.domains.category.models.toDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Categories", description = "Manager category management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/categories")
@PreAuthorize(AUTH_MANAGER)
class CategoryManagerController(
    private val categoryService: CategoryService,
) {

    @PostMapping
    fun createCategory(
        @RequestBody request: CreateCategoryRequest,
        @AuthenticationPrincipal manager: AuthUser
    ): ResponseEntity<CategoryDto> {
        val normalizedRequest = request.normalize()
        val category = categoryService.createCategory(manager.id, normalizedRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(category.toDto())
    }

    @PutMapping("/{categoryId}")
    fun updateCategory(
        @PathVariable categoryId: Long,
        @RequestBody request: UpdateCategoryRequest,
        @AuthenticationPrincipal manager: AuthUser
    ): ResponseEntity<CategoryDto> {
        val normalizedRequest = request.normalize()
        val updatedCategory = categoryService.updateCategory(manager.id, categoryId, normalizedRequest)
        return ResponseEntity.ok(updatedCategory.toDto())
    }

    @DeleteMapping("/{categoryId}")
    fun deleteCategory(
        @PathVariable categoryId: Long,
        @AuthenticationPrincipal manager: AuthUser
    ): ResponseEntity<String> {
        categoryService.deleteCategory(manager.id, categoryId)
        return ResponseEntity.ok("Category deleted successfully")
    }
}
