package com.pt.ordersystem.ordersystem.notification.email.emails

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDto
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import java.io.InputStream
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Shared utilities for email template building.
 */
object EmailTemplateHelper {

  private val logger = LoggerFactory.getLogger(EmailTemplateHelper::class.java)
  private val templateCache = mutableMapOf<String, String>()
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm")

  fun formatCurrency(amount: BigDecimal): String {
    return "₪${amount}"
  }

  fun formatDateTime(dateTime: LocalDateTime): String {
    return dateTime.format(dateFormatter)
  }

  fun buildProductList(order: OrderDto, accentColor: String = "#4338ca", borderColor: String = "#c7d2fe"): String {
    if (order.products.isEmpty()) {
      return """
        <div class="products-empty">
          No products added yet.
        </div>
      """.trimIndent()
    }

    val rows = order.products.joinToString(separator = "") { product ->
      val lineTotal = product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong()))
      """
        <div class="product-row">
          <div>
            <div class="product-name">${escapeHtml(product.productName)}</div>
            <div class="product-meta">${product.quantity} × ${formatCurrency(product.pricePerUnit)}</div>
            <div class="product-total" style="color: $accentColor;">${formatCurrency(lineTotal)}</div>
          </div>
        </div>
      """
    }

    return """
      <div class="products" style="border-color: $borderColor;">
        <div class="products-title">Order items</div>
        <div class="products-body">
          $rows
        </div>
      </div>
    """.trimIndent()
  }

  fun escapeHtml(text: String): String {
    return text
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#39;")
  }

  fun getCustomerName(order: OrderDto): String {
    return order.customerName ?: "Customer"
  }

  /**
   * Loads an HTML template from resources.
   */
  fun loadTemplate(templateName: String): String {
    return templateCache.getOrPut(templateName) {
      try {
        val resource = ClassPathResource("templates/notification/$templateName")
        resource.inputStream.use { inputStream: InputStream ->
          inputStream.bufferedReader().use { it.readText() }
        }
      } catch (e: Exception) {
        logger.error("Failed to load email template: $templateName", e)
        throw IllegalStateException("Email template not found: $templateName", e)
      }
    }
  }

  /**
   * Replaces placeholders in a template with provided data.
   */
  fun replacePlaceholders(template: String, replacements: Map<String, String>): String {
    var result = template
    replacements.forEach { (key, value) ->
      result = result.replace("{{$key}}", value)
    }
    return result
  }
}

