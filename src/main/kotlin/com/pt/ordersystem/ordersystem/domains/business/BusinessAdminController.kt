package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_ADMIN
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDto
import com.pt.ordersystem.ordersystem.domains.business.models.CreateBusinessRequest
import com.pt.ordersystem.ordersystem.domains.business.models.toDto
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Business Management", description = "Admin business management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/businesses")
@PreAuthorize(AUTH_ADMIN)
class BusinessAdminController(
  private val businessService: BusinessService,
  private val s3StorageService: S3StorageService,
) {

  @PostMapping
  fun createBusiness(@RequestBody createBusinessRequest: CreateBusinessRequest): ResponseEntity<BusinessDto> {
    val normalizedRequest = createBusinessRequest.normalize()
    val business = businessService.createBusiness(normalizedRequest)
    val businessDto = business.toDto(s3StorageService.getPublicUrl(business.s3Key))
    return ResponseEntity.status(HttpStatus.CREATED).body(businessDto)
  }

  @PostMapping("/by-managers")
  fun getBusinessesByManagerIds(@RequestBody managerIds: List<String>): ResponseEntity<Map<String, BusinessDto>> {
    val businesses = businessService.getBusinessesByManagerIds(managerIds)
    val businessesDto = businesses.associate { business ->
      business.managerId to business.toDto(s3StorageService.getPublicUrl(business.s3Key))
    }
    return ResponseEntity.ok(businessesDto)
  }
}
