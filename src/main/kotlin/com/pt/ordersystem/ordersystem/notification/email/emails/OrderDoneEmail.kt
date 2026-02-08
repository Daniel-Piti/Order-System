package com.pt.ordersystem.ordersystem.notification.email.emails

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDto
import com.pt.ordersystem.ordersystem.domains.order.models.OrderStatus
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.notification.email.models.EmailNotificationFailureReason
import org.springframework.http.HttpStatus

object OrderDoneEmail {
  private const val TEMPLATE_NAME = "order-done.html"

  fun buildSubject(order: OrderDto): String = "Order Completed - Order #${order.id}"

  fun validate(order: OrderDto) {
    if (order.status != OrderStatus.DONE) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = EmailNotificationFailureReason.ORDER_NOT_DONE.userMessage,
        technicalMessage = EmailNotificationFailureReason.ORDER_NOT_DONE.technical + "orderId=${order.id} status=${order.status}",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun buildHtml(order: OrderDto): String {
    val template = EmailTemplateHelper.loadTemplate(TEMPLATE_NAME)
    val data = mapOf(
      "customerName" to EmailTemplateHelper.escapeHtml(EmailTemplateHelper.getCustomerName(order)),
      "orderId" to order.id,
      "completedAt" to EmailTemplateHelper.formatDateTime(order.doneAt!!),
      "formattedTotal" to EmailTemplateHelper.formatCurrency(order.totalPrice),
      "productList" to EmailTemplateHelper.buildProductList(order, "#15803d", "#86efac")
    )
    return EmailTemplateHelper.replacePlaceholders(template, data)
  }
}

