package com.pt.ordersystem.ordersystem.domains.location

import com.pt.ordersystem.ordersystem.domains.location.models.LocationDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Locations", description = "Public location API for customers")
@RestController
@RequestMapping("/api/public/locations")
class PublicLocationController(
  private val locationService: LocationService,
  private val managerService: ManagerService
) {

  @GetMapping("/user/{userId}")
  fun getLocationsForUser(@PathVariable userId: String): ResponseEntity<List<LocationDto>> {
    // Validate user exists
    managerService.getManagerById(userId)
    
    val locations = locationService.getUserLocations(userId)
    return ResponseEntity.ok(locations)
  }

}

