package com.pt.ordersystem.ordersystem.domains.location

import com.pt.ordersystem.ordersystem.domains.location.models.LocationDto
import com.pt.ordersystem.ordersystem.domains.user.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Locations", description = "Public location API for customers")
@RestController
@RequestMapping("/api/public/locations")
class PublicLocationController(
  private val locationService: LocationService,
  private val userService: com.pt.ordersystem.ordersystem.domains.user.UserService
) {

  @GetMapping("/user/{userId}")
  fun getLocationsForUser(@PathVariable userId: String): ResponseEntity<List<LocationDto>> {
    // Validate user exists
    userService.getUserById(userId)
    
    val locations = locationService.getUserLocations(userId)
    return ResponseEntity.ok(locations)
  }

}

