package com.pt.ordersystem.ordersystem.domains.invoices

import com.pt.ordersystem.ordersystem.domains.invoices.models.PaymentMethod
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import com.pt.ordersystem.ordersystem.domains.order.models.OrderStatus
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.http.HttpStatus
import java.math.BigDecimal

object InvoiceHelper {

  fun validateOrderEligibilityForInvoice(order: OrderDbEntity) {
    val status = OrderStatus.valueOf(order.status)
    if (status != OrderStatus.DONE) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Invoice is only available for completed orders",
        technicalMessage = "Invoice requested for order ${order.id} with status ${order.status}",
        severity = SeverityLevel.INFO
      )
    }

    if (order.products.isEmpty()) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Cannot create invoice for order without products",
        technicalMessage = "Invoice requested for order ${order.id} with empty products list",
        severity = SeverityLevel.INFO
      )
    }
  }

  fun validateAndPrepareAllocationNumber(order: OrderDbEntity, allocationNumber: String?): String? {
    val needAllocationNumber = isRequiredAllocationNumber(order)
    return if (needAllocationNumber) {
      if (allocationNumber.isNullOrBlank()) {
        throw ServiceException(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "Allocation number is required for invoice creation for this order",
          technicalMessage = "Allocation number required for order ${order.id} with total price ${order.totalPrice}",
          severity = SeverityLevel.INFO
        )
      }
      FieldValidators.validateNumericString(allocationNumber, 9, "Allocation number")
      allocationNumber
    } else null
  }

  private fun isRequiredAllocationNumber(order: OrderDbEntity): Boolean {
    val totalPrice = order.totalPrice
    val orderDate = order.doneAt!!
    val year = orderDate.year
    val month = orderDate.monthValue

    val threshold = when {
      year < 2026 -> BigDecimal("10000")
      year == 2026 && month < 6 -> BigDecimal("10000")
      else -> BigDecimal("5000")
    }

    return totalPrice >= threshold
  }

  fun validatePaymentMethodAndProof(paymentMethod: PaymentMethod, paymentProof: String) {

    when (paymentMethod) {
      PaymentMethod.CREDIT_CARD -> FieldValidators.validateNumericString(paymentProof, 4, "Allocation number")
      PaymentMethod.CASH -> {
        // Cash proof is just a string - no specific validation needed
      }
    }
  }
}