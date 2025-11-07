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
import java.util.Locale

@Service
class EmailNotificationMailer(
  private val mailSender: JavaMailSender,
) {

  @Value("\${spring.mail.from-email}")
  private lateinit var fromEmail: String

  private val logger = LoggerFactory.getLogger(EmailNotificationService::class.java)

  fun sendOrderPlacedEmail(order: OrderDto, recipientEmail: String) {
    try {
      val message: MimeMessage = mailSender.createMimeMessage()
      val helper = MimeMessageHelper(message, true, "UTF-8")

      helper.setTo(recipientEmail)
      helper.setSubject("Order Placed - Order #${order.id}")
      helper.setFrom(fromEmail)
      helper.setText(buildEmailBody(order), true) // true = HTML

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

  private fun buildEmailBody(order: OrderDto): String {
    val locale = Locale.Builder().setLanguage("en").setRegion("IL").build()
    val currencyFormatter = NumberFormat.getCurrencyInstance(locale)
    val formattedTotal = currencyFormatter.format(order.totalPrice)

    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <style>
          body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
          .container { max-width: 600px; margin: 0 auto; padding: 20px; }
          .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
          .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
          .order-info { background-color: white; padding: 15px; margin: 15px 0; border-radius: 5px; }
          .order-item { padding: 10px 0; border-bottom: 1px solid #eee; }
          .order-item:last-child { border-bottom: none; }
          .total { font-size: 18px; font-weight: bold; color: #4CAF50; margin-top: 15px; }
          .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
        </style>
      </head>
      <body>
        <div class="container">
          <div class="header">
            <h1>Order Placed</h1>
          </div>
          <div class="content">
            <p>Hello ${order.customerName ?: "Customer"},</p>
            <p>Thank you for your order! We've received your order and it's being processed.</p>
            
            <div class="order-info">
              <h2>Order Details</h2>
              <p><strong>Order ID:</strong> ${order.id}</p>
              <p><strong>Order Date:</strong> ${order.createdAt}</p>
              <p><strong>Status:</strong> ${order.status.name}</p>
              
              ${if (order.deliveryDate != null) "<p><strong>Delivery Date:</strong> ${order.deliveryDate}</p>" else ""}
              
              <h3>Items Ordered:</h3>
              ${order.products.joinToString("") { product ->
                """
                <div class="order-item">
                  <p><strong>${product.productName}</strong></p>
                  <p>${product.quantity} × ${currencyFormatter.format(product.pricePerUnit)} = ${currencyFormatter.format(product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))}</p>
                </div>
                """
              }}
              
              <div class="total">
                <p>Total: $formattedTotal</p>
              </div>
            </div>
            
            ${if (order.customerStreetAddress != null && order.customerCity != null) """
            <div class="order-info">
              <h3>Delivery Address</h3>
              <p>${order.customerStreetAddress}<br>${order.customerCity}</p>
            </div>
            """ else ""}
            
            ${if (order.notes.isNotBlank()) """
            <div class="order-info">
              <h3>Notes</h3>
              <p>${order.notes}</p>
            </div>
            """ else ""}
            
            <p>If you have any questions about your order, please don't hesitate to contact us.</p>
            <p>Best regards,<br>Order System Team</p>
          </div>
          <div class="footer">
            <p>This is an automated message. Please do not reply to this email.</p>
          </div>
        </div>
      </body>
      </html>
    """.trimIndent()
  }
}
