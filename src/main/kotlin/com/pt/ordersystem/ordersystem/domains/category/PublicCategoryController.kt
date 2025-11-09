package com.pt.ordersystem.ordersystem.domains.category

import com.pt.ordersystem.ordersystem.domains.category.models.CategoryDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Categories", description = "Public category API for customers")
@RestController
@RequestMapping("/api/public/categories")
class PublicCategoryController(
  private val categoryService: CategoryService,
  private val managerService: ManagerService
) {

  @GetMapping("/user/{userId}")
  fun getAllCategories(@PathVariable userId: String): ResponseEntity<List<CategoryDto>> {
    // Validate user exists first - will throw 404 if not found
    managerService.getManagerById(userId)
    
    return ResponseEntity.ok(categoryService.getUserCategories(userId))
  }

  @GetMapping("/user/{userId}/category/{categoryId}")
  fun getCategoryById(
    @PathVariable userId: String,
    @PathVariable categoryId: Long
  ): ResponseEntity<CategoryDto> {
    // Validate user exists
    managerService.getManagerById(userId)
    
    return ResponseEntity.ok(categoryService.getCategoryById(userId, categoryId))
  }

}

