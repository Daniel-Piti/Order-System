package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.domains.business.models.BusinessStoreDto
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Public Business", description = "Public business API for store header")
@RestController
@RequestMapping("/api/public/business")
class PublicBusinessController(
  private val businessService: BusinessService,
) {

  @GetMapping("/manager/{managerId}")
  fun getByManagerId(@PathVariable managerId: String): ResponseEntity<BusinessStoreDto> {
    val business = businessService.getBusinessByManagerId(managerId)
    return ResponseEntity.ok(
      BusinessStoreDto(
        name = business.name,
        imageUrl = business.imageUrl,
      )
    )
  }
}
