package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.domains.customer.CustomerService
import com.pt.ordersystem.ordersystem.domains.order.models.*
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class OrderService(
  private val orderRepository: OrderRepository,
  private val customerService: CustomerService,
) {

  fun getAllOrdersForUser(userId: String): List<OrderDto> {
    return orderRepository.findAllByUserId(userId).map { it.toDto() }
  }

  fun getOrderById(orderId: String, userId: String? = null): OrderDto {
    val order = orderRepository.findById(orderId).orElseThrow {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = OrderFailureReason.NOT_FOUND.userMessage,
        technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
        severity = SeverityLevel.WARN
      )
    }

    if (userId != null && order.userId != userId) {
      throw ServiceException(
        status = HttpStatus.FORBIDDEN,
        userMessage = "You are not authorized to access this order",
        technicalMessage = "User $userId tried to access order owned by ${order.userId}",
        severity = SeverityLevel.WARN
      )
    }

    return order.toDto()
  }

  fun createEmptyOrder(userId: String, request: CreateEmptyOrderRequest): String {
    val now = LocalDateTime.now()

    // If customerId is provided, fetch customer data and pre-fill the order
    val customer = request.customerId?.let { customerId ->
      customerService.getCustomerByIdAndUserId(userId, customerId)
    }

    val order = OrderDbEntity(
      id = GeneralUtils.genId(),
      userId = userId,
      customerId = customer?.id,
      customerName = customer?.name,
      customerPhone = customer?.phoneNumber,
      customerEmail = customer?.email,
      customerCity = null,
      customerAddress = null,
      locationId = null,
      status = OrderStatus.EMPTY.name,
      products = null,
      totalPrice = BigDecimal.ZERO,
      createdAt = now,
      updatedAt = now,
    )

    return orderRepository.save(order).id
  }

  fun deleteOrder(orderId: String) {
    val order = orderRepository.findById(orderId).orElseThrow {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = OrderFailureReason.NOT_FOUND.userMessage,
        technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(order.userId)

    orderRepository.delete(order)
  }
}
