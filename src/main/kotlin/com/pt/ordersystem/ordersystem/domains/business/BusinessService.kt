package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.domains.business.helpers.BusinessValidators
import com.pt.ordersystem.ordersystem.domains.business.models.Business
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
        return business.toDto(s3StorageService.getPublicUrl(business.s3Key))
    }

    fun getBusinessesByManagerIds(managerIds: List<String>): List<Business> =
        businessRepository.findByManagerIdIn(managerIds)

    fun validateCreateBusiness(request: CreateBusinessRequest) {
        BusinessValidators.validateCreateBusinessFields(request)

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
    }

    @Transactional
    fun createBusiness(request: CreateBusinessRequest): Business {
        validateCreateBusiness(request)

        val now = LocalDateTime.now()
        val business = BusinessDbEntity(
            id = GeneralUtils.genId(),
            managerId = request.managerId,
            name = request.name,
            stateIdNumber = request.stateIdNumber,
            email = request.email,
            phoneNumber = request.phoneNumber,
            streetAddress = request.streetAddress,
            city = request.city,
            s3Key = null,
            fileName = null,
            fileSizeBytes = null,
            mimeType = null,
            createdAt = now,
            updatedAt = now
        )

        return businessRepository.save(business)
    }

    @Transactional
    fun updateBusiness(managerId: String, request: UpdateBusinessRequest): BusinessUpdateResponse {
        BusinessValidators.validateUpdateBusinessFields(request)

        val business = businessRepository.findEntityByManagerId(managerId)

        // Handle image removal if we want to remove image
        if (request.removeImage == true) {
            s3StorageService.deleteImageIfExists(business.s3Key)
        }

        // Handle image removal & update in case of update
        val preSignedUrlResult = request.imageMetadata?.let { imageMetadata ->
            s3StorageService.deleteImageIfExists(business.s3Key)
            s3StorageService.generatePreSignedUploadUrl(
                basePath = "managers/$managerId/business",
                imageMetadata = imageMetadata
            )
        }

        val updatedBusiness = business.copy(
            name = request.name,
            stateIdNumber = request.stateIdNumber,
            email = request.email,
            phoneNumber = request.phoneNumber,
            streetAddress = request.streetAddress,
            city = request.city,
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

        val saved = businessRepository.save(updatedBusiness)

        return BusinessUpdateResponse(
            businessDto = saved.toDto(s3StorageService.getPublicUrl(saved.s3Key)),
            preSignedUrl = preSignedUrlResult?.preSignedUrl
        )
    }
}
