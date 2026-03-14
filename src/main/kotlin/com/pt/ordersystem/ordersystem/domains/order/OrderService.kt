package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.constants.TaxConstants
import com.pt.ordersystem.ordersystem.domains.customer.CustomerRepository
import com.pt.ordersystem.ordersystem.domains.location.LocationRepository
import com.pt.ordersystem.ordersystem.domains.order.helpers.OrderValidators
import com.pt.ordersystem.ordersystem.domains.order.models.*
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import com.pt.ordersystem.ordersystem.utils.PageRequestBase
import com.pt.ordersystem.ordersystem.utils.PaginationUtils
import com.pt.ordersystem.ordersystem.utils.SortOrder
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class OrderService(
  private val orderRepository: OrderRepository,
  private val customerRepository: CustomerRepository,
  private val locationRepository: LocationRepository,
  private val orderValidationService: OrderValidationService,
) {

  companion object {
    private val ORDER_ALLOWED_SORT_FIELDS = setOf("createdAt", "updatedAt", "placedAt", "doneAt", "totalPrice", "status")
    private const val ORDER_DEFAULT_SORT_FIELD = "createdAt"
    private const val ORDER_MAX_PAGE_SIZE = 100
  }

  fun getOrders(
    managerId: String,
    page: Int,
    pageSize: Int,
    sortBy: String,
    sortDirection: String,
    status: String?,
    filterAgent: Boolean,
    agentId: String?
  ): Page<Order> {
    val pageable = PaginationUtils.getValidatedPageRequest(
      PageRequestBase(page, pageSize, sortBy, SortOrder.fromString(sortDirection)),
      allowedSortFields = ORDER_ALLOWED_SORT_FIELDS,
      defaultSortBy = ORDER_DEFAULT_SORT_FIELD,
      maxPageSize = ORDER_MAX_PAGE_SIZE,
    )

    // Fetch orders with optional filters
    // filterAgent=true, agentId=null -> manager's orders (agentId IS NULL)
    // filterAgent=true, agentId=123 -> specific agent's orders
    // filterAgent=false -> no filter by agent (all orders)
    return when {
      filterAgent && !status.isNullOrBlank() -> {
        if (agentId == null) {
          orderRepository.findAllByManagerIdAndAgentIdIsNullAndStatus(managerId, status, pageable)
        } else {
          orderRepository.findAllByManagerIdAndAgentIdAndStatus(managerId, agentId, status, pageable)
        }
      }
      filterAgent -> {
        if (agentId == null) {
          orderRepository.findAllByManagerIdAndAgentIdIsNull(managerId, pageable)
        } else {
          orderRepository.findAllByManagerIdAndAgentId(managerId, agentId, pageable)
        }
      }
      !status.isNullOrBlank() -> {
        orderRepository.findAllByManagerIdAndStatus(managerId, status, pageable)
      }
      else -> {
        orderRepository.findAllByManagerId(managerId, pageable)
      }
    }
  }

  fun getOrdersByCustomerId(
    managerId: String,
    customerId: String,
    page: Int,
    pageSize: Int,
    sortBy: String,
    sortDirection: String,
    status: String?,
    agentId: String?
  ): Page<Order> {
    // When agent scoped, ensure customer belongs to this agent (throws if not)
    if (agentId != null) {
      customerRepository.findByManagerIdAndAgentIdAndId(managerId, agentId, customerId)
    }
    val pageable = PaginationUtils.getValidatedPageRequest(
      PageRequestBase(page, pageSize, sortBy, SortOrder.fromString(sortDirection)),
      allowedSortFields = ORDER_ALLOWED_SORT_FIELDS,
      defaultSortBy = ORDER_DEFAULT_SORT_FIELD,
      maxPageSize = ORDER_MAX_PAGE_SIZE,
    )

    return when {
      agentId != null && !status.isNullOrBlank() ->
        orderRepository.findAllByManagerIdAndAgentIdAndCustomerIdAndStatus(managerId, agentId, customerId, status, pageable)
      agentId != null ->
        orderRepository.findAllByManagerIdAndAgentIdAndCustomerId(managerId, agentId, customerId, pageable)
      !status.isNullOrBlank() ->
        orderRepository.findAllByManagerIdAndCustomerIdAndStatus(managerId, customerId, status, pageable)
      else ->
        orderRepository.findAllByManagerIdAndCustomerId(managerId, customerId, pageable)
    }
  }

  fun getOrderById(orderId: String, managerId: String, agentId: String? = null): Order =
    orderRepository.findByIdAndManagerIdAndAgentId(orderId, managerId, agentId)

  fun getOrderById(orderId: String): Order = orderRepository.findById(orderId)

  @Transactional
  fun createOrder(managerId: String, agentId: String?, orderSource: OrderSource, request: CreateOrderRequest): Order {

    orderValidationService.validateCreateOrder(managerId)

    val customer = request.customerId?.let { customerId ->
      customerRepository.findByManagerIdAndAgentIdAndId(managerId, agentId, customerId)
    }

    val now = LocalDateTime.now()

    // Link expires in 7 days by default
    val linkExpiresAt = now.plusDays(7)

    val order = OrderDbEntity(
      id = GeneralUtils.genId(),
      managerId = managerId,
      agentId = agentId,
      orderSource = orderSource.name,
      // Selected location
      selectedLocation = null,
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

    return orderRepository.save(order)
  }

  @Transactional
  fun placeOrder(orderId: String, request: PlaceOrderRequest) {
    val order = orderRepository.findById(orderId)

    orderValidationService.validatePlaceOrder(order, request)

    val selectedLocation = locationRepository.findByManagerIdAndId(order.managerId, request.pickupLocationId)

    val totalPrice = request.products.fold(BigDecimal.ZERO) { sum, product ->
      sum + (product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))
    }

    val customer = order.customerId?.let { customerId ->
      customerRepository.findByManagerIdAndAgentIdAndId(order.managerId, order.agentId, customerId)
    }

    val now = LocalDateTime.now()
    val updatedEntity = order.toEntity().copy(
      selectedLocation = SelectedLocation(
        locationId = selectedLocation.id,
        name = selectedLocation.name,
        streetAddress = selectedLocation.streetAddress,
        city = selectedLocation.city,
        phoneNumber = selectedLocation.phoneNumber,
      ),
      customerName = customer?.name ?: request.customerName,
      customerPhone = customer?.phoneNumber ?: request.customerPhone,
      customerEmail = customer?.email ?: request.customerEmail,
      customerStreetAddress = customer?.streetAddress ?: request.customerStreetAddress,
      customerCity = customer?.city ?: request.customerCity,
      customerStateId = customer?.stateId ?: request.customerStateId,
      status = OrderStatus.PLACED.name,
      products = request.products,
      totalPrice = totalPrice,
      vat = TaxConstants.VAT_PERCENTAGE,
      notes = request.notes,
      placedAt = now,
      doneAt = null,
      updatedAt = now
    )

    orderRepository.save(updatedEntity)
  }

  @Transactional
  fun createAndPlacePublicOrder(managerId: String, request: PlaceOrderRequest): String {
    orderValidationService.validateCreateAndPlacePublicOrder(managerId, request)

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
      // Selected location data
      selectedLocation = SelectedLocation(
        locationId = selectedLocation.id,
        name = selectedLocation.name,
        streetAddress = selectedLocation.streetAddress,
        city = selectedLocation.city,
        phoneNumber = selectedLocation.phoneNumber,
      ),
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
    val order = orderRepository.findByIdAndManagerIdAndAgentId(orderId, managerId, null)

    OrderValidators.validateOrderStatus(order.status, OrderStatus.PLACED, orderId)

    val now = LocalDateTime.now()
    val updatedEntity = order.toEntity().copy(
      status = OrderStatus.DONE.name,
      doneAt = now,
      updatedAt = now
    )

    orderRepository.save(updatedEntity)
  }

  @Transactional
  fun updateOrder(orderId: String, managerId: String, agentId: String?, updateOrderRequest: UpdateOrderRequest): Order {
    val order = orderRepository.findByIdAndManagerIdAndAgentId(orderId, managerId, agentId)

    orderValidationService.validateUpdateOrder(managerId, order, updateOrderRequest)

    val selectedLocation = locationRepository.findByManagerIdAndId(managerId, updateOrderRequest.pickupLocationId)

    val productsTotal = updateOrderRequest.products.fold(BigDecimal.ZERO) { sum, product ->
      sum + (product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))
    }
    val totalPrice = productsTotal.subtract(order.discount).max(BigDecimal.ZERO)

    val now = LocalDateTime.now()
    val updatedEntity = order.toEntity().copy(
      selectedLocation = SelectedLocation(
        locationId = selectedLocation.id,
        name = selectedLocation.name,
        streetAddress = selectedLocation.streetAddress,
        city = selectedLocation.city,
        phoneNumber = selectedLocation.phoneNumber,
      ),
      products = updateOrderRequest.products,
      totalPrice = totalPrice,
      notes = updateOrderRequest.notes,
      updatedAt = now
    )

    return orderRepository.save(updatedEntity)
  }

  @Transactional
  fun cancelOrder(orderId: String, managerId: String, agentId: String? = null): Order {
    val order = orderRepository.findByIdAndManagerIdAndAgentId(orderId, managerId, agentId)

    OrderValidators.validateOrderStatusIn(
        orderStatus = order.status,
        allowedStatuses = setOf(OrderStatus.PLACED, OrderStatus.EMPTY),
        orderId = orderId,
        userMessage = "Only placed or empty orders can be cancelled",
    )

    val updatedEntity = order.toEntity().copy(
      status = OrderStatus.CANCELLED.name,
      updatedAt = LocalDateTime.now()
    )

    return orderRepository.save(updatedEntity)
  }

  @Transactional
  fun updateOrderDiscount(orderId: String, managerId: String, agentId: String?, request: UpdateDiscountRequest): Order {
    val order = orderRepository.findByIdAndManagerIdAndAgentId(orderId, managerId, agentId)

    orderValidationService.validateUpdateOrderDiscount(order, request)

    // Calculate products total
    val productsTotal = order.products.fold(BigDecimal.ZERO) { sum, product ->
      sum + (product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))
    }

    // Validate discount doesn't exceed products total
    OrderValidators.validateTotalPriceAfterDiscount(productsTotal - request.discount)

    val newTotalPrice = productsTotal.subtract(request.discount).max(BigDecimal.ZERO)

    val now = LocalDateTime.now()
    val updatedEntity = order.toEntity().copy(
      discount = request.discount,
      totalPrice = newTotalPrice,
      updatedAt = now
    )

    return orderRepository.save(updatedEntity)
  }

}
