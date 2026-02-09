package com.pt.ordersystem.ordersystem.notification.email

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDto
import com.pt.ordersystem.ordersystem.domains.order.models.OrderSource
import com.pt.ordersystem.ordersystem.domains.order.models.OrderStatus
import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import com.pt.ordersystem.ordersystem.notification.email.emails.OrderCancelledEmail
import com.pt.ordersystem.ordersystem.notification.email.emails.OrderDoneEmail
import com.pt.ordersystem.ordersystem.notification.email.emails.OrderPlacedEmail
import com.pt.ordersystem.ordersystem.notification.email.emails.OrderUpdatedEmail
import java.io.File
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Utility class to generate HTML preview files for email templates.
 * Run the main function to generate all email previews in the test/resources/email-previews folder.
 * This is a development utility for previewing email templates.
 */
object EmailPreviewGenerator {

  fun generateAllPreviews() {
    // Try to find the project root by looking for build.gradle.kts
    val projectRoot = findProjectRoot()
    val previewDir = File(projectRoot, "src/test/resources/email-previews")
    previewDir.mkdirs()

    // Generate sample order data
    val sampleOrder = createSampleOrder()

    // Use email template objects
    val placedHtml = OrderPlacedEmail.buildHtml(sampleOrder)
    File(previewDir, "order-placed.html").writeText(placedHtml)

    val doneHtml = OrderDoneEmail.buildHtml(sampleOrder)
    File(previewDir, "order-done.html").writeText(doneHtml)

    val cancelledHtml = OrderCancelledEmail.buildHtml(sampleOrder)
    File(previewDir, "order-cancelled.html").writeText(cancelledHtml)

    val updatedHtml = OrderUpdatedEmail.buildHtml(sampleOrder)
    File(previewDir, "order-updated.html").writeText(updatedHtml)

    println("✅ Email previews generated successfully in: ${previewDir.absolutePath}")
    println("   - order-placed.html")
    println("   - order-done.html")
    println("   - order-cancelled.html")
    println("   - order-updated.html")
  }

  @JvmStatic
  fun main(args: Array<String>) {
    generateAllPreviews()
  }

  private fun createSampleOrder(): OrderDto {
    val now = LocalDateTime.now()
    val placedAt = now.minusHours(2)
    val doneAt = now.minusMinutes(30)
    val updatedAt = now.minusMinutes(15)

    return OrderDto(
      id = "ORD-2024-001234",
      referenceId = 10001L,
      orderSource = OrderSource.PUBLIC,
      managerId = "manager-123",
      agentId = 456L,
      customerId = "customer-789",
      storeStreetAddress = "123 Main Street",
      storeCity = "Tel Aviv",
      storePhoneNumber = "+972-3-1234567",
      customerName = "John Doe",
      customerPhone = "+972-50-1234567",
      customerEmail = "john.doe@example.com",
      customerStreetAddress = "456 Oak Avenue",
      customerCity = "Jerusalem",
      customerStateId = null,
      status = OrderStatus.PLACED,
      products = listOf(
        ProductDataForOrder(
          productId = "prod-1",
          productName = "Premium Coffee Beans - Arabica",
          quantity = 2,
          pricePerUnit = BigDecimal("89.90")
        ),
        ProductDataForOrder(
          productId = "prod-2",
          productName = "Organic Green Tea - 100g",
          quantity = 1,
          pricePerUnit = BigDecimal("45.50")
        ),
        ProductDataForOrder(
          productId = "prod-3",
          productName = "Chocolate Chip Cookies - Pack of 12",
          quantity = 3,
          pricePerUnit = BigDecimal("32.00")
        )
      ),
      productsVersion = 1,
      totalPrice = BigDecimal("336.70"), // (89.90 * 2) + 45.50 + (32.00 * 3)
      discount = BigDecimal.ZERO,
      linkExpiresAt = now.plusDays(7),
      notes = "Please deliver to the front door. Ring the bell twice.",
      placedAt = placedAt,
      doneAt = doneAt,
      createdAt = placedAt,
      updatedAt = updatedAt
    )
  }


  private fun findProjectRoot(): File {
    // Start from current working directory and look for build.gradle.kts
    var current = File(System.getProperty("user.dir"))
    while (current != null && current.exists()) {
      if (File(current, "build.gradle.kts").exists()) {
        return current
      }
      current = current.parentFile
    }
    // Fallback to current directory
    return File(System.getProperty("user.dir"))
  }

}

