package com.pt.ordersystem.ordersystem.location

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.utils.genId
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

  fun getLocationById(id: String): LocationDto {
    val location = locationRepository.findById(id).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = LocationFailureReason.NOT_FOUND.userMessage,
        technicalMessage = LocationFailureReason.NOT_FOUND.technical + "id=$id",
        severity = SeverityLevel.WARN
      )
    }
    return location.toDto()
  }

  fun getUserLocations(userId: String): List<LocationDto> =
    locationRepository.findByUserId(userId).map { it.toDto() }

  fun createLocation(request: NewLocationRequest): String {
    val usersLocationCount = locationRepository.countByUserId(request.userId)

    if (usersLocationCount >= MAXIMUM_LOCATIONS) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = LocationFailureReason.TWO_MANY_LOCATIONS.userMessage,
        technicalMessage = LocationFailureReason.TWO_MANY_LOCATIONS.technical + "user=${request.userId}",
        severity = SeverityLevel.INFO
      )
    }

    val location = LocationDbEntity(
      id = genId(),
      userId = request.userId,
      name = request.name,
      address = request.address,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    return locationRepository.save(location).id
  }

  fun updateLocation(id: String, request: UpdateLocationRequest): String {
    val existing = locationRepository.findById(id).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Location not found",
        technicalMessage = "Location ID: $id not found",
        severity = SeverityLevel.WARN
      )
    }

    val updated = existing.copy(
      name = request.name,
      address = request.address,
      updatedAt = LocalDateTime.now()
    )

    return locationRepository.save(updated).id
  }

  fun deleteLocation(id: String) {
    val existing = locationRepository.findById(id).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Location not found",
        technicalMessage = "Location ID: $id not found",
        severity = SeverityLevel.WARN
      )
    }
    locationRepository.delete(existing)
  }
}