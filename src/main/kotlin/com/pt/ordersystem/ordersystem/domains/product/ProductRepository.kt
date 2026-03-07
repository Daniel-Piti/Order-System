package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.product.models.ProductDbEntity
import com.pt.ordersystem.ordersystem.domains.product.models.ProductFailureReason
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ProductRepository(
  private val productDao: ProductDao,
) {

  fun findAllByManagerId(managerId: String): List<ProductDbEntity> =
    productDao.findAllByManagerId(managerId)

  fun findAllByManagerId(managerId: String, pageable: Pageable): Page<ProductDbEntity> =
    productDao.findAllByManagerId(managerId, pageable)

  fun countByManagerId(managerId: String): Long =
    productDao.countByManagerId(managerId)

  fun findByManagerIdAndId(managerId: String, productId: String): ProductDbEntity =
    productDao.findByManagerIdAndId(managerId, productId) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = ProductFailureReason.NOT_FOUND.userMessage,
      technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId, managerId=$managerId",
      severity = SeverityLevel.WARN
    )

  fun findByManagerIdAndCategoryId(managerId: String, categoryId: Long, pageable: Pageable): Page<ProductDbEntity> =
    productDao.findByManagerIdAndCategoryId(managerId, categoryId, pageable)

  fun findByManagerIdAndBrandId(managerId: String, brandId: Long, pageable: Pageable): Page<ProductDbEntity> =
    productDao.findByManagerIdAndBrandId(managerId, brandId, pageable)

  fun findByManagerIdAndCategoryIdAndBrandId(managerId: String, categoryId: Long, brandId: Long, pageable: Pageable): Page<ProductDbEntity> =
    productDao.findByManagerIdAndCategoryIdAndBrandId(managerId, categoryId, brandId, pageable)

  fun removeBrandFromProducts(managerId: String, brandId: Long): Int =
    productDao.removeBrandFromProducts(managerId, brandId, LocalDateTime.now())

  fun removeCategoryFromProducts(managerId: String, categoryId: Long): Int =
    productDao.removeCategoryFromProducts(managerId, categoryId, LocalDateTime.now())

  fun save(entity: ProductDbEntity): ProductDbEntity =
    productDao.save(entity)

  fun delete(entity: ProductDbEntity) =
    productDao.delete(entity)

  fun findAllById(ids: Iterable<String>): List<ProductDbEntity> =
    productDao.findAllById(ids).toList()
}
