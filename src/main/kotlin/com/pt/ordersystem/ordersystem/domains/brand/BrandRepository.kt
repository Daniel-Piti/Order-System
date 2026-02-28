package com.pt.ordersystem.ordersystem.domains.brand

import com.pt.ordersystem.ordersystem.domains.brand.models.Brand
import com.pt.ordersystem.ordersystem.domains.brand.models.BrandDbEntity
import com.pt.ordersystem.ordersystem.domains.brand.models.BrandFailureReason
import com.pt.ordersystem.ordersystem.domains.brand.models.toModel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository

@Repository
class BrandRepository(
    private val brandDao: BrandDao,
) {

    fun findByManagerIdAndId(managerId: String, id: Long): Brand =
        brandDao.findByManagerIdAndId(managerId, id)?.toModel() ?: throw ServiceException(
            status = HttpStatus.NOT_FOUND,
            userMessage = BrandFailureReason.NOT_FOUND.userMessage,
            technicalMessage = BrandFailureReason.NOT_FOUND.technical + "brandId=$id",
            severity = SeverityLevel.WARN
        )

    fun findByManagerId(managerId: String): List<Brand> =
        brandDao.findByManagerId(managerId).map { it.toModel() }

    fun countByManagerId(managerId: String): Long = brandDao.countByManagerId(managerId)

    fun existsByManagerIdAndName(managerId: String, name: String): Boolean =
        brandDao.existsByManagerIdAndName(managerId, name)

    fun hasDuplicateName(managerId: String, name: String, id: Long): Boolean =
        brandDao.existsByManagerIdAndNameAndIdNot(managerId, name, id)

    fun save(brandDbEntity: BrandDbEntity): Brand = brandDao.save(brandDbEntity).toModel()

    fun deleteById(brandId: Long): Unit = brandDao.deleteById(brandId)

}

