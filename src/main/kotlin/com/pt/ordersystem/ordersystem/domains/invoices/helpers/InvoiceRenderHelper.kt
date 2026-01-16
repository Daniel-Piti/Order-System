package com.pt.ordersystem.ordersystem.domains.invoices.helpers

import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDto
import com.pt.ordersystem.ordersystem.domains.invoices.models.PaymentMethod
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.io.InputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.util.*

object InvoiceRenderHelper {
  private val currencyFormatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))

  private const val VAT_PERCENTAGE = 18
  private val VAT_RATE = BigDecimal("1.18")

  // Layout constants
  private const val PAGE_MARGIN = 50f
  private const val PANEL_GAP = 30f
  private const val LINE_SPACING = 14f
  private const val ROW_HEIGHT = 25f
  private const val VERTICAL_PADDING = 6f
  private const val FONT_SIZE_REGULAR = 11f
  private const val FONT_SIZE_SMALL = 10f
  private const val TEXT_BASELINE_OFFSET = 2.5f

  private data class InvoiceTheme(
    val regularFont: PDFont,
    val boldFont: PDFont,
    val textColor: Color = Color(0, 0, 0),
    val borderColor: Color = Color(200, 200, 200),
    val tableHeaderBgColor: Color = Color(170, 130, 210), // Darker lavender for table header
    val tableHeaderTextColor: Color = Color(255, 255, 255),
    val sectionBgColor: Color = Color(248, 240, 255), // Light lavender tint for sections
    val dividerColor: Color = Color(189, 195, 199) // Gray for dividers
  )
  
  private const val CORNER_RADIUS = 15f
  
  // Status text mapping
  private val statusTextMap = mapOf(
    "DONE" to "הושלם",
    "PLACED" to "הוזמן",
    "EMPTY" to "ריק",
    "EXPIRED" to "פג תוקף",
    "CANCELLED" to "בוטל"
  )
  
  // Payment method text mapping
  private val paymentMethodTextMap = mapOf(
    PaymentMethod.CREDIT_CARD to "כרטיס אשראי",
    PaymentMethod.CASH to "מזומן"
  )

  fun renderPdf(
    business: BusinessDto,
    order: OrderDbEntity,
    invoiceSequenceNumber: Int,
    paymentMethod: PaymentMethod? = null,
    paymentProof: String? = null,
    allocationNumber: String? = null
  ): ByteArray {
    val invoiceDate = formatCurrentDate()

    return PDDocument().use { document ->
      document.documentInformation.title = "Invoice-$invoiceSequenceNumber"
      document.documentInformation.author = "Order System"

      val theme = createTheme(document)
      val pages = mutableListOf<PDPage>()
      val page = PDPage(PDRectangle.A4)
      document.addPage(page)
      pages.add(page)

      val pageWidth = page.mediaBox.width
      val pageHeight = page.mediaBox.height
      val contentWidth = pageWidth - (PAGE_MARGIN * 2)

      var currentPageIndex = 0
      var content = PDPageContentStream(document, pages[currentPageIndex])
      var currentY = pageHeight - PAGE_MARGIN

      try {
        currentY = drawHeader(content, theme, pageWidth, currentY, order.id, invoiceSequenceNumber, invoiceDate, order.status)
        currentY =
          drawCustomerAndBusinessPanels(content, theme, pageWidth, currentY, order, business, allocationNumber)
        
        // Draw products table with pagination
        val productsResult = drawProductsTableWithPagination(
          document, content, theme, PAGE_MARGIN, contentWidth, currentY, order,
          pageHeight, pages, currentPageIndex
        )
        currentY = productsResult.first
        content = productsResult.third // Use the content stream returned from pagination function
        var currentPageIndex = productsResult.second
        
        // Draw summary with pagination check
        val summaryResult = drawSummaryWithPagination(
          document, content, theme, PAGE_MARGIN, contentWidth, currentY, order, pageHeight, pages, currentPageIndex
        )
        currentY = summaryResult.first
        content = summaryResult.third
        currentPageIndex = summaryResult.second
        
        // Draw payment details with pagination check
        val paymentResult = drawPaymentDetailsWithPagination(
          document, content, theme, PAGE_MARGIN, contentWidth, currentY, order, paymentMethod, paymentProof, pageHeight, pages, currentPageIndex
        )
        currentY = paymentResult.first
        content = paymentResult.third
        currentPageIndex = paymentResult.second
        
        // Draw footer with pagination check
        val footerResult = drawFooterWithPagination(
          document, content, theme, pageWidth, currentY, pageHeight, pages, currentPageIndex
        )
        content = footerResult.third
      } finally {
        content.close()
      }

      val output = ByteArrayOutputStream()
      document.save(output)
      output.toByteArray()
    }
  }

  private fun drawHeader(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    pageWidth: Float,
    y: Float,
    orderId: String,
    invoiceSequenceNumber: Int,
    date: String,
    orderStatus: String
  ): Float {
    val titleHeight = 40f
    val detailsSpacing = 20f
    val details = listOf(
      "מספר חשבונית: INV-$invoiceSequenceNumber" to true,
      "תאריך: $date" to false,
      "מספר הזמנה: $orderId" to false,
      "סטטוס: ${statusTextMap[orderStatus] ?: orderStatus}" to false
    )
    val headerHeight = 30f + titleHeight + (details.size * detailsSpacing) + 20f
    val headerBottom = y - headerHeight
    val headerWidth = pageWidth - (PAGE_MARGIN * 2)
    val detailsRightX = pageWidth - PAGE_MARGIN - 20f

    // Draw header background and border
    content.setNonStrokingColor(Color(248, 240, 255))
    content.drawRoundedRect(PAGE_MARGIN, headerBottom, headerWidth, headerHeight, CORNER_RADIUS, fill = true, stroke = false)
    content.setStrokingColor(theme.borderColor)
    content.setLineWidth(1f)
    content.drawRoundedRect(PAGE_MARGIN, headerBottom, headerWidth, headerHeight, CORNER_RADIUS, fill = false, stroke = true)

    // Title
    var currentY = y - 30f
    content.writeTextRightAligned("חשבונית מס־קבלה", detailsRightX, currentY, theme.boldFont, 32f, theme.textColor)
    
    // Details
    currentY -= 45f
    details.forEach { (text, isBold) ->
      content.writeTextRightAligned(text, detailsRightX, currentY, if (isBold) theme.boldFont else theme.regularFont, if (isBold) 12f else FONT_SIZE_REGULAR, theme.textColor)
      currentY -= detailsSpacing
    }

    return headerBottom - 30f
  }

  private fun drawCustomerAndBusinessPanels(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    pageWidth: Float,
    y: Float,
    order: OrderDbEntity,
    business: BusinessDto,
    allocationNumber: String?
  ): Float {
    val panelWidth = (pageWidth - (PAGE_MARGIN * 2) - PANEL_GAP) / 2f
    val rightPanelX = pageWidth - PAGE_MARGIN
    val leftPanelX = rightPanelX - panelWidth - PANEL_GAP

    val customerAddress = listOfNotNull(
      order.customerStreetAddress,
      order.customerCity
    ).joinToString(", ")

    val customerFields = mutableListOf(
      "שם: ${order.customerName ?: ""}",
      "כתובת: $customerAddress"
    )
    order.customerPhone?.let { customerFields.add("טלפון: $it") }
    order.customerEmail?.let { customerFields.add("אימייל: $it") }
    order.customerStateId?.let { customerFields.add("ח.פ / ע.מ: $it") }

    val customerY = drawStyledPanel(content, theme, rightPanelX, panelWidth, y, "לכבוד", customerFields)

    val businessAddress = "${business.streetAddress}, ${business.city}"

    val businessFields = mutableListOf(
      "שם העסק: ${business.name}",
      "כתובת: $businessAddress",
      "טלפון: ${business.phoneNumber}",
      "ח.פ / ע.מ: ${business.stateIdNumber}"
    )

    allocationNumber?.let { businessFields.add("מספר הקצאה: $it") }

    val businessY = drawStyledPanel(content, theme, leftPanelX, panelWidth, y, "פרטי עסק", businessFields)

    return minOf(customerY, businessY) - 30f
  }

  private fun drawStyledPanel(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    rightX: Float,
    width: Float,
    y: Float,
    title: String,
    fields: List<String>,
    useBoldFont: Boolean = true,
    lineHeight: Float = 20f,
    yOffset: Float = 0f,
    bottomOffset: Float = 10f
  ): Float {
    val panelLeft = rightX - width
    val titleHeight = 30f
    val fieldPadding = 15f
    val currentY = y + yOffset
    
    // Calculate panel height
    val totalFieldsHeight = fields.fold(0f) { acc, field ->
      acc + (content.wrapTextForPdf(field, width - 20f, theme.regularFont, FONT_SIZE_REGULAR).size * lineHeight + 5f)
    }
    val panelHeight = titleHeight + fieldPadding + totalFieldsHeight + fieldPadding
    val panelBottom = currentY - panelHeight

    // Draw panel background and border
    content.setNonStrokingColor(theme.sectionBgColor)
    content.drawRoundedRect(panelLeft, panelBottom, width, panelHeight, CORNER_RADIUS, fill = true, stroke = false)
    content.setStrokingColor(theme.borderColor)
    content.setLineWidth(1f)
    content.drawRoundedRect(panelLeft, panelBottom, width, panelHeight, CORNER_RADIUS, fill = false, stroke = true)

    // Draw title header
    drawRoundedTitleHeader(content, theme, panelLeft, currentY - titleHeight, width, titleHeight)
    content.writeTextRightAligned(title, rightX - 10f, currentY - (titleHeight / 2f) - TEXT_BASELINE_OFFSET, theme.boldFont, 13f, theme.tableHeaderTextColor)
    
    // Draw fields (vertically centered)
    val fieldsStartY = (currentY - titleHeight) - ((panelHeight - titleHeight) / 2f) + (totalFieldsHeight / 2f)
    var fieldY = fieldsStartY
    fields.forEach { field ->
      fieldY = content.writeWrappedTextRightAligned(field, rightX - 10f, fieldY, width - 20f, if (useBoldFont) theme.boldFont else theme.regularFont, FONT_SIZE_REGULAR, theme.textColor, lineHeight)
      fieldY -= 5f
    }

    return panelBottom - bottomOffset
  }

  private fun getColumnWidths(contentWidth: Float) = floatArrayOf(60f, contentWidth - 260f, 100f, 100f)

  /**
   * Checks if there's enough space on the current page and creates a new page if needed.
   * Returns a Triple of (newY, newPageIndex, newContentStream)
   */
  private fun ensureSpaceForComponent(
    document: PDDocument,
    content: PDPageContentStream,
    currentY: Float,
    requiredHeight: Float,
    pageHeight: Float,
    pages: MutableList<PDPage>,
    currentPageIndex: Int,
    minSpaceThreshold: Float = pageHeight * 0.1f // 10% of page height as minimum space
  ): Triple<Float, Int, PDPageContentStream> {
    var newY = currentY
    var newPageIndex = currentPageIndex
    var newContent = content
    
    // Check if we need a new page (if currentY - requiredHeight would be less than threshold)
    if (currentY - requiredHeight < minSpaceThreshold) {
      // Close current content stream
      try {
        content.close()
      } catch (e: Exception) {
        // Stream might already be closed, continue anyway
      }
      
      // Create new page
      val newPage = PDPage(PDRectangle.A4)
      document.addPage(newPage)
      pages.add(newPage)
      newPageIndex++
      newContent = PDPageContentStream(document, newPage)
      newY = pageHeight - PAGE_MARGIN
    }
    
    return Triple(newY, newPageIndex, newContent)
  }

  private fun drawProductsTableWithPagination(
    document: PDDocument,
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    contentWidth: Float,
    y: Float,
    order: OrderDbEntity,
    pageHeight: Float,
    pages: MutableList<PDPage>,
    currentPageIndex: Int
  ): Triple<Float, Int, PDPageContentStream> {
    val columnWidths = getColumnWidths(contentWidth)
    var currentY = y
    var pageIndex = currentPageIndex
    var currentContent = content
    var minYForContent = PAGE_MARGIN + 150f // Reserve space for summary, payment, footer
    
    // Draw header with colored background and rounded corners
    val headerHeight = 30f
    var headerY = currentY - headerHeight
    
    // Check if we need a new page for header
    if (currentY - headerHeight < minYForContent) {
      // Only close if we're switching from the original page
      if (pageIndex == currentPageIndex) {
        currentContent.close()
      }
      val newPage = PDPage(PDRectangle.A4)
      document.addPage(newPage)
      pages.add(newPage)
      pageIndex++
      currentContent = PDPageContentStream(document, newPage)
      currentY = pageHeight - PAGE_MARGIN
      headerY = currentY - headerHeight
    }
    
    drawTableHeader(currentContent, theme, margin, headerY, currentY, contentWidth, headerHeight, columnWidths)
    currentY -= headerHeight

    // Draw products with alternating row colors and pagination
    order.products.forEachIndexed { index, product ->
      val productTotal = product.pricePerUnit.multiply(BigDecimal(product.quantity))
      
      // Calculate row height first to check if we need a new page
      val productNameText = product.productName.replace("\n", " ")
      val tempLines = currentContent.wrapTextForPdf(
        productNameText,
        columnWidths[1] - 10f,
        theme.regularFont,
        FONT_SIZE_SMALL
      )
      val estimatedRowHeight = maxOf(ROW_HEIGHT, (tempLines.size * LINE_SPACING) + (VERTICAL_PADDING * 2f))
      
      // Check if we need a new page BEFORE drawing the row
      if (currentY - estimatedRowHeight < minYForContent) {
        // Close current content stream before creating new page
        try {
          currentContent.close()
        } catch (e: Exception) {
          // Stream might already be closed, continue anyway
        }
        val newPage = PDPage(PDRectangle.A4)
        document.addPage(newPage)
        pages.add(newPage)
        pageIndex++
        currentContent = PDPageContentStream(document, newPage)
        currentY = pageHeight - PAGE_MARGIN
        
        // Redraw header on new page
        headerY = currentY - headerHeight
        drawTableHeader(currentContent, theme, margin, headerY, currentY, contentWidth, headerHeight, columnWidths)
        currentY -= headerHeight
      }
      
      // Now recalculate with the current content stream (in case we switched pages)
      val productNameLines = currentContent.wrapTextForPdf(
        productNameText,
        columnWidths[1] - 10f,
        theme.regularFont,
        FONT_SIZE_SMALL
      )
      val actualRowHeight = maxOf(ROW_HEIGHT, (productNameLines.size * LINE_SPACING) + (VERTICAL_PADDING * 2f))
      
      drawProductRow(
        currentContent, theme, margin, currentY, actualRowHeight, contentWidth,
        product.quantity.toString(), productNameLines, formatCurrency(product.pricePerUnit),
        formatCurrency(productTotal), columnWidths, index % 2 == 0
      )
      currentY -= actualRowHeight
    }
    
    // Return the current Y position, page index, and the content stream (still open)
    return Triple(currentY - 30f, pageIndex, currentContent)
  }

  // Helper function to draw table header
  private fun drawTableHeader(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    headerY: Float,
    currentY: Float,
    contentWidth: Float,
    headerHeight: Float,
    columnWidths: FloatArray
  ) {
    content.setNonStrokingColor(theme.tableHeaderBgColor)
    content.drawRoundedRect(margin, headerY, contentWidth, headerHeight, CORNER_RADIUS, fill = true, stroke = false)
    content.setStrokingColor(theme.borderColor)
    content.setLineWidth(1f)
    content.drawRoundedRect(margin, headerY, contentWidth, headerHeight, CORNER_RADIUS, fill = false, stroke = true)
    
    val headerTextY = currentY - (headerHeight / 2f) - TEXT_BASELINE_OFFSET
    var tableX = margin
    
    content.writeTextCentered("כמות", tableX, columnWidths[0], headerTextY, theme.boldFont, FONT_SIZE_REGULAR, theme.tableHeaderTextColor)
    tableX += columnWidths[0]
    content.writeText("תיאור מוצר", tableX + 5f, headerTextY, theme.boldFont, FONT_SIZE_REGULAR, theme.tableHeaderTextColor)
    tableX += columnWidths[1]
    content.writeTextCentered("מחיר יחידה", tableX, columnWidths[2], headerTextY, theme.boldFont, FONT_SIZE_REGULAR, theme.tableHeaderTextColor)
    tableX += columnWidths[2]
    content.writeTextCentered("סה\"כ", tableX, columnWidths[3], headerTextY, theme.boldFont, FONT_SIZE_REGULAR, theme.tableHeaderTextColor)
  }
  
  // Helper function to draw rounded title header (top corners only)
  private fun drawRoundedTitleHeader(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    leftX: Float,
    titleY: Float,
    width: Float,
    titleHeight: Float
  ) {
    content.setNonStrokingColor(theme.tableHeaderBgColor)
    content.moveTo(leftX + CORNER_RADIUS, titleY)
    content.lineTo(leftX + width - CORNER_RADIUS, titleY)
    content.curveTo(leftX + width, titleY, leftX + width, titleY, leftX + width, titleY + CORNER_RADIUS)
    content.lineTo(leftX + width, titleY + titleHeight)
    content.lineTo(leftX, titleY + titleHeight)
    content.lineTo(leftX, titleY + CORNER_RADIUS)
    content.curveTo(leftX, titleY, leftX, titleY, leftX + CORNER_RADIUS, titleY)
    content.closePath()
    content.fill()
  }
  
  // Helper function to draw a product row
  private fun drawProductRow(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    currentY: Float,
    rowHeight: Float,
    contentWidth: Float,
    quantity: String,
    productNameLines: List<String>,
    unitPrice: String,
    totalPrice: String,
    columnWidths: FloatArray,
    isEvenRow: Boolean
  ) {
    val rowBottom = currentY - rowHeight
    val rowCenterY = rowBottom + (rowHeight / 2f)
    val textBaselineY = rowCenterY - TEXT_BASELINE_OFFSET
    
    // Alternate row background
    if (isEvenRow) {
      content.setNonStrokingColor(Color(252, 248, 255))
      content.addRect(margin, rowBottom, contentWidth, rowHeight)
      content.fill()
    }
    
    // Draw row border
    content.setStrokingColor(theme.borderColor)
    content.setLineWidth(0.5f)
    content.addRect(margin, rowBottom, contentWidth, rowHeight)
    content.stroke()
    
    var tableX = margin
    content.writeTextCentered(quantity, tableX, columnWidths[0], textBaselineY, theme.regularFont, FONT_SIZE_SMALL, theme.textColor)
    tableX += columnWidths[0]
    
    drawMultiLineText(content, tableX + 5f, productNameLines, rowCenterY, theme.regularFont, FONT_SIZE_SMALL, theme.textColor)
    tableX += columnWidths[1]
    
    content.writeTextCentered(unitPrice, tableX, columnWidths[2], textBaselineY, theme.regularFont, FONT_SIZE_SMALL, theme.textColor)
    tableX += columnWidths[2]
    
    content.writeTextCentered(totalPrice, tableX, columnWidths[3], textBaselineY, theme.regularFont, FONT_SIZE_SMALL, theme.textColor)
  }

  private fun drawMultiLineText(
    content: PDPageContentStream,
    x: Float,
    lines: List<String>,
    rowCenterY: Float,
    font: PDFont,
    fontSize: Float,
    color: Color
  ) {
    val middleLineIndex = (lines.size - 1) / 2
    val middleLineBaseline = rowCenterY - TEXT_BASELINE_OFFSET
    var nameY = middleLineBaseline + (middleLineIndex * LINE_SPACING)

    lines.forEach { line ->
      content.writeText(line, x, nameY, font, fontSize, color)
      nameY -= LINE_SPACING
    }
  }

  private fun drawSummaryWithPagination(
    document: PDDocument,
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    contentWidth: Float,
    y: Float,
    order: OrderDbEntity,
    pageHeight: Float,
    pages: MutableList<PDPage>,
    currentPageIndex: Int
  ): Triple<Float, Int, PDPageContentStream> {
    // Adjust summary height based on whether discount is applied
    val hasDiscount = order.discount > BigDecimal.ZERO
    val summaryHeight = if (hasDiscount) 140f else 100f
    
    // Check if we need a new page
    val spaceResult = ensureSpaceForComponent(document, content, y, summaryHeight, pageHeight, pages, currentPageIndex)
    var currentY = spaceResult.first
    var pageIndex = spaceResult.second
    var currentContent = spaceResult.third
    
    return Triple(drawSummary(currentContent, theme, margin, contentWidth, currentY, order), pageIndex, currentContent)
  }

  private fun drawSummary(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    contentWidth: Float,
    y: Float,
    order: OrderDbEntity
  ): Float {
    // Calculate products total (sum of all products - this is WITH VAT)
    val productsTotalWithVat = order.products.fold(BigDecimal.ZERO) { sum, product ->
      sum + (product.pricePerUnit.multiply(BigDecimal.valueOf(product.quantity.toLong())))
    }
    
    val totalWithVat = order.totalPrice
    val columnWidths = getColumnWidths(contentWidth)
    val summaryX = margin + columnWidths[0] + columnWidths[1]
    val summaryWidth = columnWidths[2] + columnWidths[3]
    val priceX = summaryX + 5f
    val labelRightX = summaryX + summaryWidth - 5f
    
    // Adjust summary height based on whether discount is applied
    val hasDiscount = order.discount > BigDecimal.ZERO
    val summaryHeight = if (hasDiscount) 140f else 100f
    val summaryY = y - summaryHeight

    // Draw summary box
    content.setNonStrokingColor(theme.sectionBgColor)
    content.drawRoundedRect(summaryX - 10f, summaryY, summaryWidth + 20f, summaryHeight, CORNER_RADIUS, fill = true, stroke = false)
    content.setStrokingColor(theme.borderColor)
    content.setLineWidth(1f)
    content.drawRoundedRect(summaryX - 10f, summaryY, summaryWidth + 20f, summaryHeight, CORNER_RADIUS, fill = false, stroke = true)

    // Summary lines
    var currentY = y - 15f
    
    if (hasDiscount) {
      // Discount breakdown: use totalPrice and discount as source of truth (both WITH VAT)
      // 1. Calculate products total WITHOUT VAT: (totalPrice + discount) / VAT_RATE
      //    This gives us the original products total before discount, without VAT
      val productsTotalWithVatBeforeDiscount = totalWithVat.add(order.discount)
      val productsTotalWithoutVat = productsTotalWithVatBeforeDiscount.divide(VAT_RATE, 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP)
      
      // 2. Calculate discount WITHOUT VAT: discount / VAT_RATE
      val discountWithoutVat = order.discount.divide(VAT_RATE, 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP)
      
      // 3. Calculate total after discount WITHOUT VAT: productsTotalWithoutVat - discountWithoutVat
      val totalAfterDiscountBeforeVat = productsTotalWithoutVat.subtract(discountWithoutVat).setScale(2, RoundingMode.HALF_UP)
      
      // 4. Calculate VAT as the difference to ensure exact match with stored totalPrice
      //    This avoids rounding errors: VAT = totalPrice - totalAfterDiscountBeforeVat
      val finalTotalWithVat = totalWithVat.setScale(2, RoundingMode.HALF_UP)
      val vatAmount = finalTotalWithVat.subtract(totalAfterDiscountBeforeVat).setScale(2, RoundingMode.HALF_UP)
      
      // Calculate discount percentage
      val discountPercentage = if (productsTotalWithoutVat > BigDecimal.ZERO) {
        discountWithoutVat.divide(productsTotalWithoutVat, 4, RoundingMode.HALF_UP)
          .multiply(BigDecimal("100"))
          .setScale(1, RoundingMode.HALF_UP)
      } else {
        BigDecimal.ZERO
      }
      
      // Format discount with percentage
      val discountText = "${formatCurrency(discountWithoutVat)} (${currencyFormatter.format(discountPercentage)}%)"
      
      val summaryLines = listOf(
        formatCurrency(productsTotalWithoutVat) to "סה\"כ ללא מע\"מ:",
        discountText to "הנחה:",
        formatCurrency(totalAfterDiscountBeforeVat) to "סה\"כ אחרי הנחה:",
        formatCurrency(vatAmount) to "מע\"מ $VAT_PERCENTAGE%:"
      )
      
      summaryLines.forEach { (price, label) ->
        content.writeText(price, priceX, currentY, theme.regularFont, FONT_SIZE_REGULAR, theme.textColor)
        content.writeTextRightAligned(label, labelRightX, currentY, theme.regularFont, FONT_SIZE_REGULAR, theme.textColor)
        currentY -= if (label.contains("מע\"מ")) 25f else 20f
      }
    } else {
      // Simple breakdown: show total before VAT and VAT
      val totalBeforeVat = totalWithVat.divide(VAT_RATE, 2, RoundingMode.HALF_UP)
      val vatAmount = totalWithVat.subtract(totalBeforeVat)
      
      val summaryLines = listOf(
        formatCurrency(totalBeforeVat) to "סה\"כ לפני מע\"מ:",
        formatCurrency(vatAmount) to "מע\"מ $VAT_PERCENTAGE%:"
      )
      
      summaryLines.forEach { (price, label) ->
        content.writeText(price, priceX, currentY, theme.regularFont, FONT_SIZE_REGULAR, theme.textColor)
        content.writeTextRightAligned(label, labelRightX, currentY, theme.regularFont, FONT_SIZE_REGULAR, theme.textColor)
        currentY -= if (label.contains("מע\"מ")) 25f else 20f
      }
    }

    // Divider
    content.setStrokingColor(theme.dividerColor)
    content.setLineWidth(1.5f)
    content.moveTo(summaryX, currentY + 5f)
    content.lineTo(summaryX + summaryWidth, currentY + 5f)
    content.stroke()

    // Total box
    val totalBoxY = currentY - 35f
    val totalBoxHeight = 25f
    content.setNonStrokingColor(Color(255, 255, 255))
    content.drawRoundedRect(summaryX - 5f, totalBoxY, summaryWidth + 10f, totalBoxHeight, CORNER_RADIUS / 2f, fill = true, stroke = false)
    val totalTextY = totalBoxY + (totalBoxHeight / 2f) - TEXT_BASELINE_OFFSET
    // Use order.totalPrice as the source of truth for final total (avoids rounding errors)
    val finalTotal = totalWithVat.setScale(2, RoundingMode.HALF_UP)
    content.writeText(formatCurrency(finalTotal), priceX, totalTextY, theme.boldFont, 12f, theme.textColor)
    content.writeTextRightAligned("סה\"כ לתשלום:", labelRightX, totalTextY, theme.boldFont, 12f, theme.textColor)

    return totalBoxY - 5f
  }

  private fun drawPaymentDetailsWithPagination(
    document: PDDocument,
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    contentWidth: Float,
    y: Float,
    order: OrderDbEntity,
    paymentMethod: PaymentMethod?,
    paymentProof: String?,
    pageHeight: Float,
    pages: MutableList<PDPage>,
    currentPageIndex: Int
  ): Triple<Float, Int, PDPageContentStream> {
    val paymentFields = mutableListOf("סכום ששולם: ${formatCurrency(order.totalPrice)}")
    paymentMethod?.let { paymentMethodTextMap[it] }?.let { paymentFields.add("אמצעי תשלום: $it") }
    paymentProof?.takeIf { it.isNotEmpty() }?.let { paymentFields.add("אסמכתא: $it") }
    
    // Estimate height: title + fields * lineHeight + padding
    val estimatedHeight = 40f + (paymentFields.size * 22f) + 40f
    
    // Check if we need a new page
    val spaceResult = ensureSpaceForComponent(document, content, y, estimatedHeight, pageHeight, pages, currentPageIndex)
    val currentY = spaceResult.first
    val pageIndex = spaceResult.second
    val currentContent = spaceResult.third
    
    val finalY = drawStyledPanel(currentContent, theme, margin + contentWidth, contentWidth, currentY, "פרטי התשלום", paymentFields,
      useBoldFont = false, lineHeight = 22f, yOffset = -20f, bottomOffset = 20f)
    
    return Triple(finalY, pageIndex, currentContent)
  }

  private fun drawPaymentDetails(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    contentWidth: Float,
    y: Float,
    order: OrderDbEntity,
    paymentMethod: PaymentMethod? = null,
    paymentProof: String? = null
  ): Float {
    val paymentFields = mutableListOf("סכום ששולם: ${formatCurrency(order.totalPrice)}")
    paymentMethod?.let { paymentMethodTextMap[it] }?.let { paymentFields.add("אמצעי תשלום: $it") }
    paymentProof?.takeIf { it.isNotEmpty() }?.let { paymentFields.add("אסמכתא: $it") }
    
    return drawStyledPanel(content, theme, margin + contentWidth, contentWidth, y, "פרטי התשלום", paymentFields,
      useBoldFont = false, lineHeight = 22f, yOffset = -20f, bottomOffset = 20f)
  }

  private fun drawFooterWithPagination(
    document: PDDocument,
    content: PDPageContentStream,
    theme: InvoiceTheme,
    pageWidth: Float,
    y: Float,
    pageHeight: Float,
    pages: MutableList<PDPage>,
    currentPageIndex: Int
  ): Triple<Float, Int, PDPageContentStream> {
    val footerLines = listOf(
      "תודה על העסקה שלך!",
      "מסמך זה מהווה חשבונית מס־קבלה ממוחשבת.",
      "המסמך הופק לאחר קבלת התשלום ואינו דורש חתימה.",
      "חשבונית זו נוצרה אוטומטית על ידי מערכת Order-it."
    )
    
    // Estimate footer height: lines * line spacing + padding
    val estimatedHeight = 10f + (footerLines.size * 16f) + 20f
    
    // Check if we need a new page
    val spaceResult = ensureSpaceForComponent(document, content, y, estimatedHeight, pageHeight, pages, currentPageIndex)
    val currentY = spaceResult.first
    val pageIndex = spaceResult.second
    val currentContent = spaceResult.third
    
    drawFooter(currentContent, theme, pageWidth, currentY)
    
    return Triple(currentY - estimatedHeight, pageIndex, currentContent)
  }

  private fun drawFooter(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    pageWidth: Float,
    y: Float
  ) {
    var currentY = y
    val footerLines = listOf(
      "תודה על העסקה שלך!",
      "מסמך זה מהווה חשבונית מס־קבלה ממוחשבת.",
      "המסמך הופק לאחר קבלת התשלום ואינו דורש חתימה.",
      "חשבונית זו נוצרה אוטומטית על ידי מערכת Order-it."
    )

    // Draw footer background with rounded corners
    val footerHeight = 10f + (footerLines.size * 16f)
    val footerY = currentY - footerHeight
    content.setNonStrokingColor(Color(252, 248, 255)) // Light lavender tint
    content.drawRoundedRect(PAGE_MARGIN, footerY, pageWidth - (PAGE_MARGIN * 2), footerHeight, CORNER_RADIUS, fill = true, stroke = false)

    footerLines.forEachIndexed { index, line ->
      val fontSize = if (index == 0) 11f else FONT_SIZE_SMALL
      val textColor = if (index == 0) Color(70, 70, 70) else Color(120, 120, 120)
      content.writeTextRightAligned(
        line,
        pageWidth - PAGE_MARGIN - 10f,
        currentY,
        if (index == 0) theme.boldFont else theme.regularFont,
        fontSize,
        textColor
      )
      currentY -= 16f
    }
  }

  private fun createTheme(document: PDDocument): InvoiceTheme {
    // Load bundled fonts from classpath (same font everywhere)
    // This ensures consistent rendering across all environments
    val regular = loadFontFromClasspath(document, "fonts/DejaVuSans.ttf")
      ?: throw IllegalStateException("Font file not found: fonts/DejaVuSans.ttf. Make sure fonts are bundled in the JAR.")
    
    val bold = loadFontFromClasspath(document, "fonts/DejaVuSans-Bold.ttf")
      ?: throw IllegalStateException("Font file not found: fonts/DejaVuSans-Bold.ttf. Make sure fonts are bundled in the JAR.")
    
    return InvoiceTheme(
      regularFont = regular,
      boldFont = bold,
      textColor = Color(0, 0, 0),
      borderColor = Color(200, 200, 200),
      tableHeaderBgColor = Color(170, 130, 210), // Darker lavender for table header
      tableHeaderTextColor = Color(255, 255, 255),
      sectionBgColor = Color(248, 240, 255), // Light lavender tint for sections
      dividerColor = Color(189, 195, 199)
    )
  }

  private fun loadFontFromClasspath(document: PDDocument, resourcePath: String): PDFont? {
    return try {
      val inputStream: InputStream? = javaClass.classLoader.getResourceAsStream(resourcePath)
      inputStream?.use { stream ->
        PDType0Font.load(document, stream, true)
      }
    } catch (e: Exception) {
      null
    }
  }

  private fun formatCurrentDate(): String {
    val now = LocalDateTime.now()
    return String.format("%d-%02d-%02d %02d:%02d", now.year, now.monthValue, now.dayOfMonth, now.hour, now.minute)
  }

  private fun formatCurrency(amount: BigDecimal): String = "${currencyFormatter.format(amount)} ₪"
}
