package com.pt.ordersystem.ordersystem.domains.productOverrides.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_AGENT
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.agent.AgentService
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.CreateProductOverrideRequest
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideDto
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideWithPriceDto
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.UpdateProductOverrideRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Agent Product overrides", description = "Product overrides API for agents")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/agent/product-overrides")
@PreAuthorize(AUTH_AGENT)
class ProductOverrideAgentController(
  private val agentService: AgentService,
  private val productOverrideService: ProductOverrideService,
) {

  @GetMapping
  fun getAgentOverrides(
    @AuthenticationPrincipal agent: AuthUser,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int,
    @RequestParam(defaultValue = "customer_id") sortBy: String,
    @RequestParam(defaultValue = "ASC") sortDirection: String,
    @RequestParam(required = false) productId: String?,
    @RequestParam(required = false) customerId: String?,
  ): ResponseEntity<Page<ProductOverrideWithPriceDto>> {
    val agent = agentService.getAgent(agent.id)
    val overrides = productOverrideService.getAllOverrides(
      managerId = agent.managerId,
      actorAgentId = agent.id,
      filterAgentId = agent.id,
      includeManagerOverrides = false,
      includeAgentOverrides = true,
      page = page,
      size = size,
      sortBy = sortBy,
      sortDirection = sortDirection,
      productId = productId,
      customerId = customerId,
    )
    return ResponseEntity.ok(overrides)
  }

  @GetMapping("/override/{overrideId}")
  fun getOverrideById(
    @AuthenticationPrincipal agent: AuthUser,
    @PathVariable overrideId: Long,
  ): ResponseEntity<ProductOverrideDto> {
    val agent = agentService.getAgent(agent.id)
    val override = productOverrideService.getProductOverrideById(agent.managerId, agent.id, overrideId)
    return ResponseEntity.ok(override)
  }

  @GetMapping("/product/{productId}")
  fun getOverridesByProduct(
    @AuthenticationPrincipal agent: AuthUser,
    @PathVariable productId: String,
  ): ResponseEntity<List<ProductOverrideDto>> {
    val agent = agentService.getAgent(agent.id)
    val overrides = productOverrideService.getProductOverridesForProductId(agent.managerId, agent.id, productId)
    return ResponseEntity.ok(overrides)
  }

  @GetMapping("/customer/{customerId}")
  fun getOverridesByCustomer(
    @AuthenticationPrincipal agent: AuthUser,
    @PathVariable customerId: String,
  ): ResponseEntity<List<ProductOverrideDto>> {
    val agent = agentService.getAgent(agent.id)
    val overrides = productOverrideService.getProductOverridesByCustomerId(agent.managerId, agent.id, customerId)
    return ResponseEntity.ok(overrides)
  }

  @PostMapping
  fun createOverride(
    @AuthenticationPrincipal agent: AuthUser,
    @RequestBody request: CreateProductOverrideRequest,
  ): ResponseEntity<ProductOverrideDto> {
    val agent = agentService.getAgent(agent.id)
    val normalizedRequest = request.normalize()
    val override = productOverrideService.createProductOverride(agent.managerId, agent.id, normalizedRequest)
    return ResponseEntity.status(HttpStatus.CREATED).body(override)
  }

  @PutMapping("/override/{overrideId}")
  fun updateOverride(
    @AuthenticationPrincipal agent: AuthUser,
    @PathVariable overrideId: Long,
    @RequestBody request: UpdateProductOverrideRequest,
  ): ResponseEntity<ProductOverrideDto> {
    val agent = agentService.getAgent(agent.id)
    val override = productOverrideService.updateProductOverride(agent.managerId, overrideId, request, agent.id)
    return ResponseEntity.ok(override)
  }

  @DeleteMapping("/override/{overrideId}")
  fun deleteOverride(
    @AuthenticationPrincipal agent: AuthUser,
    @PathVariable overrideId: Long,
  ): ResponseEntity<String> {
    val agent = agentService.getAgent(agent.id)
    productOverrideService.deleteProductOverride(agent.managerId, agent.id, overrideId)
    return ResponseEntity.ok("Product override deleted successfully")
  }
}
