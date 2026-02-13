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
import java.time.format.DateTimeFormatter
import java.util.*

object InvoiceRenderHelper {
  private val currencyFormatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
  private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.forLanguageTag("he-IL"))

  private const val VAT_PERCENTAGE = 18
  private val VAT_RATE = BigDecimal("1.18")

  // Layout constants - more formal spacing
  private const val PAGE_MARGIN = 40f
  private const val SECTION_SPACING = 25f
  private const val LINE_SPACING = 12f
  private const val ROW_HEIGHT = 28f
  private const val VERTICAL_PADDING = 8f
  private const val FONT_SIZE_LARGE = 16f
  private const val FONT_SIZE_MEDIUM = 12f
  private const val FONT_SIZE_REGULAR = 11f
  private const val FONT_SIZE_SMALL = 10f
  private const val TEXT_BASELINE_OFFSET = 3f

  private data class InvoiceTheme(
    val regularFont: PDFont,
    val boldFont: PDFont,
    val textColor: Color = Color(30, 30, 30),
    val borderColor: Color = Color(220, 220, 220),
    val tableHeaderBgColor: Color = Color(147, 112, 219), // Moderate purple/lavender
    val tableHeaderTextColor: Color = Color(255, 255, 255),
    val sectionBgColor: Color = Color(250, 250, 250),
    val dividerColor: Color = Color(200, 200, 200),
    val accentPurple: Color = Color(171, 130, 255), // Moderate lavender accent
    val lightPurpleBg: Color = Color(251, 247, 255), // Light purple background
    val darkPurple: Color = Color(120, 81, 169) // Darker purple for emphasis
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
        // 1. Business Details Section (Header)
        currentY = drawBusinessDetailsSection(
          content, theme, PAGE_MARGIN, contentWidth, currentY, business, invoiceSequenceNumber, invoiceDate, allocationNumber
        )
        currentY -= SECTION_SPACING

        // 2. Customer Section
        currentY = drawCustomerSection(content, theme, PAGE_MARGIN, contentWidth, currentY, order)
        currentY -= SECTION_SPACING

        // 3. Products Table with pagination
        val productsResult = drawProductsTableWithPagination(
          document, content, theme, PAGE_MARGIN, contentWidth, currentY, order,
          pageHeight, pages, currentPageIndex
        )
        currentY = productsResult.first
        content = productsResult.third
        currentPageIndex = productsResult.second
        currentY -= SECTION_SPACING

        // 4. Payment Details Section (Summary)
        val paymentResult = drawPaymentDetailsSectionWithPagination(
          document, content, theme, PAGE_MARGIN, contentWidth, currentY, order, paymentMethod, paymentProof,
          pageHeight, pages, currentPageIndex
        )
        currentY = paymentResult.first
        content = paymentResult.third
        currentPageIndex = paymentResult.second

        // Footer
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

  /**
   * Section 1: Business Details (Formal Header)
   */
  private fun drawBusinessDetailsSection(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    contentWidth: Float,
    y: Float,
    business: BusinessDto,
    invoiceSequenceNumber: Int,
    invoiceDate: String,
    allocationNumber: String?
  ): Float {
    val sectionHeight = 120f
    val sectionBottom = y - sectionHeight
    val sectionRight = margin + contentWidth

    var currentY = y - 25f

    // Title: "חשבונית מס קבלה" - larger and more prominent
    content.writeTextRightAligned(
      "חשבונית מס קבלה",
      sectionRight - 10f,
      currentY,
      theme.boldFont,
      FONT_SIZE_LARGE + 2f,
      theme.textColor
    )
    currentY -= 30f

    // Invoice number - regular color (no accent)
    content.writeTextRightAligned(
      "מספר: $invoiceSequenceNumber",
      sectionRight - 10f,
      currentY,
      theme.boldFont,
      FONT_SIZE_MEDIUM + 1f,
      theme.textColor
    )
    currentY -= 22f

    // Date
    content.writeTextRightAligned(
      "תאריך: $invoiceDate",
      sectionRight - 10f,
      currentY,
      theme.regularFont,
      FONT_SIZE_REGULAR,
      Color(80, 80, 80)
    )
    currentY -= 22f

    // Allocation number if exists - regular color (no accent)
    allocationNumber?.let {
      content.writeTextRightAligned(
        "מספר הקצאה: $it",
        sectionRight - 10f,
        currentY,
        theme.regularFont,
        FONT_SIZE_REGULAR,
        theme.textColor
      )
      currentY -= 20f
    }

    // Business details (left side) - with modern styling
    val businessY = y - 25f
    val businessLeft = margin + 10f
    val businessMaxWidth = (contentWidth / 2f) - 20f // Available width for business section
    
    // Business title "עסק" - with purple accent
    content.writeText(
      "עסק",
      businessLeft,
      businessY,
      theme.boldFont,
      FONT_SIZE_MEDIUM + 1f,
      theme.darkPurple
    )
    var businessLineY = businessY - 22f
    
    // Business name with wrapping
    val businessNameLines = content.wrapTextForPdf(
      business.name,
      businessMaxWidth,
      theme.boldFont,
      FONT_SIZE_MEDIUM + 1f
    )
    businessNameLines.forEach { line ->
      content.writeText(
        line,
        businessLeft,
        businessLineY,
        theme.boldFont,
        FONT_SIZE_MEDIUM + 1f,
        theme.textColor
      )
      businessLineY -= 20f
    }

    // Business address with wrapping
    val businessAddress = "${business.streetAddress}, ${business.city}"
    val addressLines = content.wrapTextForPdf(
      businessAddress,
      businessMaxWidth,
      theme.regularFont,
      FONT_SIZE_REGULAR
    )
    addressLines.forEach { line ->
      content.writeText(
        line,
        businessLeft,
        businessLineY,
        theme.regularFont,
        FONT_SIZE_REGULAR,
        Color(60, 60, 60)
      )
      businessLineY -= 18f
    }

    // Phone with wrapping
    val phoneText = "טלפון: ${business.phoneNumber}"
    val phoneLines = content.wrapTextForPdf(
      phoneText,
      businessMaxWidth,
      theme.regularFont,
      FONT_SIZE_REGULAR
    )
    phoneLines.forEach { line ->
      content.writeText(
        line,
        businessLeft,
        businessLineY,
        theme.regularFont,
        FONT_SIZE_REGULAR,
        Color(60, 60, 60)
      )
      businessLineY -= 18f
    }

    // State ID with wrapping
    val stateIdText = "ח.פ / ע.מ: ${business.stateIdNumber}"
    val stateIdLines = content.wrapTextForPdf(
      stateIdText,
      businessMaxWidth,
      theme.regularFont,
      FONT_SIZE_REGULAR
    )
    stateIdLines.forEach { line ->
      content.writeText(
        line,
        businessLeft,
        businessLineY,
        theme.regularFont,
        FONT_SIZE_REGULAR,
        Color(60, 60, 60)
      )
      businessLineY -= 18f
    }

    return sectionBottom
  }

  /**
   * Section 2: Customer Details
   */
  private fun drawCustomerSection(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    contentWidth: Float,
    y: Float,
    order: OrderDbEntity
  ): Float {
    val sectionTitleHeight = 25f
    val fieldSpacing = 18f
    val padding = 15f
    val customerMaxWidth = (contentWidth / 2f) - 20f // Available width for customer section

    val customerFields = mutableListOf<String>()
    order.customerName?.let { customerFields.add("שם: $it") }
    
    val customerAddress = listOfNotNull(
      order.customerStreetAddress,
      order.customerCity
    ).joinToString(", ")
    if (customerAddress.isNotEmpty()) {
      customerFields.add("כתובת: $customerAddress")
    }
    
    order.customerPhone?.let { customerFields.add("טלפון: $it") }
    order.customerEmail?.let { customerFields.add("אימייל: $it") }
    order.customerStateId?.let { customerFields.add("ח.פ / ע.מ: $it") }

    // Calculate total height including wrapped text
    var totalFieldsHeight = 0f
    customerFields.forEach { field ->
      val wrappedLines = content.wrapTextForPdf(
        field,
        customerMaxWidth,
        theme.regularFont,
        FONT_SIZE_REGULAR
      )
      totalFieldsHeight += wrappedLines.size * fieldSpacing
    }
    
    val sectionHeight = sectionTitleHeight + padding + totalFieldsHeight + padding
    val sectionBottom = y - sectionHeight
    val sectionRight = margin + contentWidth

    // Section title with modern styling
    val titleY = y - (sectionTitleHeight / 2f) - TEXT_BASELINE_OFFSET
    
    content.writeTextRightAligned(
      "לכבוד",
      sectionRight - 15f,
      titleY,
      theme.boldFont,
      FONT_SIZE_MEDIUM + 1f,
      theme.textColor
    )

    // Customer fields with wrapping
    var fieldY = y - sectionTitleHeight - padding - TEXT_BASELINE_OFFSET
    customerFields.forEach { field ->
      val wrappedLines = content.wrapTextForPdf(
        field,
        customerMaxWidth,
        theme.regularFont,
        FONT_SIZE_REGULAR
      )
      wrappedLines.forEach { line ->
        content.writeTextRightAligned(
          line,
          sectionRight - 10f,
          fieldY,
          theme.regularFont,
          FONT_SIZE_REGULAR,
          Color(50, 50, 50)
        )
        fieldY -= fieldSpacing
      }
    }

    return sectionBottom
  }

  /**
   * Section 3: Products Table with pagination
   */
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
    val minYForContent = PAGE_MARGIN + 100f // Reserve space for summary and footer (reduced from 200f)

    // Draw table header
    val headerHeight = 32f
    var headerY = currentY - headerHeight

    // Check if we need a new page for header
    if (currentY - headerHeight < minYForContent) {
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

    // Draw products with pagination
    order.products.forEachIndexed { index, product ->
      val productTotal = product.pricePerUnit.multiply(BigDecimal(product.quantity))

      // Calculate row height
      val productNameText = product.productName.replace("\n", " ")
      val tempLines = currentContent.wrapTextForPdf(
        productNameText,
        columnWidths[1] - 10f,
        theme.regularFont,
        FONT_SIZE_SMALL
      )
      val estimatedRowHeight = maxOf(ROW_HEIGHT, (tempLines.size * LINE_SPACING) + (VERTICAL_PADDING * 2f))

      // Check if we need a new page
      if (currentY - estimatedRowHeight < minYForContent) {
        try {
          currentContent.close()
        } catch (e: Exception) {
          // Stream might already be closed
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

      // Recalculate with current content stream
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

    return Triple(currentY - 20f, pageIndex, currentContent)
  }

  private fun getColumnWidths(contentWidth: Float) = floatArrayOf(50f, contentWidth - 280f, 115f, 115f)

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
    // Header background - purple/lavender
    content.setNonStrokingColor(theme.tableHeaderBgColor)
    content.addRect(margin, headerY, contentWidth, headerHeight)
    content.fill()

    // Header border - darker purple
    content.setStrokingColor(theme.darkPurple)
    content.setLineWidth(1.5f)
    content.addRect(margin, headerY, contentWidth, headerHeight)
    content.stroke()

    val headerTextY = currentY - (headerHeight / 2f) - TEXT_BASELINE_OFFSET
    var tableX = margin

    // Header text in white, slightly larger
    content.writeTextCentered("כמות", tableX, columnWidths[0], headerTextY, theme.boldFont, FONT_SIZE_REGULAR + 0.5f, theme.tableHeaderTextColor)
    tableX += columnWidths[0]
    content.writeText("תיאור", tableX + 5f, headerTextY, theme.boldFont, FONT_SIZE_REGULAR + 0.5f, theme.tableHeaderTextColor)
    tableX += columnWidths[1]
    content.writeTextCentered("מחיר יחידה", tableX, columnWidths[2], headerTextY, theme.boldFont, FONT_SIZE_REGULAR + 0.5f, theme.tableHeaderTextColor)
    tableX += columnWidths[2]
    content.writeTextCentered("סה\"כ", tableX, columnWidths[3], headerTextY, theme.boldFont, FONT_SIZE_REGULAR + 0.5f, theme.tableHeaderTextColor)
  }

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

    // Alternate row background with light purple/lavender tint
    if (isEvenRow) {
      content.setNonStrokingColor(theme.lightPurpleBg)
      content.addRect(margin, rowBottom, contentWidth, rowHeight)
      content.fill()
    }

    // Draw row border - lighter and more modern
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

  /**
   * Section 4: Payment Details (Formal Summary)
   */
  private fun drawPaymentDetailsSectionWithPagination(
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
    val hasDiscount = order.discount > BigDecimal.ZERO
    val summaryHeight = if (hasDiscount) 180f else 140f

    // Check if we need a new page
    val spaceResult = ensureSpaceForComponent(document, content, y, summaryHeight, pageHeight, pages, currentPageIndex)
    var currentY = spaceResult.first
    var pageIndex = spaceResult.second
    var currentContent = spaceResult.third

    return Triple(drawPaymentDetailsSection(currentContent, theme, margin, contentWidth, currentY, order, paymentMethod, paymentProof), pageIndex, currentContent)
  }

  private fun drawPaymentDetailsSection(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    contentWidth: Float,
    y: Float,
    order: OrderDbEntity,
    paymentMethod: PaymentMethod?,
    paymentProof: String?
  ): Float {
    val totalWithVat = order.totalPrice
    val columnWidths = getColumnWidths(contentWidth)
    val summaryX = margin + columnWidths[0] + columnWidths[1]
    val summaryWidth = columnWidths[2] + columnWidths[3]
    val priceX = summaryX + 10f
    val labelRightX = summaryX + summaryWidth - 10f

    val hasDiscount = order.discount > BigDecimal.ZERO

    // Summary lines
    var currentY = y - 20f

    if (hasDiscount) {
      val productsTotalWithVatBeforeDiscount = totalWithVat.add(order.discount)
      val productsTotalWithoutVat = productsTotalWithVatBeforeDiscount.divide(VAT_RATE, 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP)
      val discountWithoutVat = order.discount.divide(VAT_RATE, 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP)
      val totalAfterDiscountBeforeVat = productsTotalWithoutVat.subtract(discountWithoutVat).setScale(2, RoundingMode.HALF_UP)
      val finalTotalWithVat = totalWithVat.setScale(2, RoundingMode.HALF_UP)
      val vatAmount = finalTotalWithVat.subtract(totalAfterDiscountBeforeVat).setScale(2, RoundingMode.HALF_UP)

      val discountPercentage = if (productsTotalWithoutVat > BigDecimal.ZERO) {
        discountWithoutVat.divide(productsTotalWithoutVat, 4, RoundingMode.HALF_UP)
          .multiply(BigDecimal("100"))
          .setScale(1, RoundingMode.HALF_UP)
      } else {
        BigDecimal.ZERO
      }

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

    // Divider line - with purple/lavender accent
    content.setStrokingColor(theme.accentPurple)
    content.setLineWidth(2f)
    content.moveTo(summaryX, currentY + 5f)
    content.lineTo(summaryX + summaryWidth, currentY + 5f)
    content.stroke()

    // Calculate text widths for positioning
    val finalTotal = totalWithVat.setScale(2, RoundingMode.HALF_UP)
    val labelText = "סה\"כ לתשלום:"
    val labelFontSize = FONT_SIZE_MEDIUM + 1f
    val priceText = formatCurrency(finalTotal)
    val priceFontSize = FONT_SIZE_MEDIUM + 2f
    
    val labelWidth = getTextWidth(labelText, theme.boldFont, labelFontSize)
    val priceWidth = getTextWidth(priceText, theme.boldFont, priceFontSize)
    
    // Calculate spacing between label and price
    val spacing = 35f // Space between label and price
    
    // Position text - no box, just text
    val totalTextY = currentY - 40f
    val totalLabelRightX = summaryX + summaryWidth - 10f
    
    // Write label first (right-aligned)
    content.writeTextRightAligned(labelText, totalLabelRightX, totalTextY, theme.boldFont, labelFontSize, theme.textColor)
    
    // Write price: position it to the left of the label with spacing
    // Label LEFT edge is at: totalLabelRightX - labelWidth
    // Price RIGHT edge should be at: totalLabelRightX - labelWidth - spacing
    // Price LEFT edge should be at: totalLabelRightX - labelWidth - spacing - priceWidth
    val priceRightEdge = totalLabelRightX - labelWidth - spacing
    val totalPriceX = priceRightEdge - priceWidth
    content.writeText(priceText, totalPriceX, totalTextY, theme.boldFont, priceFontSize, theme.textColor)

    // Payment method and proof
    var paymentY = totalTextY - 25f
    paymentMethod?.let {
      content.writeTextRightAligned(
        "אמצעי תשלום: ${paymentMethodTextMap[it] ?: it.name}",
        margin + contentWidth - 10f,
        paymentY,
        theme.regularFont,
        FONT_SIZE_REGULAR,
        theme.textColor
      )
      paymentY -= 18f
    }

    paymentProof?.takeIf { it.isNotEmpty() }?.let {
      content.writeTextRightAligned(
        "אסמכתא: $it",
        margin + contentWidth - 10f,
        paymentY,
        theme.regularFont,
        FONT_SIZE_REGULAR,
        theme.textColor
      )
      paymentY -= 18f // Update paymentY after writing proof
    }

    // Add extra spacing after payment details before footer
    val spacingAfterPayment = 30f
    return minOf(totalTextY - 5f, paymentY - spacingAfterPayment)
  }

  private fun ensureSpaceForComponent(
    document: PDDocument,
    content: PDPageContentStream,
    currentY: Float,
    requiredHeight: Float,
    pageHeight: Float,
    pages: MutableList<PDPage>,
    currentPageIndex: Int,
    minSpaceThreshold: Float = PAGE_MARGIN + 80f // Reduced threshold to use more of the page
  ): Triple<Float, Int, PDPageContentStream> {
    var newY = currentY
    var newPageIndex = currentPageIndex
    var newContent = content

    if (currentY - requiredHeight < minSpaceThreshold) {
      try {
        content.close()
      } catch (e: Exception) {
        // Stream might already be closed
      }

      val newPage = PDPage(PDRectangle.A4)
      document.addPage(newPage)
      pages.add(newPage)
      newPageIndex++
      newContent = PDPageContentStream(document, newPage)
      newY = pageHeight - PAGE_MARGIN
    }

    return Triple(newY, newPageIndex, newContent)
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
      "המסמך הופק לאחר קבלת התשלום ואינו דורש חתימה."
    )

    val estimatedHeight = 10f + (footerLines.size * 14f) + 20f

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
      "המסמך הופק לאחר קבלת התשלום ואינו דורש חתימה."
    )

    // Footer with subtle purple/lavender accent background
    val footerHeight = 10f + (footerLines.size * 16f)
    val footerY = currentY - footerHeight
    
    content.setNonStrokingColor(theme.lightPurpleBg)
    content.addRect(PAGE_MARGIN, footerY, pageWidth - (PAGE_MARGIN * 2), footerHeight)
    content.fill()

    footerLines.forEachIndexed { index, line ->
      val fontSize = if (index == 0) FONT_SIZE_REGULAR else FONT_SIZE_SMALL
      val textColor = if (index == 0) theme.darkPurple else Color(100, 100, 100)
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
    val regular = loadFontFromClasspath(document, "fonts/DejaVuSans.ttf")
      ?: throw IllegalStateException("Font file not found: fonts/DejaVuSans.ttf")

    val bold = loadFontFromClasspath(document, "fonts/DejaVuSans-Bold.ttf")
      ?: throw IllegalStateException("Font file not found: fonts/DejaVuSans-Bold.ttf")

    return InvoiceTheme(
      regularFont = regular,
      boldFont = bold,
      textColor = Color(30, 30, 30),
      borderColor = Color(220, 220, 220),
      tableHeaderBgColor = Color(147, 112, 219), // Moderate purple/lavender
      tableHeaderTextColor = Color(255, 255, 255),
      sectionBgColor = Color(250, 250, 250),
      dividerColor = Color(200, 200, 200),
      accentPurple = Color(171, 130, 255), // Moderate lavender accent
      lightPurpleBg = Color(251, 247, 255), // Light purple background
      darkPurple = Color(120, 81, 169) // Darker purple for emphasis
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
    return now.format(dateFormatter)
  }

  private fun formatCurrency(amount: BigDecimal): String = "${currencyFormatter.format(amount)} ₪"
}
