package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.domains.business.helpers.BusinessValidators
import com.pt.ordersystem.ordersystem.domains.business.models.Business
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDbEntity
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDto
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessFailureReason
import com.pt.ordersystem.ordersystem.domains.business.models.CreateBusinessRequest
import com.pt.ordersystem.ordersystem.domains.business.models.SetBusinessImageResponse
import com.pt.ordersystem.ordersystem.domains.business.models.UpdateBusinessDetailsRequest
import com.pt.ordersystem.ordersystem.domains.business.models.toDto
import com.pt.ordersystem.ordersystem.domains.manager.ManagerRepository
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
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

        // Check if manager exists (throws if not found)
        managerRepository.findById(request.managerId)

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
    fun updateBusinessDetails(managerId: String, request: UpdateBusinessDetailsRequest): BusinessDto {
        BusinessValidators.validateUpdateBusinessFields(request)

        val entity = businessRepository.findEntityByManagerId(managerId)

        val updatedEntity = entity.copy(
            name = request.name,
            stateIdNumber = request.stateIdNumber,
            email = request.email,
            phoneNumber = request.phoneNumber,
            streetAddress = request.streetAddress,
            city = request.city,
            updatedAt = LocalDateTime.now()
        )

        val business = businessRepository.save(updatedEntity)
        return business.toDto(s3StorageService.getPublicUrl(business.s3Key))
    }

    @Transactional
    fun removeBusinessImage(managerId: String) {
        val entity = businessRepository.findEntityByManagerId(managerId)
        s3StorageService.deleteImageIfExists(entity.s3Key)
        val updatedEntity = entity.copy(
            s3Key = null,
            fileName = null,
            fileSizeBytes = null,
            mimeType = null,
            updatedAt = LocalDateTime.now()
        )
        businessRepository.save(updatedEntity)
    }

    @Transactional
    fun setBusinessImage(managerId: String, imageMetadata: ImageMetadata): SetBusinessImageResponse {
        val entity = businessRepository.findEntityByManagerId(managerId)
        entity.s3Key?.let { s3StorageService.deleteImageIfExists(it) }

        val preSignedUrlResult = s3StorageService.generatePreSignedUploadUrl(
            basePath = "managers/$managerId/business",
            imageMetadata = imageMetadata
        )

        val updatedEntity = entity.copy(
            s3Key = preSignedUrlResult.s3Key,
            fileName = imageMetadata.fileName,
            fileSizeBytes = imageMetadata.fileSizeBytes,
            mimeType = imageMetadata.contentType,
            updatedAt = LocalDateTime.now()
        )

        val saved = businessRepository.save(updatedEntity)
        return SetBusinessImageResponse(
            business = saved.toDto(s3StorageService.getPublicUrl(saved.s3Key)),
            preSignedUrl = preSignedUrlResult.preSignedUrl
        )
    }
}
