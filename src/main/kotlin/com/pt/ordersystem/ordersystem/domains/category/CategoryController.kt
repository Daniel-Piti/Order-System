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

@Tag(name = "Categories", description = "User category management API")
@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping("/user/{userId}")
    fun getAllCategories(@PathVariable userId: String): ResponseEntity<List<CategoryDto>> =
        ResponseEntity.ok(categoryService.getUserCategories(userId))

    @GetMapping("/user/{userId}/{categoryId}")
    fun getCategoryById(
        @PathVariable userId: String,
        @PathVariable categoryId: String
    ): ResponseEntity<CategoryDto> =
        ResponseEntity.ok(categoryService.getCategoryById(userId, categoryId))

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize(AUTH_USER)
    @PostMapping
    fun createCategory(
        @RequestBody request: CreateCategoryRequest,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<String> {
        val categoryId = categoryService.createCategory(user.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryId)
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize(AUTH_USER)
    @PutMapping("/{categoryId}")
    fun updateCategory(
        @PathVariable categoryId: String,
        @RequestBody request: UpdateCategoryRequest,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<String> {
        val updatedCategoryId = categoryService.updateCategory(user.userId, categoryId, request)
        return ResponseEntity.ok(updatedCategoryId)
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize(AUTH_USER)
    @DeleteMapping("/{categoryId}")
    fun deleteCategory(
        @PathVariable categoryId: String,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<String> {
        categoryService.deleteCategory(user.userId, categoryId)
        return ResponseEntity.ok("Category deleted successfully")
    }
}
