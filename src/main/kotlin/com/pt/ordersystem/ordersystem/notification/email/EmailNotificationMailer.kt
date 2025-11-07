package com.pt.ordersystem.ordersystem.notification.email

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDto
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Service
class EmailNotificationMailer(
  private val mailSender: JavaMailSender,
) {

  @Value("\${spring.mail.from-email}")
  private lateinit var fromEmail: String

  private val logger = LoggerFactory.getLogger(EmailNotificationService::class.java)
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm")

  fun sendOrderPlacedEmail(order: OrderDto, recipientEmail: String) {
    try {
      val message: MimeMessage = mailSender.createMimeMessage()
      val helper = MimeMessageHelper(message, true, "UTF-8")

      helper.setTo(recipientEmail)
      helper.setSubject("Order Placed - Order #${order.id}")
      helper.setFrom(fromEmail)
      helper.setText(buildOrderPlacedBody(order), true) // true = HTML

      mailSender.send(message)

      logger.info(
        "Order placed email sent successfully | orderId={} | customer={} | to={} | total={}",
        order.id,
        order.customerName ?: "unknown",
        recipientEmail,
        order.totalPrice
      )
    } catch (e: Exception) {
      logger.error(
        "Failed to send order placed email | orderId={} | to={} | error={}",
        order.id,
        recipientEmail,
        e.message,
        e
      )
      throw e
    }
  }

  fun sendOrderDoneEmail(order: OrderDto, recipientEmail: String) {
    try {
      val message: MimeMessage = mailSender.createMimeMessage()
      val helper = MimeMessageHelper(message, true, "UTF-8")

      helper.setTo(recipientEmail)
      helper.setSubject("Order Completed - Order #${order.id}")
      helper.setFrom(fromEmail)
      helper.setText(buildOrderDoneBody(order), true)

      mailSender.send(message)

      logger.info(
        "Order done email sent successfully | orderId={} | customer={} | to={} | total={}",
        order.id,
        order.customerName ?: "unknown",
        recipientEmail,
        order.totalPrice
      )
    } catch (e: Exception) {
      logger.error(
        "Failed to send order done email | orderId={} | to={} | error={}",
        order.id,
        recipientEmail,
        e.message,
        e
      )
      throw e
    }
  }

  private fun buildOrderPlacedBody(order: OrderDto): String {
    val locale = Locale.Builder().setLanguage("en").setRegion("IL").build()
    val currencyFormatter = NumberFormat.getCurrencyInstance(locale)
    val formattedTotal = currencyFormatter.format(order.totalPrice)
    val placedAt = formatDateTime(order.createdAt)

    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <style>
          body { font-family: 'Inter', Arial, sans-serif; background-color: #ede9fe; padding: 32px 12px; color: #1f2937; }
          .wrapper { max-width: 560px; margin: 0 auto; }
          .card { background: #f5f3ff; border-radius: 18px; border: 1px solid #c7d2fe; box-shadow: 0 18px 36px rgba(79, 70, 229, 0.12); overflow: hidden; }
          .card-header { padding: 28px 32px 18px; background: linear-gradient(135deg, #4338ca, #6366f1); color: #ffffff; }
          .card-header h2 { margin: 0; font-size: 22px; font-weight: 600; }
          .card-header p { margin: 6px 0 0; font-size: 14px; opacity: 0.85; }
          .card-body { padding: 26px 32px 32px; }
          .summary { border: 1px solid #e5e7eb; border-radius: 16px; padding: 18px 20px; background: #f9fafb; margin-top: 20px; }
          .summary-row { display: flex; justify-content: space-between; font-size: 14px; padding: 6px 0; }
          .summary-row span:first-child { color: #6b7280; }
          .summary-row span:last-child { font-weight: 600; color: #111827; margin-left: 8px; }
          .total { margin-top: 18px; text-align: center; font-size: 18px; font-weight: 600; color: #4338ca; }
          .products { margin-top: 26px; border: 1px solid #c7d2fe; border-radius: 16px; padding: 18px 20px; background: #ffffff; }
          .products-title { font-size: 12px; text-transform: uppercase; letter-spacing: 0.08em; color: #6366f1; font-weight: 700; }
          .products-body { margin-top: 12px; }
          .product-row { padding: 16px 0; border-top: 1px solid #e5e7eb; }
          .product-row:first-child { border-top: none; padding-top: 12px; }
          .product-name { font-weight: 600; color: #111827; font-size: 15px; }
          .product-meta { color: #6b7280; font-size: 12px; margin-top: 6px; display: block; }
          .product-total { margin-top: 8px; font-weight: 700; color: #4338ca; font-size: 16px; display: block; text-align: left; }
          .products-empty { margin-top: 26px; border: 1px solid #e5e7eb; border-radius: 16px; background: #ffffff; padding: 18px 20px; text-align: center; font-size: 13px; color: #6b7280; }
          .footer { padding: 20px 32px 24px; text-align: center; font-size: 12px; color: #6b7280; }
        </style>
      </head>
      <body>
        <div class="wrapper">
          <div class="card">
            <div class="card-header">
              <h2>Your order has been placed</h2>
              <p>We received your request and will keep you posted.</p>
            </div>
            <div class="card-body">
              <p style="margin: 0 0 14px; font-size: 15px;">Hello ${order.customerName ?: "Customer"},</p>
              <p style="margin: 0 0 18px; font-size: 14px; color: #4b5563;">Thanks for placing an order with us. Here are the key details:</p>

              <div class="summary">
                <div class="summary-row"><span>Order ID:&nbsp;</span><span>${order.id}</span></div>
                <div class="summary-row"><span>Placed:&nbsp;</span><span>$placedAt</span></div>
              </div>

              <div class="total">Current total: $formattedTotal</div>

              ${buildProductList(order)}

              <p style="margin: 22px 0 0; font-size: 14px; color: #4b5563;">We’ll notify you once the order is completed. In the meantime you can share the order link with your customer to fill in the remaining details.</p>
              <p style="margin: 14px 0 0; font-size: 14px; color: #4b5563;">Warm regards,<br><strong>Order System Team</strong></p>
            </div>
            <div class="footer">
              This is an automated message. Please do not reply.
            </div>
          </div>
        </div>
      </body>
      </html>
    """.trimIndent()
  }

  private fun buildProductList(order: OrderDto): String {
    if (order.products.isEmpty()) {
      return """
        <div class="products-empty">
          No products added yet.
        </div>
      """.trimIndent()
    }

    val locale = Locale.Builder().setLanguage("en").setRegion("IL").build()
    val currencyFormatter = NumberFormat.getCurrencyInstance(locale)

    val rows = order.products.joinToString(separator = "") { product ->
      val lineTotal = product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong()))
      """
        <div class="product-row">
          <div>
            <div class="product-name">${product.productName}</div>
            <div class="product-meta">${product.quantity} × ${currencyFormatter.format(product.pricePerUnit)}</div>
            <div class="product-total">${currencyFormatter.format(lineTotal)}</div>
          </div>
        </div>
      """
    }

    return """
      <div class="products">
        <div class="products-title">Order items</div>
        <div class="products-body">
          $rows
        </div>
      </div>
    """.trimIndent()
  }

  private fun buildOrderDoneBody(order: OrderDto): String {
    val locale = Locale.Builder().setLanguage("en").setRegion("IL").build()
    val currencyFormatter = NumberFormat.getCurrencyInstance(locale)
    val formattedTotal = currencyFormatter.format(order.totalPrice)
    val completedAt = formatDateTime(order.updatedAt)

    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <style>
          body { font-family: 'Inter', Arial, sans-serif; background-color: #dcfce7; padding: 32px 12px; color: #1f2937; }
          .wrapper { max-width: 560px; margin: 0 auto; }
          .card { background: #f0fdf4; border-radius: 18px; border: 1px solid #86efac; box-shadow: 0 18px 36px rgba(22, 163, 74, 0.12); overflow: hidden; }
          .card-header { padding: 28px 32px 18px; background: linear-gradient(135deg, #15803d, #22c55e); color: #ffffff; }
          .card-header h2 { margin: 0; font-size: 22px; font-weight: 600; }
          .card-header p { margin: 6px 0 0; font-size: 14px; opacity: 0.85; }
          .card-body { padding: 26px 32px 32px; }
          .summary { border: 1px solid #e5e7eb; border-radius: 16px; padding: 18px 20px; background: #f9fafb; margin-top: 20px; }
          .summary-item { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; }
          .summary-item span:first-child { color: #6b7280; }
          .summary-item span:last-child { font-weight: 600; color: #111827; margin-left: 8px; }
          .total { margin-top: 18px; text-align: center; font-size: 18px; font-weight: 600; color: #15803d; }
          .footer { padding: 20px 32px 24px; text-align: center; font-size: 12px; color: #6b7280; }
        </style>
      </head>
      <body>
        <div class="wrapper">
          <div class="card">
            <div class="card-header">
              <h2>Order completed</h2>
              <p>We’re happy to let you know everything is wrapped up.</p>
            </div>
            <div class="card-body">
              <p style="margin: 0 0 14px; font-size: 15px;">Hello ${order.customerName ?: "Customer"},</p>
              <p style="margin: 0 0 18px; font-size: 14px; color: #4b5563;">Thanks again for your order. Here’s a quick summary of what was completed:</p>

              <div class="summary">
                <div class="summary-item"><span>Order ID:&nbsp;</span><span>${order.id}</span></div>
                <div class="summary-item"><span>Completed:&nbsp;</span><span>$completedAt</span></div>
              </div>

              <div class="total">Final total: $formattedTotal</div>

              <p style="margin: 22px 0 0; font-size: 14px; color: #4b5563;">We appreciate your business and look forward to serving you again. If you have any feedback, we’d love to hear it.</p>
              <p style="margin: 14px 0 0; font-size: 14px; color: #4b5563;">Warm regards,<br><strong>Order System Team</strong></p>
            </div>
            <div class="footer">
              This is an automated message. Please do not reply.
            </div>
          </div>
        </div>
      </body>
      </html>
    """.trimIndent()
  }

  private fun formatDateTime(dateTime: LocalDateTime): String = dateTime.format(dateFormatter)
}
