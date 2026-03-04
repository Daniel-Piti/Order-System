package com.pt.ordersystem.ordersystem.domains.order.helpers

import com.pt.ordersystem.ordersystem.domains.order.models.OrderStatus
import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import java.math.BigDecimal

object OrderValidators {

    fun validateOrderProductsNotEmpty(products: List<ProductDataForOrder>, orderId: String? = null) {
        if (products.isEmpty()) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = "Cannot place an order with no products",
                technicalMessage = "Order $orderId attempted to be placed with empty products list",
                severity = SeverityLevel.WARN
            )
        }
    }

    fun validateTotalPriceAfterDiscount(totalPrice: BigDecimal) {
        if (totalPrice < BigDecimal(0)) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = "Discount cannot exceed the total price of products",
                technicalMessage = "Minimum total price is 0",
                severity = SeverityLevel.WARN
            )
        }
    }

    fun validateOrderStatus(orderStatus: OrderStatus, expectedStatus: OrderStatus, orderId: String) {
        if (orderStatus != expectedStatus) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = "Order status required to be $expectedStatus was $orderStatus",
                technicalMessage = "Order $orderId has status $orderStatus, expected $expectedStatus",
                severity = SeverityLevel.WARN
            )
        }
    }

    fun validateOrderStatusIn(orderStatus: OrderStatus, allowedStatuses: Set<OrderStatus>, orderId: String, userMessage: String) {
        if (orderStatus !in allowedStatuses) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = userMessage,
                technicalMessage = "Order $orderId has status $orderStatus, expected one of $allowedStatuses",
                severity = SeverityLevel.WARN
            )
        }
    }

}