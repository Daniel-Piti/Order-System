package com.pt.ordersystem.ordersystem.domains.productOverrides

import com.pt.ordersystem.ordersystem.domains.customer.CustomerRepository
import com.pt.ordersystem.ordersystem.domains.product.ProductRepository
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.*
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.utils.PageRequestBase
import com.pt.ordersystem.ordersystem.utils.PaginationUtils
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ProductOverrideService(
  private val productOverrideRepository: ProductOverrideRepository,
  private val productRepository: ProductRepository,
  private val customerRepository: CustomerRepository,
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

  fun getProductOverrideById(managerId: String, agentId: String?, overrideId: Long): ProductOverride =
    productOverrideRepository.getProductOverride(managerId, agentId, overrideId)

  fun getProductOverridesForProductId(managerId: String, agentId: String?, productId: String): List<ProductOverride> =
    productOverrideRepository.getAllForManagerAgentAndProduct(managerId, agentId, productId)

  fun getProductOverridesByCustomerId(managerId: String, agentId: String?, customerId: String): List<ProductOverride> =
    productOverrideRepository.findByManagerIdAndAgentIdAndCustomerId(managerId, agentId, customerId)

  fun validateCreateProductOverride(
    managerId: String,
    agentId: String?,
    request: CreateProductOverrideRequest,
  ) {
    FieldValidators.validatePriceRange(request.overridePrice)

    val product = productRepository.findByManagerIdAndId(managerId, request.productId)

    if (request.overridePrice < product.minimumPrice) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = ProductOverrideFailureReason.BELOW_MINIMUM_PRICE.message,
        technicalMessage = "Override price ${request.overridePrice} below minimum price ${product.minimumPrice} for product ${request.productId} manager $managerId agentId=$agentId",
        severity = SeverityLevel.WARN
      )
    }

    productOverrideRepository.validateOverrideNotExists(
      managerId = managerId,
      agentId = agentId,
      productId = request.productId,
      customerId = request.customerId,
    )

    customerRepository.findByManagerIdAndAgentIdAndId(managerId, agentId, request.customerId)
  }

  @Transactional
  fun createProductOverride(managerId: String, agentId: String?, request: CreateProductOverrideRequest): ProductOverride {
    validateCreateProductOverride(managerId, agentId, request)

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
    FieldValidators.validatePriceRange(request.overridePrice)

    val override = productOverrideRepository.getProductOverride(managerId, agentId, overrideId)

    val product = productRepository.findByManagerIdAndId(managerId, override.productId)

    if (request.overridePrice < product.minimumPrice) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = ProductOverrideFailureReason.BELOW_MINIMUM_PRICE.message,
        technicalMessage = "Override price ${request.overridePrice} below minimum price ${product.minimumPrice} for product ${override.productId} manager $managerId agentId=$agentId overrideId=$overrideId",
        severity = SeverityLevel.WARN
      )
    }

    val updatedEntity = override.toEntity(
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
