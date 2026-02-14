package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDto
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessUpdateResponse
import com.pt.ordersystem.ordersystem.domains.business.models.UpdateBusinessRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Business", description = "Manager business management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/businesses")
@PreAuthorize(AUTH_MANAGER)
class BusinessManagerController(
  private val businessService: BusinessService,
) {

  @GetMapping("/me")
  fun getMyBusiness(
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<BusinessDto> {
    val business = businessService.getBusinessByManagerId(manager.id)
    return ResponseEntity.ok(business)
  }

  @PutMapping("/me")
  fun updateMyBusiness(
    @RequestBody updateRequest: UpdateBusinessRequest,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<BusinessUpdateResponse> {
    val response = businessService.updateBusiness(manager.id, updateRequest)
    return ResponseEntity.ok(response)
  }
}
