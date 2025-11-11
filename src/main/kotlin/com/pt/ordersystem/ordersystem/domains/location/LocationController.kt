package com.pt.ordersystem.ordersystem.domains.location

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.location.models.NewLocationRequest
import com.pt.ordersystem.ordersystem.domains.location.models.UpdateLocationRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Locations", description = "Manager location management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/locations")
@PreAuthorize(AUTH_MANAGER)
class LocationController(
  private val locationService: LocationService
) {

  @PostMapping
  fun createLocation(
    @RequestBody request: NewLocationRequest,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<Long> {
    val createdId = locationService.createLocation(manager.id, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(createdId)
  }

  @PutMapping("/{locationId}")
  fun updateLocation(
    @PathVariable locationId: Long,
    @RequestBody request: UpdateLocationRequest,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<Long> =
    ResponseEntity.ok(locationService.updateLocation(manager.id, locationId, request))

  @DeleteMapping("/{locationId}")
  fun deleteLocation(
    @PathVariable locationId: Long,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<String> {
    locationService.deleteLocation(manager.id, locationId)
    return ResponseEntity.ok("Location deleted successfully")
  }

}