package com.pt.ordersystem.ordersystem.domains.productOverrides

import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverride
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideDbEntity
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideFailureReason
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideWithPrice
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.toModel
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.toProductOverrideWithPrice
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
class ProductOverrideRepository(
  private val productOverrideDao: ProductOverrideDao,
) {
  fun findByManagerIdAndId(managerId: String, overrideId: Long): ProductOverride =
    productOverrideDao.findByManagerIdAndId(managerId, overrideId)?.toModel() ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_NOT_FOUND.message,
      technicalMessage = "Product override with id $overrideId not found for manager $managerId",
      severity = SeverityLevel.WARN
    )

  fun getAllForManagerIdAndProductId(managerId: String, productId: String): List<ProductOverride> =
    productOverrideDao.findByManagerIdAndProductId(managerId, productId).map { it.toModel() }

  fun findByManagerIdAndAgentIdAndProductId(managerId: String, agentId: String?, productId: String): List<ProductOverride> =
    productOverrideDao.findByManagerIdAndAgentIdAndProductId(managerId, agentId, productId).map { it.toModel() }

  fun getAllForManagerIdAndCustomerId(managerId: String, customerId: String): List<ProductOverride> =
    productOverrideDao.findByManagerIdAndCustomerId(managerId, customerId).map { it.toModel() }

  fun validateOverrideNotExists(managerId: String, agentId: String?, productId: String, customerId: String) {
    val existing = productOverrideDao.findByManagerIdAndAgentIdAndProductIdAndCustomerId(managerId, agentId, productId, customerId)
    if (existing != null) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_ALREADY_EXISTS.message,
        technicalMessage = "Product override already exists for product $productId and customer $customerId",
        severity = SeverityLevel.WARN,
      )
    }
  }

  fun findByManagerIdAndAgentId(managerId: String, agentId: String): List<ProductOverride> =
    productOverrideDao.findByManagerIdAndAgentId(managerId, agentId).map { it.toModel() }

  fun findByManagerIdAndAgentIdAndCustomerId(managerId: String, agentId: String, customerId: String): List<ProductOverride> =
    productOverrideDao.findByManagerIdAndAgentIdAndCustomerId(managerId, agentId, customerId).map { it.toModel() }

  fun save(entity: ProductOverrideDbEntity): ProductOverride = productOverrideDao.save(entity).toModel()

  fun delete(overrideId: Long) = productOverrideDao.deleteById(overrideId)

  fun deleteAll(overrides: List<ProductOverride>) =
    overrides.forEach { productOverrideDao.deleteById(it.id) }

  fun updateInvalidOverridesForProduct(managerId: String, productId: String, newMinimumPrice: BigDecimal) {
    productOverrideDao.updateInvalidOverridesForProduct(
      managerId = managerId,
      productId = productId,
      newMinimumPrice = newMinimumPrice,
      updatedAt = LocalDateTime.now(),
    )
  }

  fun findOverridesWithPrice(
    managerId: String,
    agentId: String?,
    includeManagerOverrides: Boolean,
    includeAgentOverrides: Boolean,
    productId: String?,
    customerId: String?,
    pageable: Pageable,
  ): Page<ProductOverrideWithPrice> =
    productOverrideDao.findOverridesWithPrice(
      managerId = managerId,
      agentId = agentId,
      includeManagerOverrides = includeManagerOverrides,
      includeAgentOverrides = includeAgentOverrides,
      productId = productId,
      customerId = customerId,
      pageable = pageable,
    ).map { it.toProductOverrideWithPrice() }
}
