package com.pt.ordersystem.ordersystem.domains.location

import com.pt.ordersystem.ordersystem.domains.location.helpers.LocationValidators
import org.springframework.stereotype.Service

@Service
class LocationValidationService(
    private val locationRepository: LocationRepository,
) {

    fun validateMaxLocationCount(managerId: String) {
        val locationCount = locationRepository.countByManagerId(managerId)
        LocationValidators.validateMaxLocationCount(locationCount, managerId)
    }

    fun validateMinLocationCount(managerId: String) {
        val locationCount = locationRepository.countByManagerId(managerId)
        LocationValidators.validateAtLeastOneLocationExists(locationCount, managerId)
    }

}