package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDbEntity
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDto
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessFailureReason
import com.pt.ordersystem.ordersystem.domains.business.models.CreateBusinessRequest
import com.pt.ordersystem.ordersystem.domains.business.models.UpdateBusinessRequest
import com.pt.ordersystem.ordersystem.domains.business.models.toDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerRepository
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BusinessService(
  private val businessRepository: BusinessRepository,
  private val managerRepository: ManagerRepository,
) {

  fun getBusinessByManagerId(managerId: String): BusinessDto {
    val business = businessRepository.findByManagerId(managerId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = BusinessFailureReason.NOT_FOUND.userMessage,
        technicalMessage = BusinessFailureReason.NOT_FOUND.technical + "managerId=$managerId",
        severity = SeverityLevel.WARN
      )
    return business.toDto()
  }

  fun getBusinessesByManagerIds(managerIds: List<String>): Map<String, BusinessDto> {
    if (managerIds.isEmpty()) return emptyMap()

    val businesses = businessRepository.findByManagerIdIn(managerIds)
    return businesses.associateBy({ it.managerId }, { it.toDto() })
  }

  @Transactional
  fun createBusiness(request: CreateBusinessRequest): String {
    with(request) {
      FieldValidators.validateNonEmpty(managerId, "'manager id'")
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validateNonEmpty(stateIdNumber, "'state id number'")
      FieldValidators.validateEmail(email)
      FieldValidators.validatePhoneNumber(phoneNumber)
      FieldValidators.validateNonEmpty(streetAddress, "'street address'")
      FieldValidators.validateNonEmpty(city, "'city'")
    }

    // Check if manager exists
    val manager = managerRepository.findById(request.managerId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = BusinessFailureReason.MANAGER_NOT_FOUND.userMessage,
        technicalMessage = BusinessFailureReason.MANAGER_NOT_FOUND.technical + "managerId=${request.managerId}",
        severity = SeverityLevel.WARN
      )
    }

    // Check if business already exists for this manager
    if (businessRepository.existsByManagerId(request.managerId)) {
      throw ServiceException(
        status = HttpStatus.CONFLICT,
        userMessage = BusinessFailureReason.ALREADY_EXISTS.userMessage,
        technicalMessage = BusinessFailureReason.ALREADY_EXISTS.technical + "managerId=${request.managerId}",
        severity = SeverityLevel.INFO
      )
    }

    val now = LocalDateTime.now()
    val business = BusinessDbEntity(
      id = GeneralUtils.genId(),
      managerId = request.managerId,
      name = request.name.trim(),
      stateIdNumber = request.stateIdNumber.trim(),
      email = request.email.trim(),
      phoneNumber = request.phoneNumber,
      streetAddress = request.streetAddress.trim(),
      city = request.city.trim(),
      createdAt = now,
      updatedAt = now
    )

    val savedBusiness = businessRepository.save(business)
    return savedBusiness.id
  }

  @Transactional
  fun updateBusiness(managerId: String, request: UpdateBusinessRequest): BusinessDto {
    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validateNonEmpty(stateIdNumber, "'state id number'")
      FieldValidators.validateEmail(email)
      FieldValidators.validatePhoneNumber(phoneNumber)
      FieldValidators.validateNonEmpty(streetAddress, "'street address'")
      FieldValidators.validateNonEmpty(city, "'city'")
    }

    val business = businessRepository.findByManagerId(managerId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = BusinessFailureReason.NOT_FOUND.userMessage,
        technicalMessage = BusinessFailureReason.NOT_FOUND.technical + "managerId=$managerId",
        severity = SeverityLevel.WARN
      )

    val updatedBusiness = business.copy(
      name = request.name.trim(),
      stateIdNumber = request.stateIdNumber.trim(),
      email = request.email.trim(),
      phoneNumber = request.phoneNumber,
      streetAddress = request.streetAddress.trim(),
      city = request.city.trim(),
      updatedAt = LocalDateTime.now()
    )

    val savedBusiness = businessRepository.save(updatedBusiness)
    return savedBusiness.toDto()
  }
}
