package com.pt.ordersystem.ordersystem.domains.location.controllers

import com.pt.ordersystem.ordersystem.domains.location.LocationService
import com.pt.ordersystem.ordersystem.domains.location.models.LocationDto
import com.pt.ordersystem.ordersystem.domains.location.models.toDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Public Locations", description = "Public location API for customers")
@RestController
@RequestMapping("/api/public/locations")
class PublicLocationController(
    private val locationService: LocationService,
    private val managerService: ManagerService,
) {

    @GetMapping("/manager/{managerId}")
    fun getLocationsForManager(@PathVariable managerId: String): ResponseEntity<List<LocationDto>> {
        managerService.validateManagerExists(managerId)
        val locations = locationService.getManagerLocations(managerId)
        return ResponseEntity.ok(locations.map { it.toDto() })
    }

}
