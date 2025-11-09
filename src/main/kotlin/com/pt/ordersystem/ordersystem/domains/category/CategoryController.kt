package com.pt.ordersystem.ordersystem.domains.category

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.category.models.*
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
@PreAuthorize(AUTH_USER)
class CategoryController(
    private val categoryService: CategoryService
) {

    @PostMapping
    fun createCategory(
        @RequestBody request: CreateCategoryRequest,
        @AuthenticationPrincipal manager: AuthUser
    ): ResponseEntity<Long> {
        val categoryId = categoryService.createCategory(manager.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryId)
    }

    @PutMapping("/{categoryId}")
    fun updateCategory(
        @PathVariable categoryId: Long,
        @RequestBody request: UpdateCategoryRequest,
        @AuthenticationPrincipal manager: AuthUser
    ): ResponseEntity<Long> {
        val updatedCategoryId = categoryService.updateCategory(manager.id, categoryId, request)
        return ResponseEntity.ok(updatedCategoryId)
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
