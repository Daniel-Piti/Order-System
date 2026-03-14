package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.domains.product.models.Product
import com.pt.ordersystem.ordersystem.domains.product.models.ProductDbEntity
import com.pt.ordersystem.ordersystem.domains.product.models.ProductFailureReason
import com.pt.ordersystem.ordersystem.domains.product.models.toProduct
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ProductRepository(
  private val productDao: ProductDao,
) {

  fun getManagersProducts(managerId: String): List<Product> =
    productDao.findAllProductsByManagerId(managerId).map { it.toProduct() }

  fun getProductByManagerIdAndId(managerId: String, productId: String): Product =
    productDao.findProductByManagerIdAndId(managerId, productId)?.toProduct() ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = ProductFailureReason.NOT_FOUND.userMessage,
      technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId, managerId=$managerId",
      severity = SeverityLevel.WARN
    )

  fun countByManagerId(managerId: String): Long =
    productDao.countByManagerId(managerId)

  fun findEntityByManagerIdAndId(managerId: String, productId: String): ProductDbEntity =
    productDao.findByManagerIdAndId(managerId, productId) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = ProductFailureReason.NOT_FOUND.userMessage,
      technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId, managerId=$managerId",
      severity = SeverityLevel.WARN
    )

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
