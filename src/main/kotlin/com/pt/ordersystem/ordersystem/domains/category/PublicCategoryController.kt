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

  @GetMapping("/manager/{managerId}")
  fun getAllCategories(@PathVariable managerId: String): ResponseEntity<List<CategoryDto>> {
    // Validate manager exists first - will throw 404 if not found
    managerService.getManagerById(managerId)
    
    return ResponseEntity.ok(categoryService.getManagerCategories(managerId))
  }

  @GetMapping("/manager/{managerId}/category/{categoryId}")
  fun getCategoryById(
    @PathVariable managerId: String,
    @PathVariable categoryId: Long
  ): ResponseEntity<CategoryDto> {
    // Validate manager exists
    managerService.getManagerById(managerId)
    
    return ResponseEntity.ok(categoryService.getCategoryById(managerId, categoryId))
  }

}

