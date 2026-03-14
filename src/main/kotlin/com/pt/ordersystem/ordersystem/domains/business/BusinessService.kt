package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.domains.business.models.Business
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDbEntity
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDto
import com.pt.ordersystem.ordersystem.domains.business.models.CreateBusinessRequest
import com.pt.ordersystem.ordersystem.domains.business.models.SetBusinessImageResponse
import com.pt.ordersystem.ordersystem.domains.business.models.UpdateBusinessDetailsRequest
import com.pt.ordersystem.ordersystem.domains.business.models.toDto
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BusinessService(
    private val businessRepository: BusinessRepository,
    private val s3StorageService: S3StorageService,
    private val businessValidationService: BusinessValidationService,
) {

    fun getBusinessByManagerId(managerId: String): BusinessDto {
        val business = businessRepository.findByManagerId(managerId)
        return business.toDto()
    }

    fun getBusinessesByManagerIds(managerIds: List<String>): List<Business> =
        businessRepository.findByManagerIdIn(managerIds)

    @Transactional
    fun createBusiness(request: CreateBusinessRequest): Business {
        businessValidationService.validateCreateBusiness(request)

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

        val businessEntity = businessRepository.findEntityByManagerId(managerId)

        val updatedEntity = businessEntity.copy(
            name = request.name,
            stateIdNumber = request.stateIdNumber,
            email = request.email,
            phoneNumber = request.phoneNumber,
            streetAddress = request.streetAddress,
            city = request.city,
            updatedAt = LocalDateTime.now()
        )

        return businessRepository.save(updatedEntity).toDto()
    }

    @Transactional
    fun removeBusinessImage(managerId: String) {
        val businessEntity = businessRepository.findEntityByManagerId(managerId)
        s3StorageService.deleteImageIfExists(businessEntity.s3Key)
        val updatedEntity = businessEntity.copy(
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
        val businessEntity = businessRepository.findEntityByManagerId(managerId)
        businessEntity.s3Key?.let { s3StorageService.deleteImageIfExists(it) }

        val preSignedUrlResult = s3StorageService.generatePreSignedUploadUrl(
            basePath = "managers/$managerId/business",
            imageMetadata = imageMetadata
        )

        val updatedEntity = businessEntity.copy(
            s3Key = preSignedUrlResult.s3Key,
            fileName = imageMetadata.fileName,
            fileSizeBytes = imageMetadata.fileSizeBytes,
            mimeType = imageMetadata.contentType,
            updatedAt = LocalDateTime.now()
        )

        val saved = businessRepository.save(updatedEntity)
        return SetBusinessImageResponse(
            business = saved.toDto(),
            preSignedUrl = preSignedUrlResult.preSignedUrl
        )
    }
}
