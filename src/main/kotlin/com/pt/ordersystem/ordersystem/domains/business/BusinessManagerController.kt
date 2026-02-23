package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDto
import com.pt.ordersystem.ordersystem.domains.business.models.SetBusinessImageResponse
import com.pt.ordersystem.ordersystem.domains.business.models.UpdateBusinessDetailsResponse
import com.pt.ordersystem.ordersystem.domains.business.models.UpdateBusinessDetailsRequest
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
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
  fun updateBusinessDetails(
    @RequestBody updateRequest: UpdateBusinessDetailsRequest,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<UpdateBusinessDetailsResponse> {
    val normalizedRequest = updateRequest.normalize()
    val businessDto = businessService.updateBusinessDetails(manager.id, normalizedRequest)
    return ResponseEntity.ok(UpdateBusinessDetailsResponse(businessDto))
  }

  @DeleteMapping("/me/image")
  fun removeBusinessImage(
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<Void> {
    businessService.removeBusinessImage(manager.id)
    return ResponseEntity.noContent().build()
  }

  @PostMapping("/me/image")
  fun setBusinessImage(
    @RequestBody imageMetadata: ImageMetadata,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<SetBusinessImageResponse> {
    val response = businessService.setBusinessImage(manager.id, imageMetadata)
    return ResponseEntity.ok(response)
  }
}
