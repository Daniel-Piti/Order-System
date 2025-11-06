package com.pt.ordersystem.ordersystem.domains.productOverrides

import com.pt.ordersystem.ordersystem.domains.customer.CustomerService
import com.pt.ordersystem.ordersystem.domains.product.ProductRepository
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.*
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ProductOverrideService(
  private val productOverrideRepository: ProductOverrideRepository,
  private val productRepository: ProductRepository,
  private val customerService: CustomerService
) {
  
  companion object {
    private const val MAX_PAGE_SIZE = 100
  }

  @Transactional
  fun createProductOverride(userId: String, request: CreateProductOverrideRequest): ProductOverrideDto {
    FieldValidators.validatePrice(request.overridePrice)
    
    try {
      productRepository.findById(request.productId)
    } catch (e: Exception) {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_NOT_FOUND.message,
        technicalMessage = "Product with id ${request.productId} not found for user $userId",
        severity = SeverityLevel.WARN
      )
    }

    try {
      customerService.getCustomerByIdAndUserId(userId, request.customerId)
    } catch (e: Exception) {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.CUSTOMER_NOT_FOUND.message,
        technicalMessage = "Customer with id ${request.customerId} not found for user $userId",
        severity = SeverityLevel.WARN
      )
    }

    // Check if override already exists
    val existingOverride = productOverrideRepository.findByProductIdAndCustomerId(request.productId, request.customerId)
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
      userId = userId,
      customerId = request.customerId,
      overridePrice = request.overridePrice,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    val savedOverride = productOverrideRepository.save(productOverride)
    return savedOverride.toDto()
  }

  fun getAllOverrides(
    userId: String,
    page: Int,
    size: Int,
    sortBy: String,
    sortDirection: String,
    productId: String?,
    customerId: String?
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

    // Fetch with JOIN to get product prices in single query
    return productOverrideRepository.findOverridesWithPrice(userId, productId, customerId, pageable)
      .map { it.toProductOverrideWithPriceDto() }
  }

  fun getProductOverrideById(userId: String, overrideId: Long): ProductOverrideDto {
    val override = productOverrideRepository.findByUserIdAndId(userId, overrideId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_NOT_FOUND.message,
        technicalMessage = "Product override with id $overrideId not found for user $userId",
        severity = SeverityLevel.WARN
      )
    return override.toDto()
  }

  fun getProductOverridesForProductId(userId: String, productId: String): List<ProductOverrideDto> =
    productOverrideRepository.findByUserIdAndProductId(userId, productId).map { it.toDto() }


  fun getProductOverridesByCustomerId(userId: String, customerId: String): List<ProductOverrideDto> =
    productOverrideRepository.findByUserIdAndCustomerId(userId, customerId).map { it.toDto() }

  @Transactional
  fun updateProductOverride(userId: String, overrideId: Long, request: UpdateProductOverrideRequest): ProductOverrideDto {
    FieldValidators.validatePrice(request.overridePrice)
    
    val override = productOverrideRepository.findByUserIdAndId(userId, overrideId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_NOT_FOUND.message,
        technicalMessage = "Product override with id $overrideId not found for user $userId",
        severity = SeverityLevel.WARN
      )

    val updatedOverride = override.copy(
      overridePrice = request.overridePrice,
      updatedAt = LocalDateTime.now()
    )

    val savedOverride = productOverrideRepository.save(updatedOverride)
    return savedOverride.toDto()
  }

  fun deleteProductOverride(userId: String, overrideId: Long) {
    val override = productOverrideRepository.findByUserIdAndId(userId, overrideId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_NOT_FOUND.message,
        technicalMessage = "Product override with id $overrideId not found for user $userId",
        severity = SeverityLevel.WARN
      )
    
    productOverrideRepository.delete(override)
  }

  @Transactional
  fun deleteAllOverridesForProduct(userId: String, productId: String) {
    val overrides = productOverrideRepository.findByUserIdAndProductId(userId, productId)
    productOverrideRepository.deleteAll(overrides)
  }

  private fun ProductOverrideDbEntity.toDto() = ProductOverrideDto(
    id = id,
    productId = productId,
    userId = userId,
    customerId = customerId,
    overridePrice = overridePrice,
  )

}
