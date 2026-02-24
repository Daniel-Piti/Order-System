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

    return overridesPage.map { it.toProductOverrideWithPriceDto() }
  }

  fun getProductOverrideById(managerId: String, overrideId: Long, agentId: String? = null): ProductOverrideDto {
    val override = getValidOverride(managerId, overrideId, agentId)
    return override.toDto()
  }

  fun getProductOverridesForProductId(managerId: String, productId: String, agentId: String? = null): List<ProductOverrideDto> {
    val overrides = if (agentId == null) {
      productOverrideRepository.findByManagerIdAndProductId(managerId, productId)
    } else {
      productOverrideRepository.findByManagerIdAndAgentIdAndProductId(managerId, agentId, productId)
    }
    return overrides.map { it.toDto() }
  }

  fun getProductOverridesByCustomerId(managerId: String, customerId: String, agentId: String? = null): List<ProductOverrideDto> {
    val overrides = if (agentId == null) {
      productOverrideRepository.findByManagerIdAndCustomerId(managerId, customerId)
    } else {
      productOverrideRepository.findByManagerIdAndAgentIdAndCustomerId(managerId, agentId, customerId)
    }
    return overrides.map { it.toDto() }
  }

  @Transactional
  fun createProductOverride(managerId: String, request: CreateProductOverrideRequest, agentId: String? = null): ProductOverrideDto {
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

    // Check if override already exists
    val existingOverride = if (agentId == null) {
      productOverrideRepository.findByManagerIdAndAgentIdIsNullAndProductIdAndCustomerId(managerId, request.productId, request.customerId)
    } else {
      productOverrideRepository.findByManagerIdAndAgentIdAndProductIdAndCustomerId(managerId, agentId, request.productId, request.customerId)
    }
    if (existingOverride != null) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_ALREADY_EXISTS.message,
        technicalMessage = "Product override already exists for product ${request.productId} and customer ${request.customerId}",
        severity = SeverityLevel.WARN
      )
    }

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
  fun updateProductOverride(managerId: String, overrideId: Long, request: UpdateProductOverrideRequest, agentId: String? = null): ProductOverrideDto {
    FieldValidators.validatePrice(request.overridePrice)
    
    val override = getValidOverride(managerId, overrideId, agentId)

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

    val updatedOverride = override.copy(
      overridePrice = request.overridePrice,
      updatedAt = LocalDateTime.now()
    )

    val savedOverride = productOverrideRepository.save(updatedOverride)
    return savedOverride.toDto()
  }

  fun deleteProductOverride(managerId: String, overrideId: Long, agentId: String? = null) {
    val override = getValidOverride(managerId, overrideId, agentId)
    productOverrideRepository.delete(override)
  }

  @Transactional
  fun deleteAllOverridesForProduct(managerId: String, productId: String) {
    val overrides = productOverrideRepository.findByManagerIdAndProductId(managerId, productId)
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
      updatedAt = LocalDateTime.now()
    )
  }

  private fun ProductOverrideDbEntity.toDto() = ProductOverrideDto(
    id = id,
    productId = productId,
    managerId = managerId,
    agentId = agentId,
    customerId = customerId,
    overridePrice = overridePrice,
  )

  private fun getValidOverride(managerId: String, overrideId: Long, agentId: String?): ProductOverrideDbEntity {
    val override = productOverrideRepository.findByManagerIdAndId(managerId, overrideId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_NOT_FOUND.message,
        technicalMessage = "Product override with id $overrideId not found for manager $managerId",
        severity = SeverityLevel.WARN
      )

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
