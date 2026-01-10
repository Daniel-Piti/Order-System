package com.pt.ordersystem.ordersystem.domains.invoices

import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDto
import com.pt.ordersystem.ordersystem.domains.invoices.models.PaymentMethod
import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerDbEntity
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.file.Files
import java.nio.file.Paths
import java.text.Bidi
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
  private const val FONT_SIZE_TITLE = 24f
  private const val TEXT_BASELINE_OFFSET = 2.5f


  private data class InvoiceTheme(
    val regularFont: PDFont,
    val boldFont: PDFont,
    val textColor: Color = Color(0, 0, 0),
    val borderColor: Color = Color(200, 200, 200)
  )

  fun renderPdf(
    manager: ManagerDbEntity,
    business: BusinessDto,
    order: OrderDbEntity,
    invoiceSequenceNumber: Int,
    paymentMethod: PaymentMethod? = null,
    paymentProof: String? = null,
    allocationNumber: String? = null
  ): ByteArray {
    val invoiceDate = formatCurrentDate()

    return PDDocument().use { document ->
      document.documentInformation.title = "Invoice invoice-$invoiceSequenceNumber"
      document.documentInformation.author = "Order System"

      val theme = createTheme(document)
      val page = PDPage(PDRectangle.A4)
      document.addPage(page)

      val pageWidth = page.mediaBox.width
      val pageHeight = page.mediaBox.height
      val contentWidth = pageWidth - (PAGE_MARGIN * 2)

      PDPageContentStream(document, page).use { content ->
        var currentY = pageHeight - PAGE_MARGIN

        currentY = drawHeader(content, theme, pageWidth, currentY, order.id, invoiceSequenceNumber, invoiceDate)
        currentY =
          drawCustomerAndBusinessPanels(content, theme, pageWidth, currentY, order, manager, business, allocationNumber)
        currentY = drawProductsTable(content, theme, PAGE_MARGIN, contentWidth, currentY, order)
        currentY = drawSummary(content, theme, PAGE_MARGIN, contentWidth, currentY, order)
        currentY =
          drawPaymentDetails(content, theme, PAGE_MARGIN, contentWidth, currentY, order, paymentMethod, paymentProof)
        drawFooter(content, theme, pageWidth, currentY)
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
    date: String
  ): Float {
    var currentY = y

    content.writeTextRightAligned(
      "חשבונית מס־קבלה",
      pageWidth - PAGE_MARGIN,
      currentY,
      theme.boldFont,
      FONT_SIZE_TITLE,
      theme.textColor
    )
    currentY -= 40f

    content.writeTextRightAligned(
      "תאריך: $date",
      pageWidth - PAGE_MARGIN,
      currentY,
      theme.regularFont,
      FONT_SIZE_REGULAR,
      theme.textColor
    )
    currentY -= 20f

    content.writeTextRightAligned(
      "מספר זיהוי: $orderId",
      pageWidth - PAGE_MARGIN,
      currentY,
      theme.regularFont,
      FONT_SIZE_REGULAR,
      theme.textColor
    )
    currentY -= 20f

    content.writeTextRightAligned(
      "מספר חשבונית: invoice-$invoiceSequenceNumber",
      pageWidth - PAGE_MARGIN,
      currentY,
      theme.regularFont,
      FONT_SIZE_REGULAR,
      theme.textColor
    )
    currentY -= 50f

    return currentY
  }

  private fun drawCustomerAndBusinessPanels(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    pageWidth: Float,
    y: Float,
    order: OrderDbEntity,
    manager: ManagerDbEntity,
    business: BusinessDto,
    allocationNumber: String?
  ): Float {
    val panelWidth = (pageWidth - (PAGE_MARGIN * 2) - PANEL_GAP) / 2f
    val rightPanelX = pageWidth - PAGE_MARGIN
    val leftPanelX = rightPanelX - panelWidth - PANEL_GAP

    val customerAddress = if (order.customerStreetAddress != null && order.customerCity != null) {
      "${order.customerStreetAddress}, ${order.customerCity}"
    } else if (order.customerStreetAddress != null) {
      order.customerStreetAddress
    } else if (order.customerCity != null) {
      order.customerCity
    } else {
      ""
    }

    val customerY = drawPanel(
      content, theme, rightPanelX, panelWidth, y, "פרטי הלקוח", listOf(
        "שם הלקוח: ${order.customerName ?: ""}",
        "כתובת: $customerAddress",
        "ח.פ / ע.מ:"
      )
    )

    val businessAddress = "${business.streetAddress}, ${business.city}"

    val businessFields = mutableListOf(
      "שם העסק / שם העוסק: ${business.name}",
      "כתובת מלאה: $businessAddress",
      "טלפון: ${business.phoneNumber}",
      "ח.פ / ע.מ: ${business.stateIdNumber}"
    )

    allocationNumber?.let { businessFields.add("מספר הקצאה: $it") }

    val businessY = drawPanel(content, theme, leftPanelX, panelWidth, y, "פרטי העסק", businessFields)

    return minOf(customerY, businessY) - 30f
  }

  private fun drawPanel(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    rightX: Float,
    width: Float,
    y: Float,
    title: String,
    fields: List<String>
  ): Float {
    var currentY = y

    content.writeTextRightAligned(title, rightX, currentY, theme.boldFont, 14f, theme.textColor)
    currentY -= 25f

    fields.forEach { field ->
      currentY = content.writeWrappedTextRightAligned(
        text = field,
        rightX = rightX - 10f,
        y = currentY,
        maxWidth = width - 20f,
        font = theme.regularFont,
        fontSize = FONT_SIZE_REGULAR,
        color = theme.textColor,
        lineHeight = 18f
      )
    }

    return currentY
  }

  private fun drawProductsTable(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    contentWidth: Float,
    y: Float,
    order: OrderDbEntity
  ): Float {
    val columnWidths = floatArrayOf(60f, contentWidth - 260f, 100f, 100f)
    var currentY = y

    // Draw header
    content.setStrokingColor(theme.borderColor)
    content.addRect(margin, currentY - ROW_HEIGHT, contentWidth, ROW_HEIGHT)
    content.stroke()

    val headerY = currentY - 18f
    var tableX = margin

    content.writeTextCentered(
      "כמות",
      tableX,
      columnWidths[0],
      headerY,
      theme.boldFont,
      FONT_SIZE_REGULAR,
      theme.textColor
    )
    tableX += columnWidths[0]

    content.writeText("תיאור מוצר", tableX + 5f, headerY, theme.boldFont, FONT_SIZE_REGULAR, theme.textColor)
    tableX += columnWidths[1]

    content.writeTextCentered(
      "מחיר יחידה",
      tableX,
      columnWidths[2],
      headerY,
      theme.boldFont,
      FONT_SIZE_REGULAR,
      theme.textColor
    )
    tableX += columnWidths[2]

    content.writeTextCentered(
      "סה\"כ",
      tableX,
      columnWidths[3],
      headerY,
      theme.boldFont,
      FONT_SIZE_REGULAR,
      theme.textColor
    )
    currentY -= ROW_HEIGHT

    // Draw products
    order.products.forEach { product ->
      val productTotal = product.pricePerUnit.multiply(BigDecimal(product.quantity))
      val productNameLines = content.wrapTextForPdf(
        product.productName.replace("\n", " "),
        columnWidths[1] - 10f,
        theme.regularFont,
        FONT_SIZE_SMALL
      )

      val actualRowHeight = maxOf(ROW_HEIGHT, (productNameLines.size * LINE_SPACING) + (VERTICAL_PADDING * 2f))
      val rowCenterY = (currentY - actualRowHeight) + (actualRowHeight / 2f)
      val textBaselineY = rowCenterY - TEXT_BASELINE_OFFSET

      content.setStrokingColor(theme.borderColor)
      content.addRect(margin, currentY - actualRowHeight, contentWidth, actualRowHeight)
      content.stroke()

      tableX = margin

      content.writeTextCentered(
        product.quantity.toString(),
        tableX,
        columnWidths[0],
        textBaselineY,
        theme.regularFont,
        FONT_SIZE_SMALL,
        theme.textColor
      )
      tableX += columnWidths[0]

      drawMultiLineText(
        content,
        tableX + 5f,
        productNameLines,
        rowCenterY,
        theme.regularFont,
        FONT_SIZE_SMALL,
        theme.textColor
      )
      tableX += columnWidths[1]

      content.writeTextCentered(
        formatCurrency(product.pricePerUnit),
        tableX,
        columnWidths[2],
        textBaselineY,
        theme.regularFont,
        FONT_SIZE_SMALL,
        theme.textColor
      )
      tableX += columnWidths[2]

      content.writeTextCentered(
        formatCurrency(productTotal),
        tableX,
        columnWidths[3],
        textBaselineY,
        theme.regularFont,
        FONT_SIZE_SMALL,
        theme.textColor
      )

      currentY -= actualRowHeight
    }

    return currentY - 30f
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

  private fun drawSummary(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    margin: Float,
    contentWidth: Float,
    y: Float,
    order: OrderDbEntity
  ): Float {
    val totalWithVat = order.totalPrice
    val totalBeforeVat = totalWithVat.divide(VAT_RATE, 2, RoundingMode.HALF_UP)
    val vatAmount = totalWithVat.subtract(totalBeforeVat)

    val columnWidths = floatArrayOf(60f, contentWidth - 260f, 100f, 100f)
    val summaryX = margin + columnWidths[0] + columnWidths[1]
    val summaryWidth = columnWidths[2] + columnWidths[3]
    val priceX = summaryX + 5f
    val labelRightX = summaryX + summaryWidth - 5f
    var currentY = y

    drawSummaryLine(
      content,
      theme,
      priceX,
      labelRightX,
      currentY,
      formatCurrency(totalBeforeVat),
      "סה\"כ לפני מע\"מ:",
      false
    )
    currentY -= 20f

    drawSummaryLine(
      content,
      theme,
      priceX,
      labelRightX,
      currentY,
      formatCurrency(vatAmount),
      "מע\"מ $VAT_PERCENTAGE%:",
      false
    )
    currentY -= 25f

    content.setStrokingColor(theme.borderColor)
    content.setLineWidth(1.5f)
    content.moveTo(summaryX, currentY + 5f)
    content.lineTo(summaryX + summaryWidth, currentY + 5f)
    content.stroke()
    currentY -= 10f

    drawSummaryLine(
      content,
      theme,
      priceX,
      labelRightX,
      currentY,
      formatCurrency(totalWithVat),
      "סה\"כ אחרי מע\"מ:",
      true
    )

    return currentY - 30f
  }

  private fun drawSummaryLine(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    priceX: Float,
    labelRightX: Float,
    y: Float,
    price: String,
    label: String,
    bold: Boolean
  ) {
    val font = if (bold) theme.boldFont else theme.regularFont
    val fontSize = if (bold) 12f else FONT_SIZE_REGULAR

    content.writeText(price, priceX, y, font, fontSize, theme.textColor)
    content.writeTextRightAligned(label, labelRightX, y, font, fontSize, theme.textColor)
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
    var currentY = y - 20f
    val pageWidth = margin + contentWidth

    content.writeTextRightAligned("פרטי התשלום", pageWidth, currentY, theme.boldFont, 14f, theme.textColor)
    currentY -= 25f

    val columnWidths = floatArrayOf(60f, contentWidth - 260f, 100f, 100f)
    val summaryX = margin + columnWidths[0] + columnWidths[1]
    val summaryWidth = columnWidths[2] + columnWidths[3]
    val priceX = summaryX + 5f
    val labelRightX = summaryX + summaryWidth - 5f

    drawSummaryLine(
      content,
      theme,
      priceX,
      labelRightX,
      currentY,
      formatCurrency(order.totalPrice),
      "סכום ששולם:",
      true
    )
    currentY -= 20f

    val paymentMethodText = when (paymentMethod) {
      PaymentMethod.CREDIT_CARD -> "כרטיס אשראי"
      PaymentMethod.CASH -> "מזומן"
      null -> ""
    }

    if (paymentMethodText.isNotEmpty()) {
      drawSummaryLine(content, theme, priceX, labelRightX, currentY, paymentMethodText, "אמצעי תשלום:", false)
      currentY -= 20f
    } else {
      content.writeTextRightAligned(
        "אמצעי תשלום:",
        labelRightX,
        currentY,
        theme.regularFont,
        FONT_SIZE_REGULAR,
        theme.textColor
      )
      currentY -= 20f
    }

    if (paymentProof != null) {
      drawSummaryLine(content, theme, priceX, labelRightX, currentY, paymentProof, "אסמכתא:", false)
      currentY -= 20f
    } else {
      content.writeTextRightAligned(
        "אסמכתא:",
        labelRightX,
        currentY,
        theme.regularFont,
        FONT_SIZE_REGULAR,
        theme.textColor
      )
    }

    return currentY - 30f
  }

  private fun drawFooter(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    pageWidth: Float,
    y: Float
  ) {
    var currentY = y
    val footerLines = listOf(
      "מסמך זה מהווה חשבונית מס־קבלה ממוחשבת.",
      "המסמך הופק לאחר קבלת התשלום ואינו דורש חתימה."
    )

    footerLines.forEach { line ->
      content.writeTextRightAligned(
        line,
        pageWidth - PAGE_MARGIN,
        currentY,
        theme.regularFont,
        FONT_SIZE_SMALL,
        Color(100, 100, 100)
      )
      currentY -= 15f
    }
  }

  private fun createTheme(document: PDDocument): InvoiceTheme {
    val regular = loadFont(document, "arial.ttf") ?: PDType1Font(Standard14Fonts.FontName.HELVETICA)
    val bold = loadFont(document, "arialbd.ttf") ?: PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
    return InvoiceTheme(regularFont = regular, boldFont = bold)
  }

  private fun loadFont(document: PDDocument, fileName: String): PDFont? {
    val candidates = listOfNotNull(
      System.getenv("WINDIR")?.let { Paths.get(it, "Fonts", fileName) },
      Paths.get("/usr/share/fonts/truetype/msttcorefonts", fileName),
      Paths.get("/usr/share/fonts/truetype/microsoft", fileName),
      Paths.get("/usr/share/fonts/truetype/freefont", fileName)
    )

    return candidates.firstOrNull { Files.exists(it) && Files.isReadable(it) }?.let { path ->
      Files.newInputStream(path).use { PDType0Font.load(document, it, true) }
    }
  }

  private fun formatCurrentDate(): String {
    val now = LocalDateTime.now()
    return String.format("%02d/%02d/%d", now.dayOfMonth, now.monthValue, now.year)
  }

  private fun formatCurrency(amount: BigDecimal): String = "${currencyFormatter.format(amount)} ₪"

  private fun getTextWidth(text: String, font: PDFont, fontSize: Float): Float {
    return font.getStringWidth(shapeTextForPdf(text)) / 1000f * fontSize
  }

  private fun shapeTextForPdf(text: String): String {
    if (text.isEmpty()) return text
    val bidi = Bidi(text, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)
    if (bidi.isLeftToRight) return text

    val runCount = bidi.runCount
    val levels = ByteArray(runCount)
    val logicalRuns = Array<String>(runCount) { "" }

    for (i in 0 until runCount) {
      val start = bidi.getRunStart(i)
      val limit = bidi.getRunLimit(i)
      val runText = text.substring(start, limit)
      val level = bidi.getRunLevel(i)
      levels[i] = level.toByte()
      logicalRuns[i] = if (level % 2 == 0) runText else runText.reversed()
    }

    val visualRuns = logicalRuns.copyOf()
    Bidi.reorderVisually(levels, 0, visualRuns, 0, runCount)

    return visualRuns.joinToString("")
  }

  // Extension functions for PDPageContentStream
  private fun PDPageContentStream.writeText(
    text: String,
    x: Float,
    y: Float,
    font: PDFont,
    fontSize: Float,
    color: Color
  ) {
    val shaped = shapeTextForPdf(text)
    setNonStrokingColor(color)
    beginText()
    setFont(font, fontSize)
    newLineAtOffset(x, y)
    showText(shaped)
    endText()
    setNonStrokingColor(Color.BLACK)
  }

  private fun PDPageContentStream.writeTextRightAligned(
    text: String,
    rightX: Float,
    y: Float,
    font: PDFont,
    fontSize: Float,
    color: Color
  ) {
    val textWidth = getTextWidth(text, font, fontSize)
    writeText(text, rightX - textWidth, y, font, fontSize, color)
  }

  private fun PDPageContentStream.writeTextCentered(
    text: String,
    leftX: Float,
    width: Float,
    y: Float,
    font: PDFont,
    fontSize: Float,
    color: Color
  ) {
    val textWidth = getTextWidth(text, font, fontSize)
    writeText(text, leftX + (width / 2f) - (textWidth / 2f), y, font, fontSize, color)
  }

  private fun PDPageContentStream.writeWrappedTextRightAligned(
    text: String,
    rightX: Float,
    y: Float,
    maxWidth: Float,
    font: PDFont,
    fontSize: Float,
    color: Color,
    lineHeight: Float
  ): Float {
    val actualMaxWidth = maxWidth.coerceAtLeast(50f).coerceAtMost(rightX - 50f)
    val lines = wrapTextForPdf(text, actualMaxWidth, font, fontSize)
    var currentY = y

    lines.forEach { line ->
      writeTextRightAligned(line, rightX, currentY, font, fontSize, color)
      currentY -= lineHeight
    }

    return currentY
  }

  private fun PDPageContentStream.wrapTextForPdf(
    text: String,
    maxWidth: Float,
    font: PDFont,
    fontSize: Float
  ): List<String> {
    if (text.isEmpty()) return listOf("")

    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = StringBuilder()

    words.forEach { word ->
      val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
      if (getTextWidth(testLine, font, fontSize) <= maxWidth) {
        if (currentLine.isNotEmpty()) currentLine.append(" ")
        currentLine.append(word)
      } else {
        if (currentLine.isNotEmpty()) {
          lines.add(currentLine.toString())
          currentLine.clear()
        }
        if (getTextWidth(word, font, fontSize) <= maxWidth) {
          currentLine.append(word)
        } else {
          lines.addAll(breakLongWord(word, maxWidth, font, fontSize))
        }
      }
    }

    if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
    return lines.ifEmpty { listOf(text) }
  }


  private fun breakLongWord(word: String, maxWidth: Float, font: PDFont, fontSize: Float): List<String> {
    val chunks = mutableListOf<String>()
    var remaining = word

    while (remaining.isNotEmpty()) {
      var chunk = ""
      for (i in 1..remaining.length) {
        val test = remaining.substring(0, i)
        if (getTextWidth(test, font, fontSize) > maxWidth) break
        chunk = test
      }
      if (chunk.isEmpty()) chunk = remaining.take(1)
      chunks.add(chunk)
      remaining = remaining.substring(chunk.length)
    }

    return chunks
  }
}
