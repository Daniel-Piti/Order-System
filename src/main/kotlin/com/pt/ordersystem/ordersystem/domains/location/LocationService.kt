package com.pt.ordersystem.ordersystem.domains.location

import com.pt.ordersystem.ordersystem.domains.location.helpers.LocationValidators
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

    fun validateCreateLocation(
        request: CreateLocationRequest,
        managerId: String,
    ) {
        val managerLocationCount = locationRepository.countByManagerId(managerId)
        LocationValidators.validateCreateLocationRequestFields(request)
        LocationValidators.validateMaxLocationCount(managerLocationCount, managerId)
    }

    @Transactional
    fun createLocation(managerId: String, request: CreateLocationRequest): Location {
        validateCreateLocation(request, managerId)

        val now = LocalDateTime.now()
        val entity = LocationDbEntity(
            managerId = managerId,
            name = request.name,
            streetAddress = request.streetAddress,
            city = request.city,
            phoneNumber = request.phoneNumber,
            createdAt = now,
            updatedAt = now,
        )

        return locationRepository.save(entity)
    }

    @Transactional
    fun updateLocation(managerId: String, locationId: Long, request: UpdateLocationRequest): Location {
        LocationValidators.validateUpdateLocationRequestFields(request)

        val existingLocation = locationRepository.findByManagerIdAndId(managerId, locationId)

        val updatedEntity = LocationDbEntity(
            id = existingLocation.id,
            managerId = existingLocation.managerId,
            name = request.name,
            streetAddress = request.streetAddress,
            city = request.city,
            phoneNumber = request.phoneNumber,
            createdAt = existingLocation.createdAt,
            updatedAt = LocalDateTime.now(),
        )

        return locationRepository.save(updatedEntity)
    }

    @Transactional
    fun deleteLocation(managerId: String, locationId: Long) {
        locationRepository.findByManagerIdAndId(managerId, locationId)

        val locationCount = locationRepository.countByManagerId(managerId)
        LocationValidators.validateAtLeastOneLocationExists(locationCount, managerId)

        locationRepository.deleteById(locationId)
    }
}
