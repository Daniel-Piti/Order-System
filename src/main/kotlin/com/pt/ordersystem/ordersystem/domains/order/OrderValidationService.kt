package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.location.LocationRepository
import com.pt.ordersystem.ordersystem.domains.location.helpers.LocationValidators
import com.pt.ordersystem.ordersystem.domains.manager.ManagerRepository
import com.pt.ordersystem.ordersystem.domains.order.helpers.OrderValidators
import com.pt.ordersystem.ordersystem.domains.order.models.Order
import com.pt.ordersystem.ordersystem.domains.order.models.OrderStatus
import com.pt.ordersystem.ordersystem.domains.order.models.PlaceOrderRequest
import com.pt.ordersystem.ordersystem.domains.order.models.UpdateDiscountRequest
import com.pt.ordersystem.ordersystem.domains.order.models.UpdateOrderRequest
import com.pt.ordersystem.ordersystem.domains.product.ProductRepository
import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class OrderValidationService(
    private val productRepository: ProductRepository,
    private val locationRepository: LocationRepository,
    private val managerRepository: ManagerRepository,
) {

    fun validateProductPrices(products: List<ProductDataForOrder>, managerId: String) {
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

    fun validateCreateOrder(managerId: String) {
        val locationCount = locationRepository.countByManagerId(managerId)
        LocationValidators.validateMinLocationCount(locationCount, managerId)
    }

    fun validatePlaceOrder(order: Order, request: PlaceOrderRequest) {
        OrderValidators.validateOrderStatus(order.status, OrderStatus.EMPTY, order.id)
        OrderValidators.validateOrderProductsNotEmpty(request.products, order.id)
        validateProductPrices(request.products, order.managerId)
    }

    fun validateCreateAndPlacePublicOrder(managerId: String, request: PlaceOrderRequest) {
        managerRepository.findById(managerId)
        OrderValidators.validateOrderProductsNotEmpty(request.products)

        // Validate that all product prices are >= minimum price
        validateProductPrices(request.products, managerId)
    }

    fun validateUpdateOrder(managerId: String, order: Order, updateOrderRequest: UpdateOrderRequest) {

        OrderValidators.validateOrderStatus(order.status, OrderStatus.PLACED, order.id)
        OrderValidators.validateOrderProductsNotEmpty(updateOrderRequest.products, order.id)
        validateProductPrices(updateOrderRequest.products, managerId)

    }

    fun validateUpdateOrderDiscount(order: Order, request: UpdateDiscountRequest) {
        OrderValidators.validateOrderStatus(order.status, OrderStatus.PLACED, order.id)

        // Validate discount >= 0 and max 2 decimal places
        FieldValidators.validateNonNegative(request.discount, "Discount")
        FieldValidators.validatePriceDecimalPlaces(request.discount, "Discount")
    }
}
