package com.pt.ordersystem.ordersystem.domains.productOverrides

import com.pt.ordersystem.ordersystem.domains.customer.CustomerRepository
import com.pt.ordersystem.ordersystem.domains.product.ProductRepository
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.*
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
  
  companion object {
    private const val MAX_PAGE_SIZE = 100
  }

  fun getAllOverrides(
    managerId: String,
    actorAgentId: String? = null,
    filterAgentId: String? = null,
    includeManagerOverrides: Boolean = true,
    includeAgentOverrides: Boolean = true,
    page: Int,
    size: Int,
    sortBy: String,
    sortDirection: String,
    productId: String?,
    customerId: String?,
  ): Page<ProductOverrideWithPriceDto> {
    // Enforce max page size
    val validatedSize = size.coerceAtMost(MAX_PAGE_SIZE)

    // Create sort
    val sort = if (sortDirection.uppercase() == "DESC") {
      Sort.by(sortBy).descending()
    } else {
      Sort.by(sortBy).ascending()
    }

    // Create pageable
    val pageable = PageRequest.of(page, validatedSize, sort)

    val effectiveIncludeManager = when {
      actorAgentId != null -> false
      filterAgentId != null -> false
      else -> includeManagerOverrides
    }
    val effectiveIncludeAgent = when {
      actorAgentId != null -> true
      filterAgentId != null -> true
      else -> includeAgentOverrides
    }

    val overridesPage = productOverrideRepository.findOverridesWithPrice(
      managerId = managerId,
      agentId = filterAgentId ?: actorAgentId,
      includeManagerOverrides = effectiveIncludeManager,
      includeAgentOverrides = effectiveIncludeAgent,
      productId = productId,
      customerId = customerId,
      pageable = pageable,
    )

    return overridesPage.map { it.toDto() }
  }

  fun getProductOverrideById(managerId: String, agentId: String?, overrideId: Long): ProductOverrideDto =
    getValidOverride(managerId, agentId, overrideId).toDto()

  fun getProductOverridesForProductId(managerId: String, agentId: String?, productId: String): List<ProductOverrideDto> =
    productOverrideRepository.findByManagerIdAndAgentIdAndProductId(managerId, agentId, productId).map { it.toDto() }

  fun getProductOverridesByCustomerId(managerId: String, agentId: String?, customerId: String): List<ProductOverrideDto> {
    val overrides = if (agentId == null) {
      productOverrideRepository.getAllForManagerIdAndCustomerId(managerId, customerId)
    } else {
      productOverrideRepository.findByManagerIdAndAgentIdAndCustomerId(managerId, agentId, customerId)
    }
    return overrides.map { it.toDto() }
  }

  @Transactional
  fun createProductOverride(managerId: String, agentId: String?, request: CreateProductOverrideRequest): ProductOverrideDto {
    FieldValidators.validatePrice(request.overridePrice)

    val product = productRepository.findByManagerIdAndId(managerId, request.productId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_NOT_FOUND.message,
        technicalMessage = "Product with id ${request.productId} not found for manager $managerId",
        severity = SeverityLevel.WARN
      )

    if (request.overridePrice < product.minimumPrice) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = ProductOverrideFailureReason.BELOW_MINIMUM_PRICE.message,
        technicalMessage = "Override price ${request.overridePrice} below minimum price ${product.minimumPrice} for product ${request.productId} manager $managerId agentId=$agentId",
        severity = SeverityLevel.WARN
      )
    }

    if (agentId == null) customerRepository.findByManagerIdAndId(managerId, request.customerId)
    else customerRepository.findByManagerIdAndAgentIdAndId(managerId, agentId, request.customerId)

    productOverrideRepository.validateOverrideNotExists(
      managerId = managerId,
      agentId = agentId,
      productId = request.productId,
      customerId = request.customerId,
    )

    val productOverride = ProductOverrideDbEntity(
      productId = request.productId,
      managerId = managerId,
      agentId = agentId,
      customerId = request.customerId,
      overridePrice = request.overridePrice,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    val savedOverride = productOverrideRepository.save(productOverride)
    return savedOverride.toDto()
  }

  @Transactional
  fun updateProductOverride(
    managerId: String,
    overrideId: Long,
    request: UpdateProductOverrideRequest,
    agentId: String? = null
  ): ProductOverrideDto {
    FieldValidators.validatePrice(request.overridePrice)

    val override = getValidOverride(managerId, agentId, overrideId)

    val product = productRepository.findByManagerIdAndId(managerId, override.productId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_NOT_FOUND.message,
        technicalMessage = "Product with id ${override.productId} not found for manager $managerId",
        severity = SeverityLevel.WARN
      )

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

    val savedOverride = productOverrideRepository.save(updatedEntity)
    return savedOverride.toDto()
  }

  fun deleteProductOverride(managerId: String, agentId: String?, overrideId: Long) {
    val override = getValidOverride(managerId, agentId, overrideId)
    productOverrideRepository.delete(override.id)
  }

  @Transactional
  fun deleteAllOverridesForProduct(managerId: String, productId: String) {
    val overrides = productOverrideRepository.getAllForManagerIdAndProductId(managerId, productId)
    productOverrideRepository.deleteAll(overrides)
  }

  @Transactional
  fun deleteAllOverridesForAgent(managerId: String, agentId: String) {
    val overrides = productOverrideRepository.findByManagerIdAndAgentId(managerId, agentId)
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

  private fun getValidOverride(managerId: String, agentId: String?, overrideId: Long): ProductOverride {
    val override = productOverrideRepository.findByManagerIdAndId(managerId, overrideId)

    if (agentId != null && override.agentId != agentId) {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_NOT_FOUND.message,
        technicalMessage = "Product override with id $overrideId not found for manager $managerId agentId=$agentId",
        severity = SeverityLevel.WARN
      )
    }
    return override
  }

}
