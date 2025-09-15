package com.pt.ordersystem.ordersystem.location

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.location.models.LocationDto
import com.pt.ordersystem.ordersystem.location.models.NewLocationRequest
import com.pt.ordersystem.ordersystem.location.models.UpdateLocationRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Locations", description = "User location management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/locations")
@PreAuthorize(AUTH_USER)
class LocationController(
  private val locationService: LocationService
) {

  @GetMapping
  fun getUserLocations(@AuthenticationPrincipal user: AuthUser): ResponseEntity<List<LocationDto>> {
    return ResponseEntity.ok(locationService.getUserLocations(user.userId))
  }

  @GetMapping("/location/{id}")
  fun getLocationById(@PathVariable id: String): ResponseEntity<LocationDto> =
    ResponseEntity.ok(locationService.getLocationById(id))

  @PostMapping
  fun createLocation(
    @RequestBody request: NewLocationRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    val createdId = locationService.createLocation(user.userId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(createdId)
  }

  @PutMapping("/{locationId}")
  fun updateLocation(
    @PathVariable locationId: String,
    @RequestBody request: UpdateLocationRequest
  ): ResponseEntity<String> =
    ResponseEntity.ok(locationService.updateLocation(locationId, request))

  @DeleteMapping("/{locationId}")
  fun deleteLocation(@PathVariable locationId: String): ResponseEntity<String> {
    locationService.deleteLocation(locationId)
    return ResponseEntity.ok("Location deleted successfully")
  }

}