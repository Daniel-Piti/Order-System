package com.pt.ordersystem.ordersystem.domains.invoices.helpers

import com.pt.ordersystem.ordersystem.constants.TaxConstants
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDto
import com.pt.ordersystem.ordersystem.domains.invoices.models.PaymentMethod
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

/**
 * Utility class to generate PDF preview files for InvoiceRenderHelper.
 * Run the main function to generate a preview PDF in the test/resources/invoice-previews folder.
 * This is a development utility for previewing invoice templates.
 */
object InvoiceRenderHelperPreviewGenerator {

  fun generatePreview() {
    // Try to find the project root by looking for build.gradle.kts
    val projectRoot = findProjectRoot()
    val previewDir = File(projectRoot, "src/test/resources/invoice-previews")
    previewDir.mkdirs()

    // Generate sample data
    val sampleBusiness = createSampleBusiness()
    val sampleOrder = createSampleOrder()
    val invoiceSequenceNumber = 7439

    // Generate PDF using InvoiceRenderHelper
    val pdfBytes = InvoiceRenderHelper.renderPdf(
      business = sampleBusiness,
      order = sampleOrder,
      invoiceSequenceNumber = invoiceSequenceNumber,
      paymentMethod = PaymentMethod.CREDIT_CARD,
      paymentProof = "1234",
      allocationNumber = "123456789"
    )

    // Save PDF to file
    val outputFile = File(previewDir, "invoice-preview.pdf")
    outputFile.writeBytes(pdfBytes)

    println("✅ Invoice preview generated successfully in: ${previewDir.absolutePath}")
    println("   - invoice-preview.pdf")
    println("   File size: ${pdfBytes.size / 1024} KB")
  }

  @JvmStatic
  fun main(args: Array<String>) {
    generatePreview()
  }

  private fun createSampleBusiness(): BusinessDto {
    return BusinessDto(
        id = "business-123",
        managerId = "manager-123",
        name = "דניאל פיטימסון בע\"מ",
        stateIdNumber = "515123456",
        email = "info@pitimson.co.il",
        phoneNumber = "050-1234567",
        streetAddress = "רחוב הרצל 15",
        city = "תל אביב",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
        imageUrl = "",
        fileName = "",
        mimeType = "",
    )
  }

  private fun createSampleOrder(): OrderDbEntity {
    val now = LocalDateTime.now()
    val placedAt = now.minusHours(2)
    val doneAt = now.minusMinutes(30)

    // Calculate realistic totals
    val product1Total = BigDecimal("89.90").multiply(BigDecimal("2")) // 179.80
    val product2Total = BigDecimal("45.50")
    val product3Total = BigDecimal("32.00").multiply(BigDecimal("3")) // 96.00
    val product4Total = BigDecimal("55.00")
    val product5Total = BigDecimal("28.50").multiply(BigDecimal("2")) // 57.00
    
    val subtotal = product1Total.add(product2Total).add(product3Total).add(product4Total).add(product5Total) // 433.30
    val discount = BigDecimal("50.00") // Realistic discount
    val totalAfterDiscount = subtotal.subtract(discount) // 383.30
    val totalWithVat = totalAfterDiscount.multiply(BigDecimal("1.18")).setScale(2, RoundingMode.HALF_UP) // 452.29

    return OrderDbEntity(
      id = "ORD-2025-001234",
      referenceId = 10001L,
      orderSource = "PUBLIC",
      managerId = "manager-123",
      agentId = "agent-456",
      customerId = "customer-789",
      storeStreetAddress = "רחוב דיזנגוף 50",
      storeCity = "תל אביב",
      storePhoneNumber = "+972-3-1234567",
      customerName = "יוסי כהן",
      customerPhone = "+972-50-9876543",
      customerEmail = "yossi.cohen@example.com",
      customerStreetAddress = "רחוב בן יהודה 20",
      customerCity = "ירושלים",
      customerStateId = "987654321",
      status = "DONE",
      products = listOf(
        ProductDataForOrder(
          productId = "prod-1",
          productName = "פולי קפה איכותיים - ערביקה",
          quantity = 2,
          pricePerUnit = BigDecimal("89.90")
        ),
        ProductDataForOrder(
          productId = "prod-2",
          productName = "תה ירוק אורגני - 100 גרם",
          quantity = 1,
          pricePerUnit = BigDecimal("45.50")
        ),
        ProductDataForOrder(
          productId = "prod-3",
          productName = "עוגיות שוקולד צ'יפס - חבילה של 12",
          quantity = 3,
          pricePerUnit = BigDecimal("32.00")
        ),
        ProductDataForOrder(
          productId = "prod-4",
          productName = "קפה נמס פילטר - חבילה של 250 גרם",
          quantity = 1,
          pricePerUnit = BigDecimal("55.00")
        ),
        ProductDataForOrder(
          productId = "prod-5",
          productName = "שוקולד מריר 70% קקאו - 200 גרם",
          quantity = 2,
          pricePerUnit = BigDecimal("28.50")
        )
      ),
      productsVersion = 1,
      totalPrice = totalWithVat,
      discount = discount,
      vat = TaxConstants.VAT_PERCENTAGE,
      linkExpiresAt = now.plusDays(7),
      notes = "אנא למסור בדלת הקדמית. לצלצל פעמיים בפעמון.",
      placedAt = placedAt,
      doneAt = doneAt,
      createdAt = placedAt,
      updatedAt = doneAt
    )
  }

  private fun findProjectRoot(): File {
    // Start from current working directory and look for build.gradle.kts
    var current = File(System.getProperty("user.dir"))
    while (current.exists()) {
      if (File(current, "build.gradle.kts").exists()) {
        return current
      }
      current = current.parentFile
    }
    // Fallback to current directory
    return File(System.getProperty("user.dir"))
  }
}
