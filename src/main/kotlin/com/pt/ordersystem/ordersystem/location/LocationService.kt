package com.pt.ordersystem.ordersystem.location

import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.location.models.*
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

  fun getLocationById(locationId: String): LocationDto {
    val location = locationRepository.findById(locationId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = LocationFailureReason.NOT_FOUND.userMessage,
        technicalMessage = LocationFailureReason.NOT_FOUND.technical + "locationId=$locationId",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(location.userId)

    return location.toDto()
  }

  fun getUserLocations(userId: String): List<LocationDto> =
    locationRepository.findByUserId(userId).map { it.toDto() }

  fun createLocation(userId: String, request: NewLocationRequest): String {
    val usersLocationCount = locationRepository.countByUserId(userId)

    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validateNonEmpty(name, "'address'")
      FieldValidators.validatePhoneNumber(phoneNumber)
    }

    if (usersLocationCount >= MAXIMUM_LOCATIONS) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = LocationFailureReason.TWO_MANY_LOCATIONS.userMessage,
        technicalMessage = LocationFailureReason.TWO_MANY_LOCATIONS.technical + "user=${userId}",
        severity = SeverityLevel.INFO
      )
    }

    val location = LocationDbEntity(
      id = GeneralUtils.genId(),
      userId = userId,
      name = request.name,
      address = request.address,
      phoneNumber = request.phoneNumber,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    return locationRepository.save(location).id
  }

  fun updateLocation(locationId: String, request: UpdateLocationRequest): String {

    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validateNonEmpty(address, "'name'")
      FieldValidators.validatePhoneNumber(phoneNumber)
    }

    val location = locationRepository.findById(locationId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = LocationFailureReason.NOT_FOUND.userMessage,
        technicalMessage = LocationFailureReason.NOT_FOUND.technical + "locationId=$locationId",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(location.userId)

    val updatedLocation = location.copy(
      name = request.name,
      address = request.address,
      phoneNumber = request.phoneNumber,
      updatedAt = LocalDateTime.now()
    )

    return locationRepository.save(updatedLocation).id
  }

  fun deleteLocation(locationId: String) {
    val location = locationRepository.findById(locationId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Location not found",
        technicalMessage = "Location ID: $locationId not found",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(location.userId)

    locationRepository.delete(location)
  }

}