package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.models.Brand
import com.pt.ordersystem.ordersystem.domains.brand.models.BrandDbEntity
import com.pt.ordersystem.ordersystem.domains.brand.models.BrandFailureReason
import com.pt.ordersystem.ordersystem.domains.brand.models.toModel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository

@Repository
class BrandRepository(
    private val brandDao: BrandDao,
    private val s3StorageService: S3StorageService,
) {

    fun findByManagerIdAndId(managerId: String, id: Long): Brand {
        val brandDbEntity = brandDao.findByManagerIdAndId(managerId, id) ?: throw ServiceException(
            status = HttpStatus.NOT_FOUND,
            userMessage = BrandFailureReason.NOT_FOUND.userMessage,
            technicalMessage = BrandFailureReason.NOT_FOUND.technical + "brandId=$id",
            severity = SeverityLevel.WARN
        )
        val imageUrl = s3StorageService.getPublicUrl(brandDbEntity.s3Key)

        return brandDbEntity.toModel(imageUrl)
    }

    fun findByManagerId(managerId: String): List<Brand> =
        brandDao.findByManagerId(managerId).map {
            it.toModel(s3StorageService.getPublicUrl(it.s3Key))
        }

    fun countByManagerId(managerId: String): Long =
        brandDao.countByManagerId(managerId)

    fun existsByManagerIdAndName(managerId: String, name: String): Boolean =
        brandDao.existsByManagerIdAndName(managerId, name)

    fun hasDuplicateName(managerId: String, name: String, id: Long): Boolean =
        brandDao.existsByManagerIdAndNameAndIdNot(managerId, name, id)

    fun save(brandDbEntity: BrandDbEntity): Brand {
        val brand = brandDao.save(brandDbEntity)
        return brand.toModel(s3StorageService.getPublicUrl(brand.s3Key))
    }

    fun deleteById(brandId: Long): Unit =
        brandDao.deleteById(brandId)

}

