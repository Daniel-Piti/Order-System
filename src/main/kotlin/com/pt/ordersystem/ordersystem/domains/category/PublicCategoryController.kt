package com.pt.ordersystem.ordersystem.domains.category

import com.pt.ordersystem.ordersystem.domains.category.models.CategoryDto
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Categories", description = "Public category API for customers")
@RestController
@RequestMapping("/api/public/categories")
class PublicCategoryController(
  private val categoryService: CategoryService
) {

  @GetMapping("/user/{userId}")
  fun getAllCategories(@PathVariable userId: String): ResponseEntity<List<CategoryDto>> =
    ResponseEntity.ok(categoryService.getUserCategories(userId))

  @GetMapping("/user/{userId}/category/{categoryId}")
  fun getCategoryById(
    @PathVariable userId: String,
    @PathVariable categoryId: String
  ): ResponseEntity<CategoryDto> =
    ResponseEntity.ok(categoryService.getCategoryById(userId, categoryId))

}

