package com.pt.ordersystem.ordersystem.domains.product

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.domains.product.models.*
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.domains.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProductService(
  private val productRepository: ProductRepository,
  private val productOverrideService: ProductOverrideService,
  private val orderService: OrderService,
) {

  companion object {
    const val MAXIMUM_PRODUCTS_FOR_CUSTOMER = 1000
  }

  fun getAllProductsForUser(userId: String): List<ProductDto> =
    productRepository.findAllByUserId(userId).map { it.toDto() }

  fun getAllProductsForOrder(orderId: String): List<ProductDto> {
    val order = orderService.getOrderById(orderId)
    val products = productRepository.findAllByUserId(order.userId)

    // If no customer assigned, return products with default prices
    val customerId = order.customerId ?: return products.map { it.toDto() }

    // Apply customer-specific price overrides
    val productOverrides = productOverrideService
      .getProductOverridesByCustomerId(order.userId, customerId)
      .associateBy { it.productId }

    return products.map { product ->
      val override = productOverrides[product.id]
      product.copy(
        specialPrice = override?.overridePrice ?: product.specialPrice,
      ).toDto()
    }
  }

  fun getProductById(productId: String): ProductDto {
    val product = productRepository.findById(productId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId",
        severity = SeverityLevel.WARN
      )
    }

    return product.toDto()
  }

  fun createProduct(userId: String, request: CreateProductRequest): String {

    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validatePrice(originalPrice)
      FieldValidators.validatePrice(specialPrice)
    }

    val numberOfProducts = productRepository.findAllByUserId(userId).size

    if(numberOfProducts >= MAXIMUM_PRODUCTS_FOR_CUSTOMER) {
      throw ServiceException(
        status = HttpStatus.CONFLICT,
        userMessage = "You have reached the product limit ($MAXIMUM_PRODUCTS_FOR_CUSTOMER)",
        technicalMessage = "User ($userId) has reached $MAXIMUM_PRODUCTS_FOR_CUSTOMER products",
        severity = SeverityLevel.WARN
      )
    }

    val product = ProductDbEntity(
      id = GeneralUtils.genId(),
      userId = userId,
      name = request.name,
      category = request.category,
      originalPrice = request.originalPrice,
      specialPrice = request.specialPrice,
      pictureUrl = request.pictureUrl,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    return productRepository.save(product).id
  }

  fun updateProduct(userId: String, productId: String, request: UpdateProductRequest): String {

    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validatePrice(originalPrice)
      FieldValidators.validatePrice(specialPrice)
    }

    val product = productRepository.findByUserIdAndId(userId, productId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId",
        severity = SeverityLevel.WARN
      )

    val updated = product.copy(
      name = request.name,
      originalPrice = request.originalPrice,
      specialPrice = request.specialPrice,
      pictureUrl = request.pictureUrl,
      updatedAt = LocalDateTime.now(),
    )

    return productRepository.save(updated).id
  }

  fun deleteProduct(userId: String, productId: String) {
    val product = productRepository.findByUserIdAndId(userId, productId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId",
        severity = SeverityLevel.WARN
      )

    productOverrideService.deleteAllOverridesForProduct(userId, productId)

    productRepository.delete(product)
  }

}