package com.pt.ordersystem.ordersystem.domains.productOverrides

import com.pt.ordersystem.ordersystem.domains.productOverrides.models.*
import com.pt.ordersystem.ordersystem.utils.PageRequestBase
import com.pt.ordersystem.ordersystem.utils.PaginationUtils
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ProductOverrideService(
  private val productOverrideRepository: ProductOverrideRepository,
  private val productOverrideValidationService: ProductOverrideValidationService,
  ) {

  fun getOverrides(
    managerId: String,
    agentId: String?,
    productId: String?,
    pageRequestBase: PageRequestBase,
  ): Page<ProductOverrideWithPrice> {
    val pageRequest = PaginationUtils.getValidatedPageRequest(pageRequestBase)
    return productOverrideRepository.findOverridesWithPrice(
      managerId = managerId,
      agentId = agentId,
      productId = productId,
      pageable = pageRequest,
    )
  }

  @Transactional
  fun createProductOverride(managerId: String, agentId: String?, request: CreateProductOverrideRequest): ProductOverride {
    productOverrideValidationService.validateCreateProductOverride(managerId, agentId, request)

    val productOverride = ProductOverrideDbEntity(
      productId = request.productId,
      managerId = managerId,
      agentId = agentId,
      customerId = request.customerId,
      overridePrice = request.overridePrice,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    return productOverrideRepository.save(productOverride)
  }

  @Transactional
  fun updateProductOverride(
    managerId: String,
    agentId: String?,
    overrideId: Long,
    request: UpdateProductOverrideRequest,
  ): ProductOverride {
    val overrideEntity = productOverrideRepository.getProductOverrideEntity(managerId, agentId, overrideId)

    productOverrideValidationService.validateUpdateOverride(managerId, overrideEntity.productId, request.overridePrice)

    val updatedEntity = overrideEntity.copy(
      overridePrice = request.overridePrice,
      updatedAt = LocalDateTime.now()
    )

    return productOverrideRepository.save(updatedEntity)
  }

  fun deleteProductOverride(managerId: String, agentId: String?, overrideId: Long) {
    val override = productOverrideRepository.getProductOverride(managerId, agentId, overrideId)
    productOverrideRepository.delete(override.id)
  }

  @Transactional
  fun deleteAllOverridesForProduct(managerId: String, productId: String) {
    val overrides = productOverrideRepository.getAllForManagerAndProduct(managerId, productId)
    productOverrideRepository.deleteAll(overrides)
  }

  @Transactional
  fun deleteAllOverridesForAgent(managerId: String, agentId: String) {
    val overrides = productOverrideRepository.getAllForManagerAndAgent(managerId, agentId)
    productOverrideRepository.deleteAll(overrides)
  }

  @Transactional
  fun updateInvalidOverridesForProduct(managerId: String, productId: String, newMinimumPrice: BigDecimal) {
    productOverrideRepository.updateInvalidOverridesForProduct(
      managerId = managerId,
      productId = productId,
      newMinimumPrice = newMinimumPrice,
    )
  }

}
