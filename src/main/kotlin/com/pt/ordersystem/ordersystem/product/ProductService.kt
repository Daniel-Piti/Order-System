package com.pt.ordersystem.ordersystem.product

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProductService(
  private val productRepository: ProductRepository
) {

  fun getAllProductsForUser(userId: String): List<ProductDto> =
    productRepository.findAllByUserId(userId).map { it.toDto() }

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

  fun createProduct(userId: String, request: ProductRequest): String {
    val product = ProductDbEntity(
      id = GeneralUtils.genId(),
      userId = userId,
      name = request.name,
      price = request.price,
      picture = request.picture,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )
    return productRepository.save(product).id
  }

  fun updateProduct(productId: String, request: ProductRequest): String {
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
      price = request.price,
      picture = request.picture,
      updatedAt = LocalDateTime.now()
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