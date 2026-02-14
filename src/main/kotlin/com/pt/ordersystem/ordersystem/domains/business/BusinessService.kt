package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDbEntity
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDto
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessFailureReason
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessUpdateResponse
import com.pt.ordersystem.ordersystem.domains.business.models.CreateBusinessRequest
import com.pt.ordersystem.ordersystem.domains.business.models.UpdateBusinessRequest
import com.pt.ordersystem.ordersystem.domains.business.models.toDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerRepository
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BusinessService(
  private val businessRepository: BusinessRepository,
  private val managerRepository: ManagerRepository,
  private val s3StorageService: S3StorageService,
) {

  fun getBusinessByManagerId(managerId: String): BusinessDto {
    val business = businessRepository.findByManagerId(managerId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = BusinessFailureReason.NOT_FOUND.userMessage,
        technicalMessage = BusinessFailureReason.NOT_FOUND.technical + "managerId=$managerId",
        severity = SeverityLevel.WARN
      )
    return business.toDto(s3StorageService.getPublicUrl(business.s3Key))
  }

  fun getBusinessesByManagerIds(managerIds: List<String>): Map<String, BusinessDto> {
    if (managerIds.isEmpty()) return emptyMap()

    val businesses = businessRepository.findByManagerIdIn(managerIds)
    return businesses.associateBy({ it.managerId }) { business ->
      business.toDto(s3StorageService.getPublicUrl(business.s3Key))
    }
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
    managerRepository.findById(request.managerId).orElseThrow {
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
      s3Key = null,
      fileName = null,
      fileSizeBytes = null,
      mimeType = null,
      createdAt = now,
      updatedAt = now
    )

    val savedBusiness = businessRepository.save(business)
    return savedBusiness.id
  }

  @Transactional
  fun updateBusiness(managerId: String, request: UpdateBusinessRequest): BusinessUpdateResponse {
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

    // Handle image removal
    if (request.removeImage == true) {
      // Delete old image from S3 if exists
      business.s3Key?.let { oldS3Key ->
        try {
          s3StorageService.deleteFile(oldS3Key)
        } catch (e: Exception) {
          println("Warning: Failed to delete business image: ${e.message}")
        }
      }
    }

    val preSignedUrlResult = request.imageMetadata?.let { imageMetadata ->
      // Delete old image if exists (only if not already deleted by removeImage)
      if (request.removeImage != true) {
        business.s3Key?.let { oldS3Key ->
          try {
            s3StorageService.deleteFile(oldS3Key)
          } catch (e: Exception) {
            println("Warning: Failed to delete old business image: ${e.message}")
          }
        }
      }

      s3StorageService.generatePreSignedUploadUrl(
        basePath = "managers/$managerId/business",
        imageMetadata = imageMetadata
      )
    }

    // Update business
    val updatedBusiness = business.copy(
      name = request.name.trim(),
      stateIdNumber = request.stateIdNumber.trim(),
      email = request.email.trim(),
      phoneNumber = request.phoneNumber,
      streetAddress = request.streetAddress.trim(),
      city = request.city.trim(),
      s3Key = when {
        request.removeImage == true -> null
        preSignedUrlResult != null -> preSignedUrlResult.s3Key
        else -> business.s3Key
      },
      fileName = when {
        request.removeImage == true -> null
        request.imageMetadata != null -> request.imageMetadata.fileName
        else -> business.fileName
      },
      fileSizeBytes = when {
        request.removeImage == true -> null
        request.imageMetadata != null -> request.imageMetadata.fileSizeBytes
        else -> business.fileSizeBytes
      },
      mimeType = when {
        request.removeImage == true -> null
        request.imageMetadata != null -> request.imageMetadata.contentType
        else -> business.mimeType
      },
      updatedAt = LocalDateTime.now()
    )

    val savedBusiness = businessRepository.save(updatedBusiness)

    return BusinessUpdateResponse(
      businessId = savedBusiness.id,
      preSignedUrl = preSignedUrlResult?.preSignedUrl
    )
  }
}
