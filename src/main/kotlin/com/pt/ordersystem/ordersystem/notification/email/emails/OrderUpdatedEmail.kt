package com.pt.ordersystem.ordersystem.notification.email.emails

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDto
import com.pt.ordersystem.ordersystem.domains.order.models.OrderStatus
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.notification.email.models.EmailNotificationFailureReason
import org.springframework.http.HttpStatus

object OrderUpdatedEmail {
  private const val TEMPLATE_NAME = "order-updated.html"

  fun buildSubject(order: OrderDto): String = "Order Updated - Order #${order.id}"

  fun validate(order: OrderDto) {
    // Updated orders should still be in PLACED status
    if (order.status != OrderStatus.PLACED) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = EmailNotificationFailureReason.ORDER_NOT_PLACED.userMessage,
        technicalMessage = EmailNotificationFailureReason.ORDER_NOT_PLACED.technical + "orderId=${order.id} status=${order.status}",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun buildHtml(order: OrderDto): String {
    val template = EmailTemplateHelper.loadTemplate(TEMPLATE_NAME)
    val placedAtRow = order.placedAt?.let {
      val formatted = EmailTemplateHelper.formatDateTime(it)
      "<div class=\"summary-row\"><span>Originally Placed:&nbsp;</span><span>$formatted</span></div>"
    } ?: ""

    val data = mapOf(
      "customerName" to EmailTemplateHelper.escapeHtml(EmailTemplateHelper.getCustomerName(order)),
      "orderId" to order.id,
      "updatedAt" to EmailTemplateHelper.formatDateTime(order.updatedAt),
      "placedAtRow" to placedAtRow,
      "formattedTotal" to EmailTemplateHelper.formatCurrency(order.totalPrice),
      "productList" to EmailTemplateHelper.buildProductList(order, "#d97706", "#fde68a")
    )
    return EmailTemplateHelper.replacePlaceholders(template, data)
  }
}

