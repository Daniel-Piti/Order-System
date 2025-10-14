package com.pt.ordersystem.ordersystem.product

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.order.OrderRepository
import com.pt.ordersystem.ordersystem.product.models.*
import com.pt.ordersystem.ordersystem.productOverrides.ProductOverrideRepository
import com.pt.ordersystem.ordersystem.productOverrides.ProductOverrideService
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProductService(
  private val productRepository: ProductRepository,
  private val productOverrideRepository: ProductOverrideRepository,
  private val orderRepository: OrderRepository,
) {

  companion object {
    const val MAXIMUM_PRODUCTS_FOR_CUSTOMER = 1000
  }

  fun getAllProductsForUser(userId: String): List<ProductDto> =
    productRepository.findAllByUserId(userId).map { it.toDto() }

  fun getAllProductsForOrder(orderId: String): List<ProductDto> {
    val order = orderRepository.findById(orderId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ProductFailureReason.NOT_FOUND.technical + "order=$orderId",
        severity = SeverityLevel.WARN
      )
    }

    val products = productRepository.findAllByUserId(order.userId)

    val productOverrides = productOverrideRepository
      .findByUserIdAndCustomerId(order.userId, order.customerId)
      .associateBy{ it.productId }

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

    AuthUtils.checkOwnership(product.userId)

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

  fun updateProduct(productId: String, request: UpdateProductRequest): String {

    with(request) {
      FieldValidators.validateNonEmpty(name, "'name'")
      FieldValidators.validatePrice(originalPrice)
      FieldValidators.validatePrice(specialPrice)
    }

    val product = productRepository.findById(productId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(product.userId)

    val updated = product.copy(
      name = request.name,
      originalPrice = request.originalPrice,
      specialPrice = request.specialPrice,
      pictureUrl = request.pictureUrl,
      updatedAt = LocalDateTime.now(),
    )

    return productRepository.save(updated).id
  }

  fun deleteProduct(productId: String) {
    val product = productRepository.findById(productId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ProductFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ProductFailureReason.NOT_FOUND.technical + "productId=$productId",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(product.userId)

    productRepository.delete(product)
  }

}