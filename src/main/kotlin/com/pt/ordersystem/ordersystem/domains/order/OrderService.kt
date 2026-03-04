package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.constants.TaxConstants
import com.pt.ordersystem.ordersystem.domains.customer.CustomerRepository
import com.pt.ordersystem.ordersystem.domains.location.LocationRepository
import com.pt.ordersystem.ordersystem.domains.location.helpers.LocationValidators
import com.pt.ordersystem.ordersystem.domains.manager.ManagerRepository
import com.pt.ordersystem.ordersystem.domains.order.helpers.OrderValidators
import com.pt.ordersystem.ordersystem.domains.order.models.*
import com.pt.ordersystem.ordersystem.domains.product.ProductRepository
import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import com.pt.ordersystem.ordersystem.utils.PageRequestBase
import com.pt.ordersystem.ordersystem.utils.PaginationUtils
import com.pt.ordersystem.ordersystem.utils.SortOrder
import org.springframework.data.domain.Page
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
  fun createOrder(managerId: String, agentId: String?, orderSource: OrderSource, request: CreateOrderRequest): String {
    
    // Validate manager has at least one location
    val locationCount = locationRepository.countByManagerId(managerId)
    LocationValidators.validateMinLocationCount(locationCount, managerId)

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
    val order = orderRepository.findById(orderId)

    OrderValidators.validateOrderStatus(order.status, OrderStatus.EMPTY, orderId)
    OrderValidators.validateOrderProductsNotEmpty(request.products, orderId)

    validateProductPrices(request.products, order.managerId)

    val selectedLocation = locationRepository.findByManagerIdAndId(order.managerId, request.pickupLocationId)

    val totalPrice = request.products.fold(BigDecimal.ZERO) { sum, product ->
      sum + (product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))
    }

    val customer = order.customerId?.let { customerId ->
      if (order.agentId == null) customerRepository.findByManagerIdAndId(order.managerId, customerId)
      else customerRepository.findByManagerIdAndAgentIdAndId(order.managerId, order.agentId, customerId)
    }

    val now = LocalDateTime.now()
    val updatedEntity = order.toEntity().copy(
      storeStreetAddress = selectedLocation.streetAddress,
      storeCity = selectedLocation.city,
      storePhoneNumber = selectedLocation.phoneNumber,
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
    // Validate manager exists
    managerRepository.findById(managerId)

    // Validate products are not empty
    OrderValidators.validateOrderProductsNotEmpty(request.products)

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
  fun updateOrder(orderId: String, managerId: String, agentId: String?, request: UpdateOrderRequest) {
    val order = orderRepository.findByIdAndManagerIdAndAgentId(orderId, managerId, agentId)

    OrderValidators.validateOrderStatus(order.status, OrderStatus.PLACED, orderId)
    OrderValidators.validateOrderProductsNotEmpty(request.products, orderId)
    validateProductPrices(request.products, managerId)

    // Validate and fetch pickup location (will throw exception if location doesn't exist or doesn't belong to manager)
    val selectedLocation = locationRepository.findByManagerIdAndId(managerId, request.pickupLocationId)

    val productsTotal = request.products.fold(BigDecimal.ZERO) { sum, product ->
      sum + (product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))
    }
    val totalPrice = productsTotal.subtract(order.discount).max(BigDecimal.ZERO)

    val now = LocalDateTime.now()
    val updatedEntity = order.toEntity().copy(
      storeStreetAddress = selectedLocation.streetAddress,
      storeCity = selectedLocation.city,
      storePhoneNumber = selectedLocation.phoneNumber,
      products = request.products,
      totalPrice = totalPrice,
      notes = request.notes,
      updatedAt = now
    )

    orderRepository.save(updatedEntity)
  }

  @Transactional
  fun cancelOrder(orderId: String, managerId: String, agentId: String? = null) {
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

    orderRepository.save(updatedEntity)
  }

  @Transactional
  fun updateOrderDiscount(orderId: String, managerId: String, agentId: String?, request: UpdateDiscountRequest) {
    val order = orderRepository.findByIdAndManagerIdAndAgentId(orderId, managerId, agentId)

    OrderValidators.validateOrderStatus(order.status, OrderStatus.PLACED, orderId)

    // Validate discount >= 0 and max 2 decimal places
    FieldValidators.validateNonNegative(request.discount, "Discount")
    FieldValidators.validatePriceDecimalPlaces(request.discount, "Discount")

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

    orderRepository.save(updatedEntity)
  }

  private fun validateProductPrices(products: List<ProductDataForOrder>, managerId: String) {
    if (products.isEmpty()) return

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
