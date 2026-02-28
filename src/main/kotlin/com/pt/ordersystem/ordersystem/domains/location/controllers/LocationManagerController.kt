package com.pt.ordersystem.ordersystem.domains.location.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.location.LocationService
import com.pt.ordersystem.ordersystem.domains.location.models.LocationDto
import com.pt.ordersystem.ordersystem.domains.location.models.CreateLocationRequest
import com.pt.ordersystem.ordersystem.domains.location.models.UpdateLocationRequest
import com.pt.ordersystem.ordersystem.domains.location.models.toDto
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
class LocationManagerController(
    private val locationService: LocationService,
) {

    @PostMapping
    fun createLocation(
        @RequestBody request: CreateLocationRequest,
        @AuthenticationPrincipal manager: AuthUser,
    ): ResponseEntity<LocationDto> {
        val normalizedRequest = request.normalize()
        val location = locationService.createLocation(manager.id, normalizedRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(location.toDto())
    }

    @PutMapping("/{locationId}")
    fun updateLocation(
        @PathVariable locationId: Long,
        @RequestBody request: UpdateLocationRequest,
        @AuthenticationPrincipal manager: AuthUser,
    ): ResponseEntity<LocationDto> {
        val normalizedRequest = request.normalize()
        val updatedLocation = locationService.updateLocation(manager.id, locationId, normalizedRequest)
        return ResponseEntity.ok(updatedLocation.toDto())
    }

    @DeleteMapping("/{locationId}")
    fun deleteLocation(
        @PathVariable locationId: Long,
        @AuthenticationPrincipal manager: AuthUser,
    ): ResponseEntity<String> {
        locationService.deleteLocation(manager.id, locationId)
        return ResponseEntity.ok("Location deleted successfully")
    }

}
