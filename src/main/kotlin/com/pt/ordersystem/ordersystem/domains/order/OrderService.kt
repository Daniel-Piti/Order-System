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

  fun getOrderById(orderId: String): OrderDto {
    val order = orderRepository.findById(orderId).orElseThrow {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = OrderFailureReason.NOT_FOUND.userMessage,
        technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
        severity = SeverityLevel.WARN
      )
    }

    AuthUtils.checkOwnership(order.userId)

    return order.toDto()
  }

  fun createEmptyOrder(userId: String, request: CreateEmptyOrderRequest): String {
    val customer = customerService.getCustomerByIdAndUserId(userId, request.customerId)
    val now = LocalDateTime.now()

    val order = OrderDbEntity(
      id = GeneralUtils.genId(),
      userId = userId,
      customerId = customer.id,
      customerName = customer.name,
      customerPhone = customer.phoneNumber,
      customerEmail = customer.email,
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
