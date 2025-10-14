package com.pt.ordersystem.ordersystem.domains.location

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.location.models.LocationDto
import com.pt.ordersystem.ordersystem.domains.location.models.NewLocationRequest
import com.pt.ordersystem.ordersystem.domains.location.models.UpdateLocationRequest
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

  @GetMapping("/{id}")
  fun getLocationById(
    @PathVariable locationId: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<LocationDto> =
    ResponseEntity.ok(locationService.getLocationById(user.userId, locationId))

  @GetMapping("/all")
  fun getUserLocations(@AuthenticationPrincipal user: AuthUser): ResponseEntity<List<LocationDto>> =
    ResponseEntity.ok(locationService.getUserLocations(user.userId))

  @PostMapping("/create")
  fun createLocation(
    @RequestBody request: NewLocationRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    val createdId = locationService.createLocation(user.userId, request)
    return ResponseEntity.status(HttpStatus.CREATED).body(createdId)
  }

  @PutMapping("/update/{locationId}")
  fun updateLocation(
    @PathVariable locationId: String,
    @RequestBody request: UpdateLocationRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> =
    ResponseEntity.ok(locationService.updateLocation(user.userId, locationId, request))

  @DeleteMapping("/delete/{locationId}")
  fun deleteLocation(
    @PathVariable locationId: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    locationService.deleteLocation(user.userId, locationId)
    return ResponseEntity.ok("Location deleted successfully")
  }

}