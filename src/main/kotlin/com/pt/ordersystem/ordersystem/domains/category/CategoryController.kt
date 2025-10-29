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
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/categories")
@PreAuthorize(AUTH_USER)
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getAllCategories(@AuthenticationPrincipal user: AuthUser): ResponseEntity<List<CategoryDto>> =
        ResponseEntity.ok(categoryService.getUserCategories(user.userId))

    @GetMapping("/{categoryId}")
    fun getCategoryById(
        @PathVariable categoryId: String,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<CategoryDto> =
        ResponseEntity.ok(categoryService.getCategoryById(user.userId, categoryId))

    @PostMapping
    fun createCategory(
        @RequestBody request: CreateCategoryRequest,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<String> {
        val categoryId = categoryService.createCategory(user.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryId)
    }

    @PutMapping("/{categoryId}")
    fun updateCategory(
        @PathVariable categoryId: String,
        @RequestBody request: UpdateCategoryRequest,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<String> {
        val updatedCategoryId = categoryService.updateCategory(user.userId, categoryId, request)
        return ResponseEntity.ok(updatedCategoryId)
    }

    @DeleteMapping("/{categoryId}")
    fun deleteCategory(
        @PathVariable categoryId: String,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<String> {
        categoryService.deleteCategory(user.userId, categoryId)
        return ResponseEntity.ok("Category deleted successfully")
    }
}
