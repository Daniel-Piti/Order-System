package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.constants.TaxConstants
import com.pt.ordersystem.ordersystem.domains.customer.CustomerRepository
import com.pt.ordersystem.ordersystem.domains.location.LocationRepository
import com.pt.ordersystem.ordersystem.domains.manager.ManagerRepository
import com.pt.ordersystem.ordersystem.domains.order.models.*
import com.pt.ordersystem.ordersystem.domains.product.ProductRepository
import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
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
  private val customerRepository: CustomerRepository,
  private val locationRepository: LocationRepository,
  private val managerRepository: ManagerRepository,
  private val productRepository: ProductRepository,
) {

  companion object {
    private const val MAX_PAGE_SIZE = 100
    private val ALLOWED_SORT_FIELDS = setOf("createdAt", "updatedAt", "placedAt", "doneAt", "totalPrice", "status")
    private const val DEFAULT_SORT_FIELD = "createdAt"
  }

  private fun resolveSortBy(sortBy: String): String =
    if (sortBy in ALLOWED_SORT_FIELDS) sortBy else DEFAULT_SORT_FIELD

  fun getOrders(
    managerId: String,
    page: Int,
    size: Int,
    sortBy: String,
    sortDirection: String,
    status: String?,
    filterAgent: Boolean,
    agentId: String?
  ): Page<OrderDto> {
    // Enforce max page size
    val validatedSize = size.coerceAtMost(MAX_PAGE_SIZE)

    // Create sort based on direction (whitelist sort field for security)
    val safeSortBy = resolveSortBy(sortBy)
    val sort = if (sortDirection.uppercase() == "DESC") {
      Sort.by(safeSortBy).descending()
    } else {
      Sort.by(safeSortBy).ascending()
    }

    // Create pageable with sort
    val pageable = PageRequest.of(page, validatedSize, sort)

    // Fetch orders with optional filters
    // filterAgent=true, agentId=null -> manager's orders (agentId IS NULL)
    // filterAgent=true, agentId=123 -> specific agent's orders
    // filterAgent=false -> no filter by agent (all orders)
    val selectedOrders = when {
      // Filter by agent AND status
      filterAgent && !status.isNullOrBlank() -> {
        if (agentId == null) {
          // Manager-only orders
          orderRepository.findAllByManagerIdAndAgentIdIsNullAndStatus(managerId, status, pageable)
        } else {
          // Specific agent orders
          orderRepository.findAllByManagerIdAndAgentIdAndStatus(managerId, agentId, status, pageable)
        }
      }
      // Filter by agent only (no status filter)
      filterAgent -> {
        if (agentId == null) {
          // Manager-only orders
          orderRepository.findAllByManagerIdAndAgentIdIsNull(managerId, pageable)
        } else {
          // Specific agent orders
          orderRepository.findAllByManagerIdAndAgentId(managerId, agentId, pageable)
        }
      }
      // Status filter only (no agent filter)
      !status.isNullOrBlank() -> {
        orderRepository.findAllByManagerIdAndStatus(managerId, status, pageable)
      }
      // No filters - return all orders for manager
      else -> {
        orderRepository.findAllByManagerId(managerId, pageable)
      }
    }

    return selectedOrders.map { it.toDto() }
  }

  fun getOrdersByCustomerId(
    managerId: String,
    customerId: String,
    page: Int,
    size: Int,
    sortBy: String,
    sortDirection: String,
    status: String?,
    agentId: String?
  ): Page<OrderDto> {
    // When agent scoped, ensure customer belongs to this agent (throws if not)
    if (agentId != null) {
      customerRepository.findByManagerIdAndAgentIdAndId(managerId, agentId, customerId)
    }
    val validatedSize = size.coerceAtMost(MAX_PAGE_SIZE)
    val safeSortBy = resolveSortBy(sortBy)
    val sort = if (sortDirection.uppercase() == "DESC") {
      Sort.by(safeSortBy).descending()
    } else {
      Sort.by(safeSortBy).ascending()
    }
    val pageable = PageRequest.of(page, validatedSize, sort)

    val selectedOrders = when {
      agentId != null && !status.isNullOrBlank() ->
        orderRepository.findAllByManagerIdAndAgentIdAndCustomerIdAndStatus(managerId, agentId, customerId, status, pageable)
      agentId != null ->
        orderRepository.findAllByManagerIdAndAgentIdAndCustomerId(managerId, agentId, customerId, pageable)
      !status.isNullOrBlank() ->
        orderRepository.findAllByManagerIdAndCustomerIdAndStatus(managerId, customerId, status, pageable)
      else ->
        orderRepository.findAllByManagerIdAndCustomerId(managerId, customerId, pageable)
    }
    return selectedOrders.map { it.toDto() }
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

  fun getOrderById(orderId: String, managerId: String, agentId: String? = null): OrderDto {
    val order = when (agentId) {
      null -> {
        // Manager query: find order by managerId (regardless of agentId)
        orderRepository.findByIdAndManagerId(id = orderId, managerId = managerId)
      }
      else -> {
        // Agent query: find order by managerId AND agentId
        orderRepository.findByIdAndManagerIdAndAgentId(id = orderId, managerId = managerId, agentId = agentId)
      }
    } ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = OrderFailureReason.NOT_FOUND.userMessage,
      technicalMessage = OrderFailureReason.NOT_FOUND.technical + 
        "orderId=$orderId, managerId=$managerId${if (agentId != null) ", agentId=$agentId" else ""}",
      severity = SeverityLevel.WARN
    )

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
  fun createOrder(managerId: String, agentId: String?, orderSource: OrderSource, request: CreateOrderRequest): String {
    
    // Validate manager has at least one location
    val locationCount = locationRepository.countByManagerId(managerId)
    if (locationCount == 0L) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = OrderFailureReason.NO_LOCATIONS.userMessage,
        technicalMessage = OrderFailureReason.NO_LOCATIONS.technical + "managerId=$managerId",
        severity = SeverityLevel.INFO
      )
    }

    // If customerId is provided, fetch customer data to pre-fill
    val customer = request.customerId?.let { customerId ->
      if (agentId == null) customerRepository.findByManagerIdAndId(managerId, customerId)
      else customerRepository.findByManagerIdAndAgentIdAndId(managerId, agentId, customerId)
    }

    val now = LocalDateTime.now()

    // Link expires in 7 days by default
    val linkExpiresAt = now.plusDays(7)

    val order = OrderDbEntity(
      id = GeneralUtils.genId(),
      managerId = managerId,
      agentId = agentId,
      orderSource = orderSource.name,
      // Store location (will be filled by customer when placing order)
      storeStreetAddress = null,
      storeCity = null,
      storePhoneNumber = null,
      // Customer data (pre-filled if linked, otherwise customer fills)
      customerId = customer?.id,
      customerName = customer?.name,
      customerPhone = customer?.phoneNumber,
      customerEmail = customer?.email,
      customerStreetAddress = customer?.streetAddress,
      customerCity = customer?.city,
      customerStateId = customer?.stateId,
      // Order details (empty, customer fills)
      status = OrderStatus.EMPTY.name,
      products = emptyList(),
      productsVersion = 1,
      totalPrice = BigDecimal.ZERO,
      discount = BigDecimal.ZERO,
      vat = TaxConstants.VAT_PERCENTAGE,
      linkExpiresAt = linkExpiresAt,
      notes = "",
      placedAt = null,
      doneAt = null,
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

    // Validate that all product prices are >= minimum price
    validateProductPrices(request.products, order.managerId)

    // Fetch pickup location
    val selectedLocation = locationRepository.findByManagerIdAndId(order.managerId, request.pickupLocationId)

    // Calculate total price
    val totalPrice = request.products.fold(BigDecimal.ZERO) { sum, product ->
      sum + (product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))
    }

    val customer = order.customerId?.let { customerId ->
      if (order.agentId == null) customerRepository.findByManagerIdAndId(order.managerId, customerId)
      else customerRepository.findByManagerIdAndAgentIdAndId(order.managerId, order.agentId, customerId)
    }

    // Update order
    val now = LocalDateTime.now()
    val updatedOrder = order.copy(
      // Store (pickup) location from selected location
      storeStreetAddress = selectedLocation.streetAddress,
      storeCity = selectedLocation.city,
      storePhoneNumber = selectedLocation.phoneNumber,
      // Customer data
      customerName = customer?.name ?: request.customerName,
      customerPhone = customer?.phoneNumber ?: request.customerPhone,
      customerEmail = customer?.email ?: request.customerEmail,
      customerStreetAddress = customer?.streetAddress ?: request.customerStreetAddress,
      customerCity = customer?.city ?: request.customerCity,
      customerStateId = customer?.stateId ?: request.customerStateId,
      // Order details
      status = OrderStatus.PLACED.name,
      products = request.products,
      productsVersion = order.productsVersion,
      totalPrice = totalPrice,
      vat = TaxConstants.VAT_PERCENTAGE,
      notes = request.notes,
      placedAt = now,
      doneAt = null,
      updatedAt = now
    )

    orderRepository.save(updatedOrder)
  }

  @Transactional
  fun createAndPlacePublicOrder(managerId: String, request: PlaceOrderRequest): String {
    // Validate manager exists
    managerRepository.findById(managerId)

    // Validate products are not empty
    if (request.products.isEmpty()) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Cannot place an order with no products",
        technicalMessage = "Public order attempted to be placed with empty products list for managerId=$managerId",
        severity = SeverityLevel.WARN
      )
    }

    // Validate that all product prices are >= minimum price
    validateProductPrices(request.products, managerId)

    // Validate and fetch pickup location (will throw exception if location doesn't exist or doesn't belong to manager)
    val selectedLocation = locationRepository.findByManagerIdAndId(managerId, request.pickupLocationId)

    // Calculate total price
    val totalPrice = request.products.fold(BigDecimal.ZERO) { sum, product ->
      sum + (product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))
    }

    val now = LocalDateTime.now()
    val linkExpiresAt = now.plusDays(7)

    // Create order with PUBLIC source, no agentId, no customerId
    val order = OrderDbEntity(
      id = GeneralUtils.genId(),
      managerId = managerId,
      agentId = null, // No agent for public orders
      orderSource = OrderSource.PUBLIC.name,
      // Store (pickup) location from selected location
      storeStreetAddress = selectedLocation.streetAddress,
      storeCity = selectedLocation.city,
      storePhoneNumber = selectedLocation.phoneNumber,
      // Customer data (from request, no customer linked)
      customerId = null, // No customer linked for public orders
      customerName = request.customerName,
      customerPhone = request.customerPhone,
      customerEmail = request.customerEmail,
      customerStreetAddress = request.customerStreetAddress,
      customerCity = request.customerCity,
      customerStateId = request.customerStateId,
      // Order details (placed immediately)
      status = OrderStatus.PLACED.name,
      products = request.products,
      productsVersion = 1,
      totalPrice = totalPrice,
      discount = BigDecimal.ZERO,
      vat = TaxConstants.VAT_PERCENTAGE,
      linkExpiresAt = linkExpiresAt,
      notes = request.notes,
      placedAt = now,
      doneAt = null,
      createdAt = now,
      updatedAt = now,
    )

    return orderRepository.save(order).id
  }

  @Transactional
  fun markOrderDone(orderId: String, managerId: String) {
    val order = orderRepository.findByIdAndManagerId(orderId, managerId) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = OrderFailureReason.NOT_FOUND.userMessage,
      technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId managerId=$managerId",
      severity = SeverityLevel.WARN
    )

    if (order.status != OrderStatus.PLACED.name) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Only placed orders can be marked as done",
        technicalMessage = "Order $orderId has status ${order.status}, expected PLACED",
        severity = SeverityLevel.WARN
      )
    }

    val now = LocalDateTime.now()
    val updatedOrder = order.copy(
      status = OrderStatus.DONE.name,
      doneAt = now,
      updatedAt = now
    )

    orderRepository.save(updatedOrder)
  }

  @Transactional
  fun updateOrder(orderId: String, managerId: String, agentId: String?, request: UpdateOrderRequest) {
    // Fetch order with permission validation
    val order = when (agentId) {
      null -> {
        // Manager can edit any order (check managerId matches)
        orderRepository.findByIdAndManagerId(id = orderId, managerId = managerId)
      }
      else -> {
        // Agent can edit only their orders (check managerId AND agentId match)
        orderRepository.findByIdAndManagerIdAndAgentId(id = orderId, managerId = managerId, agentId = agentId)
      }
    } ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = OrderFailureReason.NOT_FOUND.userMessage,
      technicalMessage = OrderFailureReason.NOT_FOUND.technical + 
        "orderId=$orderId, managerId=$managerId${if (agentId != null) ", agentId=$agentId" else ""}",
      severity = SeverityLevel.WARN
    )

    // Validate order is in PLACED status
    if (order.status != OrderStatus.PLACED.name) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Only placed orders can be edited",
        technicalMessage = "Order $orderId has status ${order.status}, expected PLACED",
        severity = SeverityLevel.WARN
      )
    }

    // Validate products are not empty
    if (request.products.isEmpty()) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Cannot update an order with no products",
        technicalMessage = "Order $orderId attempted to be updated with empty products list",
        severity = SeverityLevel.WARN
      )
    }

    // Validate that all product prices are >= minimum price
    validateProductPrices(request.products, managerId)

    // Validate and fetch pickup location (will throw exception if location doesn't exist or doesn't belong to manager)
    val selectedLocation = locationRepository.findByManagerIdAndId(managerId, request.pickupLocationId)

    val productsTotal = request.products.fold(BigDecimal.ZERO) { sum, product ->
      sum + (product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))
    }
    val totalPrice = productsTotal.subtract(order.discount).max(BigDecimal.ZERO)

    // Update order (keep customer info unchanged, keep productsVersion unchanged)
    val now = LocalDateTime.now()
    val updatedOrder = order.copy(
      // Store (pickup) location from selected location
      storeStreetAddress = selectedLocation.streetAddress,
      storeCity = selectedLocation.city,
      storePhoneNumber = selectedLocation.phoneNumber,
      // Customer data - KEEP UNCHANGED (order already placed)
      // Order details - UPDATE products, notes, totalPrice
      products = request.products,
      totalPrice = totalPrice,
      notes = request.notes,
      // Keep productsVersion unchanged (has different purpose)
      updatedAt = now
    )

    orderRepository.save(updatedOrder)
  }

  @Transactional
  fun cancelOrder(orderId: String, managerId: String, agentId: String? = null) {
    val order = when (agentId) {
      null -> {
        // Manager query: find order by managerId (regardless of agentId)
        orderRepository.findByIdAndManagerId(id = orderId, managerId = managerId)
      }
      else -> {
        // Agent query: find order by managerId AND agentId
        orderRepository.findByIdAndManagerIdAndAgentId(id = orderId, managerId = managerId, agentId = agentId)
      }
    } ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = OrderFailureReason.NOT_FOUND.userMessage,
      technicalMessage = OrderFailureReason.NOT_FOUND.technical + 
        "orderId=$orderId, managerId=$managerId${if (agentId != null) ", agentId=$agentId" else ""}",
      severity = SeverityLevel.WARN
    )

    val cancellableStatuses = setOf(OrderStatus.PLACED.name, OrderStatus.EMPTY.name)

    if (order.status !in cancellableStatuses) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Only placed or empty orders can be cancelled",
        technicalMessage = "Order $orderId has status ${order.status}, expected $cancellableStatuses",
        severity = SeverityLevel.WARN
      )
    }

    val updatedOrder = order.copy(
      status = OrderStatus.CANCELLED.name,
      updatedAt = LocalDateTime.now()
    )

    orderRepository.save(updatedOrder)
  }

  @Transactional
  fun updateOrderDiscount(orderId: String, managerId: String, agentId: String?, request: UpdateDiscountRequest) {
    // Fetch order with permission validation
    val order = when (agentId) {
      null -> {
        // Manager can edit any order (check managerId matches)
        orderRepository.findByIdAndManagerId(id = orderId, managerId = managerId)
      }
      else -> {
        // Agent can edit only their orders (check managerId AND agentId match)
        orderRepository.findByIdAndManagerIdAndAgentId(id = orderId, managerId = managerId, agentId = agentId)
      }
    } ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = OrderFailureReason.NOT_FOUND.userMessage,
      technicalMessage = OrderFailureReason.NOT_FOUND.technical + 
        "orderId=$orderId, managerId=$managerId${if (agentId != null) ", agentId=$agentId" else ""}",
      severity = SeverityLevel.WARN
    )

    // Validate order is in PLACED status
    if (order.status != OrderStatus.PLACED.name) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Only placed orders can have discount updated",
        technicalMessage = "Order $orderId has status ${order.status}, expected PLACED",
        severity = SeverityLevel.WARN
      )
    }

    // Validate discount >= 0 and max 2 decimal places
    FieldValidators.validateNonNegative(request.discount, "Discount")
    FieldValidators.validatePriceDecimalPlaces(request.discount, "Discount")

    // Calculate products total
    val productsTotal = order.products.fold(BigDecimal.ZERO) { sum, product ->
      sum + (product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))
    }

    // Validate discount doesn't exceed products total
    if (request.discount > productsTotal) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Discount cannot exceed the total price of products",
        technicalMessage = "Order $orderId attempted to set discount ${request.discount} which exceeds products total $productsTotal",
        severity = SeverityLevel.WARN
      )
    }

    // Calculate new total price (products total - discount)
    val newTotalPrice = productsTotal.subtract(request.discount).max(BigDecimal.ZERO)

    // Update order with new discount and recalculated total price
    val now = LocalDateTime.now()
    val updatedOrder = order.copy(
      discount = request.discount,
      totalPrice = newTotalPrice,
      updatedAt = now
    )

    orderRepository.save(updatedOrder)
  }

  private fun validateProductPrices(products: List<ProductDataForOrder>, managerId: String) {
    if (products.isEmpty()) {
      return // Empty products validation is handled separately
    }

    val productIds = products.map { it.productId }.distinct()
    val productEntities = productRepository.findAllById(productIds)
      .filter { it.managerId == managerId }

    val invalidProducts = products.filter { orderProduct ->
      val product = productEntities.find { it.id == orderProduct.productId }
      product != null && orderProduct.pricePerUnit < product.minimumPrice
    }

    if (invalidProducts.isNotEmpty()) {
      val productNames = invalidProducts.joinToString(", ") { it.productName }
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "The following products have prices below their minimum price: $productNames",
        technicalMessage = "Products below minimum price for managerId=$managerId: ${invalidProducts.map { "${it.productName} (price=${it.pricePerUnit})" }}",
        severity = SeverityLevel.WARN
      )
    }
  }

}
