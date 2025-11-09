package com.pt.ordersystem.ordersystem.domains.location

import com.pt.ordersystem.ordersystem.domains.location.models.*
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LocationService(
  private val locationRepository: LocationRepository
) {

  companion object {
    const val MAXIMUM_LOCATIONS = 10
  }

  fun getLocationById(managerId: String, locationId: Long): LocationDto {
    val location = locationRepository.findByManagerIdAndId(managerId, locationId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = LocationFailureReason.NOT_FOUND.userMessage,
        technicalMessage = LocationFailureReason.NOT_FOUND.technical + "locationId=$locationId",
        severity = SeverityLevel.WARN
      )

    return location.toDto()
  }

  fun getManagerLocations(managerId: String): List<LocationDto> =
    locationRepository.findByManagerId(managerId).map { it.toDto() }

  fun createLocation(managerId: String, request: NewLocationRequest): Long {
    val managerLocationCount = locationRepository.countByManagerId(managerId)

    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validateNonEmpty(streetAddress, "'street address'")
      FieldValidators.validateNonEmpty(city, "'city'")
      FieldValidators.validatePhoneNumber(phoneNumber)
    }

    if (managerLocationCount >= MAXIMUM_LOCATIONS) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = LocationFailureReason.TWO_MANY_LOCATIONS.userMessage,
        technicalMessage = LocationFailureReason.TWO_MANY_LOCATIONS.technical + "manager=${managerId}",
        severity = SeverityLevel.INFO
      )
    }

    val location = LocationDbEntity(
      managerId = managerId,
      name = request.name,
      streetAddress = request.streetAddress,
      city = request.city,
      phoneNumber = request.phoneNumber,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    return locationRepository.save(location).id
  }

  fun updateLocation(managerId: String, locationId: Long, request: UpdateLocationRequest): Long {

    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validateNonEmpty(streetAddress, "'street address'")
      FieldValidators.validateNonEmpty(city, "'city'")
      FieldValidators.validatePhoneNumber(phoneNumber)
    }

    val location = locationRepository.findByManagerIdAndId(managerId, locationId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = LocationFailureReason.NOT_FOUND.userMessage,
        technicalMessage = LocationFailureReason.NOT_FOUND.technical + "locationId=$locationId",
        severity = SeverityLevel.WARN
      )

    val updatedLocation = location.copy(
      name = request.name,
      streetAddress = request.streetAddress,
      city = request.city,
      phoneNumber = request.phoneNumber,
      updatedAt = LocalDateTime.now()
    )

    return locationRepository.save(updatedLocation).id
  }

  fun deleteLocation(managerId: String, locationId: Long) {
    val location = locationRepository.findByManagerIdAndId(managerId, locationId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Location not found",
        technicalMessage = "Location ID: $locationId not found",
        severity = SeverityLevel.WARN
      )

    val locationCount = locationRepository.countByManagerId(managerId)
    if (locationCount <= 1) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Cannot delete the last location. You must have at least one location.",
        technicalMessage = "Manager $managerId attempted to delete their last location",
        severity = SeverityLevel.INFO
      )
    }

    locationRepository.delete(location)
  }

}