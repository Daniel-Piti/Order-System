package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.domains.customer.CustomerService
import com.pt.ordersystem.ordersystem.domains.location.LocationRepository
import com.pt.ordersystem.ordersystem.domains.location.LocationService
import com.pt.ordersystem.ordersystem.domains.order.models.*
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class OrderService(
  private val orderRepository: OrderRepository,
  private val customerService: CustomerService,
  private val locationRepository: LocationRepository,
  private val locationService: LocationService,
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

  fun getOrderByIdPublic(orderId: String): OrderPublicDto {
    val order = orderRepository.findById(orderId).orElseThrow {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = OrderFailureReason.NOT_FOUND.userMessage,
        technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
        severity = SeverityLevel.WARN
      )
    }

    return order.toPublicDto()
  }

  fun getOrderByIdForUser(orderId: String): OrderDto {
    val order = orderRepository.findById(orderId).orElseThrow {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = OrderFailureReason.NOT_FOUND.userMessage,
        technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
        severity = SeverityLevel.WARN
      )
    }

    // Validate ownership - users can only access their own orders
    AuthUtils.checkOwnership(order.userId)

    return order.toDto()
  }

  fun getOrderByIdInternal(orderId: String): OrderDto {
    val order = orderRepository.findById(orderId).orElseThrow {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = OrderFailureReason.NOT_FOUND.userMessage,
        technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
        severity = SeverityLevel.WARN
      )
    }
    return order.toDto()
  }

  @Transactional
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

  @Transactional
  fun placeOrder(orderId: String, request: PlaceOrderRequest) {
    val order = orderRepository.findById(orderId).orElseThrow {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = OrderFailureReason.NOT_FOUND.userMessage,
        technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
        severity = SeverityLevel.WARN
      )
    }

    // Validate order is in EMPTY status
    if (order.status != OrderStatus.EMPTY.name) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Order cannot be placed. Order status must be EMPTY.",
        technicalMessage = "Order $orderId has status ${order.status}, expected EMPTY",
        severity = SeverityLevel.WARN
      )
    }

    // Validate products are not empty
    if (request.products.isEmpty()) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Cannot place an order with no products",
        technicalMessage = "Order $orderId attempted to be placed with empty products list",
        severity = SeverityLevel.WARN
      )
    }

    // Fetch pickup location
    val pickupLocation = locationService.getLocationById(order.userId, request.pickupLocationId)

    // Calculate total price
    val totalPrice = request.products.fold(BigDecimal.ZERO) { sum, product ->
      sum + (product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))
    }

    // Update order
    val updatedOrder = order.copy(
      // User (pickup) location from selected location
      userStreetAddress = pickupLocation.streetAddress,
      userCity = pickupLocation.city,
      userPhoneNumber = pickupLocation.phoneNumber,
      // Customer data
      customerName = request.customerName,
      customerPhone = request.customerPhone,
      customerEmail = request.customerEmail,
      customerStreetAddress = request.customerStreetAddress,
      customerCity = request.customerCity,
      // Order details
      status = OrderStatus.PLACED.name,
      products = request.products,
      productsVersion = order.productsVersion,
      totalPrice = totalPrice,
      deliveryDate = request.deliveryDate,
      notes = request.notes,
      updatedAt = LocalDateTime.now()
    )

    orderRepository.save(updatedOrder)
  }

  @Transactional
  fun markOrderDone(orderId: String) {
    val order = orderRepository.findById(orderId).orElseThrow {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = OrderFailureReason.NOT_FOUND.userMessage,
        technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
        severity = SeverityLevel.WARN
      )
    }

    // Ensure user owns order
    AuthUtils.checkOwnership(order.userId)

    if (order.status != OrderStatus.PLACED.name) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Only placed orders can be marked as done",
        technicalMessage = "Order $orderId has status ${order.status}, expected PLACED",
        severity = SeverityLevel.WARN
      )
    }

    val updatedOrder = order.copy(
      status = OrderStatus.DONE.name,
      updatedAt = LocalDateTime.now()
    )

    orderRepository.save(updatedOrder)
  }

}
