package com.pt.ordersystem.ordersystem.domains.location

import com.pt.ordersystem.ordersystem.domains.location.models.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class LocationService(
    private val locationRepository: LocationRepository,
) {

    fun getManagerLocations(managerId: String): List<Location> =
        locationRepository.findByManagerId(managerId)

    @Transactional
    fun createLocation(managerId: String, request: CreateLocationRequest): Location {

        val now = LocalDateTime.now()
        val locationDbEntity = LocationDbEntity(
            managerId = managerId,
            name = request.name,
            streetAddress = request.streetAddress,
            city = request.city,
            phoneNumber = request.phoneNumber,
            createdAt = now,
            updatedAt = now,
        )

        return locationRepository.save(locationDbEntity)
    }

    @Transactional
    fun updateLocation(managerId: String, locationId: Long, request: UpdateLocationRequest): Location {

        val existingLocation = locationRepository.findEntityByManagerIdAndId(managerId, locationId)

        val updatedEntity = existingLocation.copy(
            name = request.name,
            streetAddress = request.streetAddress,
            city = request.city,
            phoneNumber = request.phoneNumber,
            updatedAt = LocalDateTime.now(),
        )

        return locationRepository.save(updatedEntity)
    }

    @Transactional
    fun deleteLocation(managerId: String, locationId: Long) {
        locationRepository.findByManagerIdAndId(managerId, locationId)
        locationRepository.deleteById(locationId)
    }
}
