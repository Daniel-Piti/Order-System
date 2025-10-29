package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.customer.CustomerService
import com.pt.ordersystem.ordersystem.domains.location.LocationRepository
import com.pt.ordersystem.ordersystem.domains.order.models.*
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class OrderService(
  private val orderRepository: OrderRepository,
  private val customerService: CustomerService,
  private val locationRepository: LocationRepository,
) {

  companion object {
    private const val MAX_PAGE_SIZE = 100
  }

  fun getAllOrdersForUser(
    userId: String,
    page: Int,
    size: Int,
    sortBy: String,
    sortDirection: String,
    status: String?
  ): Page<OrderDto> {
    // Enforce max page size
    val validatedSize = size.coerceAtMost(MAX_PAGE_SIZE)

    // Create sort based on direction
    val sort = if (sortDirection.uppercase() == "DESC") {
      Sort.by(sortBy).descending()
    } else {
      Sort.by(sortBy).ascending()
    }

    // Create pageable with sort
    val pageable = PageRequest.of(page, validatedSize, sort)

    // Fetch orders with optional status filter
    return if (status != null && status.isNotBlank()) {
      orderRepository.findAllByUserIdAndStatus(userId, status, pageable).map { it.toDto() }
    } else {
      orderRepository.findAllByUserId(userId, pageable).map { it.toDto() }
    }
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
    // Validate user has at least one location
    val locationCount = locationRepository.countByUserId(userId)
    if (locationCount == 0) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = OrderFailureReason.NO_LOCATIONS.userMessage,
        technicalMessage = OrderFailureReason.NO_LOCATIONS.technical + "userId=$userId",
        severity = SeverityLevel.INFO
      )
    }

    val now = LocalDateTime.now()

    // If customerId is provided, fetch customer data to pre-fill
    val customer = request.customerId?.let { customerId ->
      customerService.getCustomerByIdAndUserId(userId, customerId)
    }

    // Link expires in 7 days by default
    val linkExpiresAt = now.plusDays(7)

    val order = OrderDbEntity(
      id = GeneralUtils.genId(),
      userId = userId,
      // User location (will be filled by customer)
      userStreetAddress = null,
      userCity = null,
      userPhoneNumber = null,
      // Customer data (pre-filled if linked, otherwise customer fills)
      customerId = customer?.id,
      customerName = customer?.name,
      customerPhone = customer?.phoneNumber,
      customerEmail = customer?.email,
      customerStreetAddress = customer?.streetAddress,
      customerCity = customer?.city,
      // Order details (empty, customer fills)
      status = OrderStatus.EMPTY.name,
      products = emptyList(),
      productsVersion = 1,
      totalPrice = BigDecimal.ZERO,
      deliveryDate = null,
      linkExpiresAt = linkExpiresAt,
      notes = "",
      createdAt = now,
      updatedAt = now,
    )

    return orderRepository.save(order).id
  }

}
