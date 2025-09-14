package com.pt.ordersystem.ordersystem.productOverrides

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.product.ProductRepository
import com.pt.ordersystem.ordersystem.productOverrides.models.*
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProductOverrideService(
  private val productOverrideRepository: ProductOverrideRepository,
  private val productRepository: ProductRepository,
  private val customerRepository: com.pt.ordersystem.ordersystem.customer.CustomerRepository
) {

  fun createProductOverride(userId: String, request: CreateProductOverrideRequest): ProductOverrideDto {
    // Verify product exists and belongs to user
    productRepository.findByUserIdAndId(userId, request.productId)
      ?: throw ServiceException(
        status = org.springframework.http.HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_NOT_FOUND.name,
        technicalMessage = "Product with id ${request.productId} not found for user $userId",
        severity = com.pt.ordersystem.ordersystem.exception.SeverityLevel.WARN
      )

    // Verify customer exists and belongs to user
    customerRepository.findByUserIdAndId(userId, request.customerId)
      ?: throw ServiceException(
        status = org.springframework.http.HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.CUSTOMER_NOT_FOUND.name,
        technicalMessage = "Customer with id ${request.customerId} not found for user $userId",
        severity = com.pt.ordersystem.ordersystem.exception.SeverityLevel.WARN
      )

    // Check if override already exists
    val existingOverride = productOverrideRepository.findByProductIdAndCustomerId(request.productId, request.customerId)
    if (existingOverride != null) {
      throw ServiceException(
        status = org.springframework.http.HttpStatus.BAD_REQUEST,
        userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_ALREADY_EXISTS.name,
        technicalMessage = "Product override already exists for product ${request.productId} and customer ${request.customerId}",
        severity = com.pt.ordersystem.ordersystem.exception.SeverityLevel.WARN
      )
    }

    val productOverride = ProductOverrideDbEntity(
      id = GeneralUtils.genId(),
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

  fun getProductOverridesByUserId(userId: String): List<ProductOverrideDto> {
    return productOverrideRepository.findByUserId(userId).map { it.toDto() }
  }

  fun getProductOverrideById(userId: String, overrideId: String): ProductOverrideDto {
    val override = productOverrideRepository.findByUserIdAndId(userId, overrideId)
      ?: throw ServiceException(
        status = org.springframework.http.HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_NOT_FOUND.name,
        technicalMessage = "Product override with id $overrideId not found for user $userId",
        severity = com.pt.ordersystem.ordersystem.exception.SeverityLevel.WARN
      )
    return override.toDto()
  }

  fun getProductOverridesByProductId(userId: String, productId: String): List<ProductOverrideDto> {
    return productOverrideRepository.findByUserIdAndProductId(userId, productId).map { it.toDto() }
  }

  fun updateProductOverride(userId: String, overrideId: String, request: UpdateProductOverrideRequest): ProductOverrideDto {
    val override = productOverrideRepository.findByUserIdAndId(userId, overrideId)
      ?: throw ServiceException(
        status = org.springframework.http.HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_NOT_FOUND.name,
        technicalMessage = "Product override with id $overrideId not found for user $userId",
        severity = com.pt.ordersystem.ordersystem.exception.SeverityLevel.WARN
      )

    val updatedOverride = override.copy(
      overridePrice = request.overridePrice,
      updatedAt = LocalDateTime.now()
    )

    val savedOverride = productOverrideRepository.save(updatedOverride)
    return savedOverride.toDto()
  }

  fun deleteProductOverride(userId: String, overrideId: String) {
    val override = productOverrideRepository.findByUserIdAndId(userId, overrideId)
      ?: throw ServiceException(
        status = org.springframework.http.HttpStatus.NOT_FOUND,
        userMessage = ProductOverrideFailureReason.PRODUCT_OVERRIDE_NOT_FOUND.name,
        technicalMessage = "Product override with id $overrideId not found for user $userId",
        severity = com.pt.ordersystem.ordersystem.exception.SeverityLevel.WARN
      )
    
    productOverrideRepository.delete(override)
  }

  private fun ProductOverrideDbEntity.toDto() = ProductOverrideDto(
    id = id,
    productId = productId,
    userId = userId,
    customerId = customerId,
    overridePrice = overridePrice,
  )
}
