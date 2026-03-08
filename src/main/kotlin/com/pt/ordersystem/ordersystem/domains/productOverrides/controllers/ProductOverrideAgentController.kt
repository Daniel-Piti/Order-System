package com.pt.ordersystem.ordersystem.domains.productOverrides.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_AGENT
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.agent.AgentService
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.CreateProductOverrideRequest
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideDto
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideWithPriceDto
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.UpdateProductOverrideRequest
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.toDto
import com.pt.ordersystem.ordersystem.utils.PageRequestBaseExternal
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
    @RequestParam(required = false) productId: String?,
    pageParams: PageRequestBaseExternal,
  ): ResponseEntity<Page<ProductOverrideWithPriceDto>> {

    val agent = agentService.getAgent(agent.id)
    val overrides = productOverrideService.getOverrides(
      managerId = agent.managerId,
      agentId = agent.id,
      pageRequestBase = pageParams.toPageRequestBase(),
      productId = productId,
    )
    return ResponseEntity.ok(overrides.map { it.toDto() })
  }

  @GetMapping("/product/{productId}")
  fun getOverridesByProduct(
    @AuthenticationPrincipal agent: AuthUser,
    @PathVariable productId: String,
  ): ResponseEntity<List<ProductOverrideDto>> {
    val agent = agentService.getAgent(agent.id)
    val overrides = productOverrideService.getProductOverridesForProductId(agent.managerId, agent.id, productId)
    return ResponseEntity.ok(overrides.map { it.toDto() })
  }

  @GetMapping("/customer/{customerId}")
  fun getOverridesByCustomer(
    @AuthenticationPrincipal agent: AuthUser,
    @PathVariable customerId: String,
  ): ResponseEntity<List<ProductOverrideDto>> {
    val agent = agentService.getAgent(agent.id)
    val overrides = productOverrideService.getProductOverridesByCustomerId(agent.managerId, agent.id, customerId)
    return ResponseEntity.ok(overrides.map { it.toDto() })
  }

  @PostMapping
  fun createOverride(
    @AuthenticationPrincipal agent: AuthUser,
    @RequestBody request: CreateProductOverrideRequest,
  ): ResponseEntity<ProductOverrideDto> {
    val agent = agentService.getAgent(agent.id)
    val override = productOverrideService.createProductOverride(agent.managerId, agent.id, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(override.toDto())
  }

  @PutMapping("/override/{overrideId}")
  fun updateOverride(
    @AuthenticationPrincipal agent: AuthUser,
    @PathVariable overrideId: Long,
    @RequestBody request: UpdateProductOverrideRequest,
  ): ResponseEntity<ProductOverrideDto> {
    val agent = agentService.getAgent(agent.id)
    val override = productOverrideService.updateProductOverride(agent.managerId, agent.id, overrideId, request)
    return ResponseEntity.ok(override.toDto())
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
