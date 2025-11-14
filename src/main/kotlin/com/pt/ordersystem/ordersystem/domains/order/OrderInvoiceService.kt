package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.agent.AgentService
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import com.pt.ordersystem.ordersystem.domains.order.models.OrderFailureReason
import com.pt.ordersystem.ordersystem.domains.order.models.OrderStatus
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.text.Bidi

@Service
class OrderInvoiceService(
  private val orderRepository: OrderRepository,
  private val managerService: ManagerService,
  private val agentService: AgentService
) {

  private data class InvoiceTheme(
    val regularFont: PDFont,
    val boldFont: PDFont,
    val accentColor: Color = Color(109, 40, 217),
    val accentColorMuted: Color = Color(167, 139, 250),
    val backgroundColor: Color = Color.WHITE,
    val textPrimary: Color = Color(17, 24, 39),
    val textSecondary: Color = Color(75, 85, 99),
    val borderColor: Color = Color(229, 231, 235),
    val panelBackground: Color = Color(248, 250, 252),
    val tableHeaderBackground: Color = Color(245, 243, 255),
    val tableStripeBackground: Color = Color(250, 245, 255)
  )

  data class InvoiceDocument(val fileName: String, val content: ByteArray)

  private val currencyFormatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))

  fun generateInvoiceForUser(orderId: String, requestingUserId: String): InvoiceDocument {
    val order = orderRepository.findById(orderId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = OrderFailureReason.NOT_FOUND.userMessage,
        technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
        severity = SeverityLevel.WARN
      )
    }

    validateOwnership(order, requestingUserId)
    validateInvoiceEligibility(order)

    val fileName = "invoice-${order.id}.pdf"
    val bytes = renderPdf(order)

    return InvoiceDocument(fileName = fileName, content = bytes)
  }

  fun generateInvoiceForAdmin(orderId: String): InvoiceDocument {
    val order = orderRepository.findById(orderId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = OrderFailureReason.NOT_FOUND.userMessage,
        technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
        severity = SeverityLevel.WARN
      )
    }

    validateInvoiceEligibility(order)

    val fileName = "invoice-${order.id}.pdf"
    val bytes = renderPdf(order)

    return InvoiceDocument(fileName = fileName, content = bytes)
  }

  private fun validateOwnership(order: OrderDbEntity, requestingUserId: String) {
    if (order.managerId != requestingUserId) {
      throw ServiceException(
        status = HttpStatus.FORBIDDEN,
        userMessage = OrderFailureReason.UNAUTHORIZED.userMessage,
        technicalMessage = OrderFailureReason.UNAUTHORIZED.technical +
          "orderId=${order.id}, ownerId=${order.managerId}, requester=$requestingUserId",
        severity = SeverityLevel.WARN
      )
    }
  }

  private fun validateInvoiceEligibility(order: OrderDbEntity) {
    val status = OrderStatus.valueOf(order.status)

    if (status != OrderStatus.DONE) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Invoice is only available for completed orders",
        technicalMessage = "Invoice requested for order ${order.id} with status ${order.status}",
        severity = SeverityLevel.INFO
      )
    }

    if (order.products.isEmpty()) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Cannot generate invoice for an order without products",
        technicalMessage = "Invoice requested for order ${order.id} with empty products list",
        severity = SeverityLevel.INFO
      )
    }
  }

  private data class PageContext(
    val page: PDPage,
    val content: PDPageContentStream,
    val pageWidth: Float,
    val pageHeight: Float,
    val margin: Float,
    var currentY: Float,
    var footerDrawn: Boolean = false
  )

  private fun createPage(
    document: PDDocument,
    theme: InvoiceTheme,
    invoiceNumber: String,
    invoiceDate: String,
    businessName: String,
    headerHeight: Float,
    margin: Float
  ): PageContext {
    val page = PDPage(PDRectangle.LETTER)
    document.addPage(page)

    val pageWidth = page.mediaBox.width
    val pageHeight = page.mediaBox.height
    val content = PDPageContentStream(document, page)

    drawHeader(
      content = content,
      theme = theme,
      pageWidth = pageWidth,
      pageHeight = pageHeight,
      height = headerHeight,
      invoiceNumber = invoiceNumber,
      invoiceDate = invoiceDate,
      businessName = businessName
    )

    return PageContext(
      page = page,
      content = content,
      pageWidth = pageWidth,
      pageHeight = pageHeight,
      margin = margin,
      currentY = pageHeight - headerHeight - 28f
    )
  }

  private fun drawFooter(
    context: PageContext,
    theme: InvoiceTheme
  ) {
    if (context.footerDrawn) return

    val footerY = 42f
    val margin = context.margin
    context.content.writeText(
      text = "Thank you for your business!",
      x = margin,
      y = footerY + 12f,
      font = theme.boldFont,
      fontSize = 12f,
      color = theme.textSecondary
    )
    context.content.writeText(
      text = "Invoice generated automatically by Order System.",
      x = margin,
      y = footerY,
      font = theme.regularFont,
      fontSize = 10f,
      color = theme.textSecondary
    )
    context.footerDrawn = true
  }

  private fun renderPdf(order: OrderDbEntity): ByteArray {
    // Fetch manager and agent information
    val manager = managerService.getManagerById(order.managerId)
    val agent = order.agentId?.let { agentId ->
      try {
        agentService.getAgentEntity(agentId.toString())
      } catch (e: Exception) {
        // Agent might not exist anymore, ignore
        null
      }
    }

    val invoiceNumber = "INV-${order.id.uppercase(Locale.ENGLISH)}"
    val invoiceDate = formatDate(order.doneAt ?: order.placedAt ?: order.createdAt)

    PDDocument().use { document ->
      document.documentInformation.title = "Invoice $invoiceNumber"
      document.documentInformation.author = "Order System"

      val theme = createTheme(document)
      val margin = 50f
      val headerHeight = 100f // Increased height for business name
      val footerHeight = 70f
      val contentWidth = PDRectangle.LETTER.width - (margin * 2)

      var context = createPage(
        document = document,
        theme = theme,
        invoiceNumber = invoiceNumber,
        invoiceDate = invoiceDate,
        businessName = manager.businessName,
        headerHeight = headerHeight,
        margin = margin
      )

      fun newPage(): PageContext {
        drawFooter(context, theme)
        context.content.close()
        context = createPage(
          document = document,
          theme = theme,
          invoiceNumber = invoiceNumber,
          invoiceDate = invoiceDate,
          businessName = manager.businessName,
          headerHeight = headerHeight,
          margin = margin
        )
        return context
      }

      fun ensureSpace(requiredHeight: Float): Boolean {
        if (context.currentY - requiredHeight < footerHeight) {
          newPage()
          return true
        }
        return false
      }

      val metaSpacing = 14f
      val metaLines = listOf(
        "Order ID: ${order.id}",
        "Date: ${formatDate(order.doneAt!!)}"
      )
      val metaHeight = metaSpacing * metaLines.size + 12f
      ensureSpace(metaHeight)
      metaLines.forEachIndexed { index, line ->
        context.content.writeText(
          text = line,
          x = margin,
          y = context.currentY - (metaSpacing * index),
          font = theme.regularFont,
          fontSize = 11f,
          color = theme.textSecondary
        )
      }
      context.currentY -= metaHeight + 20f

      val panelGap = 16f
      val panelWidth = (contentWidth - panelGap) / 2f
      val panelPadding = 18f
      val panelLineHeight = 15f
      val panelInnerWidth = panelWidth - (panelPadding * 2)

      // Build seller lines with manager and agent information
      val sellerLines = mutableListOf<String>()
      
      // Business name (bold, will be handled separately)
      // Manager name
      sellerLines.add("${manager.firstName} ${manager.lastName}")
      sellerLines.add("Email: ${manager.email}")
      sellerLines.add("Phone: ${manager.phoneNumber}")
      sellerLines.add("${manager.streetAddress}")
      sellerLines.add("${manager.city}")
      
      // Agent information if exists
      if (agent != null) {
        sellerLines.add("")
        sellerLines.add("Agent: ${agent.firstName} ${agent.lastName}")
        sellerLines.add("Email: ${agent.email}")
        sellerLines.add("Phone: ${agent.phoneNumber}")
      }
      
      // Store (pickup location) information
      if (order.storeStreetAddress != null || order.storeCity != null || order.storePhoneNumber != null) {
        sellerLines.add("")
        sellerLines.add("Location:")
        if (order.storeStreetAddress != null) {
          sellerLines.add("${order.storeStreetAddress}")
        }
        if (order.storeCity != null) {
          sellerLines.add("${order.storeCity}")
        }
        if (order.storePhoneNumber != null) {
          sellerLines.add("Phone: ${order.storePhoneNumber}")
        }
      }
      
      val wrappedSellerLines = sellerLines.flatMap { line ->
        if (line.isBlank()) {
          listOf("")
        } else {
          wrapWithFont(theme.regularFont, line, panelInnerWidth, 11f)
        }
      }

      val customerLines = listOf(
        sanitize(order.customerName)?.takeIf { it != "Not provided" }?.let { "Name: $it" },
        sanitize(order.customerStreetAddress)?.takeIf { it != "Not provided" }?.let { it },
        sanitize(order.customerCity)?.takeIf { it != "Not provided" }?.let { it },
        sanitize(order.customerPhone)?.takeIf { it != "Not provided" }?.let { "Phone: $it" },
        sanitize(order.customerEmail)?.takeIf { it != "Not provided" }?.let { "Email: $it" }
      ).filterNotNull().flatMap { wrapWithFont(theme.regularFont, it, panelInnerWidth, 11f) }

      // Calculate heights - seller panel includes business name as title
      val businessNameTitleHeight = 20f // Extra height for business name
      val sellerContentHeight = panelPadding * 2 + wrappedSellerLines.size * panelLineHeight + businessNameTitleHeight
      val customerContentHeight = panelPadding * 2 + (customerLines.size + 1) * panelLineHeight
      val panelHeight = maxOf(sellerContentHeight, customerContentHeight)

      ensureSpace(panelHeight)
      val sellerBottom = context.content.drawSellerPanel(
        theme = theme,
        businessName = manager.businessName,
        lines = wrappedSellerLines,
        topLeftX = margin,
        topY = context.currentY,
        width = panelWidth
      )

      val customerBottom = context.content.drawInfoPanel(
        theme = theme,
        title = "Customer",
        lines = customerLines,
        topLeftX = margin + panelWidth + panelGap,
        topY = context.currentY,
        width = panelWidth
      )

      context.currentY = minOf(sellerBottom, customerBottom) - 32f

      val tableColumnWidths = floatArrayOf(
        70f,
        contentWidth - 70f - 95f - 95f,
        95f,
        95f
      )
      val tableHeaders = listOf("Qty", "Item", "Unit Price", "Line Total")
      val tableHeaderHeight = 26f
      val tableLineSpacing = 3f
      val tableFontSize = 11f

      fun drawTableHeader() {
        ensureSpace(tableHeaderHeight)
        val topY = context.currentY
        context.content.setNonStrokingColor(theme.tableHeaderBackground)
        context.content.addRect(margin, topY - tableHeaderHeight, tableColumnWidths.sum(), tableHeaderHeight)
        context.content.fill()

        var textX = margin + 12f
        tableHeaders.forEachIndexed { index, header ->
          context.content.writeText(
            text = header,
            x = textX,
            y = topY - tableHeaderHeight + 8f,
            font = theme.boldFont,
            fontSize = 11f,
            color = theme.textPrimary
          )
          textX += tableColumnWidths[index]
        }

        context.content.setStrokingColor(theme.borderColor)
        context.content.addRect(margin, topY - tableHeaderHeight, tableColumnWidths.sum(), tableHeaderHeight)
        context.content.stroke()
        context.currentY -= tableHeaderHeight
      }

      drawTableHeader()

      order.products.forEachIndexed { index, product ->
        val rowValues = listOf(
          product.quantity.toString(),
          product.productName.replace("\n", " "),
          formatCurrency(product.pricePerUnit),
          formatCurrency(product.pricePerUnit.multiply(BigDecimal(product.quantity)))
        )

        val wrappedColumns = rowValues.mapIndexed { columnIndex, value ->
          val maxWidth = tableColumnWidths[columnIndex] - 24f
          wrapWithFont(theme.regularFont, value, maxWidth, tableFontSize)
        }

        val maxLines = wrappedColumns.maxOf { it.size.coerceAtLeast(1) }
        val rowHeight = maxLines * (tableFontSize + tableLineSpacing) + 14f

        if (ensureSpace(rowHeight)) {
          drawTableHeader()
        }

        val rowTop = context.currentY
        val rowBottom = rowTop - rowHeight

        if (index % 2 == 0) {
          context.content.setNonStrokingColor(theme.tableStripeBackground)
          context.content.addRect(margin, rowBottom, tableColumnWidths.sum(), rowHeight)
          context.content.fill()
        }

        var cellX = margin + 12f
        wrappedColumns.forEachIndexed { columnIndex, lines ->
          val textHeight = tableFontSize + tableLineSpacing
          val totalTextHeight = lines.size * textHeight
          val verticalPadding = (rowHeight - totalTextHeight) / 2
          val baselineAdjustment = tableFontSize * 0.75f
          lines.forEachIndexed { lineIdx, line ->
            val lineY = rowBottom + verticalPadding + tableFontSize - baselineAdjustment + lineIdx * textHeight
            context.content.writeText(
              text = line,
              x = cellX,
              y = lineY,
              font = theme.regularFont,
              fontSize = tableFontSize,
              color = theme.textPrimary
            )
          }
          cellX += tableColumnWidths[columnIndex]
        }

        context.content.setStrokingColor(theme.borderColor)
        context.content.moveTo(margin, rowBottom)
        context.content.lineTo(margin + tableColumnWidths.sum(), rowBottom)
        context.content.stroke()

        context.currentY = rowBottom
      }

      context.currentY -= 24f

      val summaryWidth = tableColumnWidths[2] + tableColumnWidths[3]
      val summaryX = margin + tableColumnWidths[0] + tableColumnWidths[1]
      val summaryHeight = 78f
      ensureSpace(summaryHeight)
      val summaryBottom = context.content.drawSummaryCard(
        theme = theme,
        topY = context.currentY,
        leftX = summaryX,
        width = summaryWidth,
        total = formatCurrency(order.totalPrice)
      )
      context.currentY = summaryBottom - 28f

      if (order.notes.isNotBlank()) {
        val noteLines = wrapWithFont(theme.regularFont, order.notes, contentWidth - 32f, 11f)
        val notesPadding = 16f
        val notesLineHeight = 14f
        var index = 0

        while (index < noteLines.size) {
          val availableHeight = context.currentY - footerHeight
          val capacity = ((availableHeight - notesPadding * 2 - notesLineHeight) / notesLineHeight).toInt()
            .coerceAtLeast(1)

          if (capacity <= 0) {
            newPage()
            continue
          }

          val endIndex = minOf(index + capacity, noteLines.size)
          val chunk = noteLines.subList(index, endIndex)
          val blockHeight = notesPadding * 2 + (chunk.size + 1) * notesLineHeight
          ensureSpace(blockHeight)

          val bottom = context.content.drawNotes(
            theme = theme,
            topY = context.currentY,
            leftX = margin,
            width = contentWidth,
            notes = chunk
          )
          context.currentY = bottom - 24f
          index = endIndex
        }
      }

      drawFooter(context, theme)
      context.content.close()

      val output = ByteArrayOutputStream()
      document.save(output)
      return output.toByteArray()
    }
  }

  private fun createTheme(document: PDDocument): InvoiceTheme {
    val regular = loadFont(document, "arial.ttf")
      ?: PDType1Font(Standard14Fonts.FontName.HELVETICA)
    val bold = loadFont(document, "arialbd.ttf")
      ?: PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)

    return InvoiceTheme(
      regularFont = regular,
      boldFont = bold
    )
  }

  private fun loadFont(document: PDDocument, fileName: String): PDFont? {
    val candidates = mutableListOf<Path>()

    System.getenv("WINDIR")?.let { windir ->
      candidates.add(Paths.get(windir, "Fonts", fileName))
    }

    candidates.add(Paths.get("/usr/share/fonts/truetype/msttcorefonts", fileName))
    candidates.add(Paths.get("/usr/share/fonts/truetype/microsoft", fileName))
    candidates.add(Paths.get("/usr/share/fonts/truetype/freefont", fileName))

    return candidates.firstOrNull { Files.exists(it) && Files.isReadable(it) }?.let { path ->
      Files.newInputStream(path).use { input ->
        PDType0Font.load(document, input, true)
      }
    }
  }

  private fun formatDate(dateTime: LocalDateTime): String =
    dateTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH))

  private fun sanitize(value: String?): String =
    value?.takeIf { it.isNotBlank() } ?: "Not provided"

  private fun wrap(text: String, maxLineLength: Int): List<String> {
    val words = text.split("\\s+".toRegex()).filter { it.isNotEmpty() }
    if (words.isEmpty()) return emptyList()

    val lines = mutableListOf<String>()
    val currentLine = StringBuilder()

    for (word in words) {
      if (currentLine.isEmpty()) {
        currentLine.append(word)
      } else if (currentLine.length + 1 + word.length <= maxLineLength) {
        currentLine.append(' ').append(word)
      } else {
        lines.add(currentLine.toString())
        currentLine.clear()
        currentLine.append(word)
      }
    }

    if (currentLine.isNotEmpty()) {
      lines.add(currentLine.toString())
    }

    return lines
  }

  private fun fitTextToWidth(font: PDFont, fontSize: Float, maxWidth: Float, text: String): String {
    if (text.isBlank()) return ""
    val ellipsis = ""
    var candidate = text

    while (candidate.isNotEmpty()) {
      val shaped = shapeTextForPdf(candidate)
      val width = font.getStringWidth(shaped) / 1000 * fontSize
      if (width <= maxWidth) return candidate

      if (candidate.length <= ellipsis.length) {
        return ellipsis.takeIf {
          val shapedEllipsis = shapeTextForPdf(it)
          font.getStringWidth(shapedEllipsis) / 1000 * fontSize <= maxWidth
        } ?: ""
      }

      candidate = candidate.dropLast(1)
      if (!candidate.endsWith(ellipsis)) {
        candidate = candidate.dropLast(ellipsis.length.coerceAtMost(candidate.length)) + ellipsis
      }
    }

    return ""
  }

  private fun wrapWithFont(font: PDFont, text: String, maxWidth: Float, fontSize: Float): List<String> {
    if (text.isBlank()) return listOf("")

    val lines = mutableListOf<String>()
    val current = StringBuilder()

    text.split("\\s+".toRegex()).forEach { word ->
      val candidate = if (current.isEmpty()) word else current.toString() + " " + word
      val shapedCandidate = shapeTextForPdf(candidate)
      val width = font.getStringWidth(shapedCandidate) / 1000 * fontSize
      if (width <= maxWidth) {
        current.clear()
        current.append(candidate)
      } else {
        if (current.isNotEmpty()) {
          lines.add(current.toString())
          current.clear()
        }

        var remaining = word
        var safety = 0
        while (remaining.isNotEmpty() && safety < 1000) {
          val fitted = fitTextToWidth(font, fontSize, maxWidth, remaining)
          if (fitted.isBlank()) break
          lines.add(fitted)
          if (remaining.length <= fitted.length) {
            remaining = ""
          } else {
            remaining = remaining.substring(fitted.length).trimStart()
          }
          safety++
        }
        if (remaining.isNotEmpty()) {
          lines.add(remaining)
          remaining = ""
        }
      }
    }

    if (current.isNotEmpty()) {
      lines.add(current.toString())
    }

    return lines.ifEmpty { listOf("") }
  }

  private fun formatCurrency(amount: BigDecimal): String {
    return "${currencyFormatter.format(amount)} ₪"
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

    val visualRuns = arrayOfNulls<Any>(runCount)
    for (i in 0 until runCount) {
      visualRuns[i] = logicalRuns[i]
    }

    Bidi.reorderVisually(levels, 0, visualRuns, 0, runCount)

    val builder = StringBuilder(text.length)
    for (i in 0 until runCount) {
      val run = visualRuns[i] as? String
      if (run != null) builder.append(run)
    }

    return builder.toString()
  }

  private fun drawHeader(
    content: PDPageContentStream,
    theme: InvoiceTheme,
    pageWidth: Float,
    pageHeight: Float,
    height: Float,
    invoiceNumber: String,
    invoiceDate: String,
    businessName: String
  ) {
    // Header background with gradient effect (solid for now)
    content.setNonStrokingColor(theme.accentColor)
    content.addRect(0f, pageHeight - height, pageWidth, height)
    content.fill()

    val headerLeftX = 50f
    val headerTopY = pageHeight - 28f

    // Business name (prominent)
    content.writeText(
      text = businessName,
      x = headerLeftX,
      y = headerTopY,
      font = theme.boldFont,
      fontSize = 20f,
      color = Color.WHITE
    )

    // Invoice title
    content.writeText(
      text = "INVOICE",
      x = headerLeftX,
      y = headerTopY - 22f,
      font = theme.boldFont,
      fontSize = 14f,
      color = Color(240, 240, 255) // Light white/blue tint
    )

    // Invoice number
    content.writeText(
      text = "Invoice #: $invoiceNumber",
      x = headerLeftX,
      y = headerTopY - 38f,
      font = theme.regularFont,
      fontSize = 11f,
      color = Color.WHITE
    )

    // Invoice date
    content.writeText(
      text = "Date: $invoiceDate",
      x = headerLeftX,
      y = headerTopY - 52f,
      font = theme.regularFont,
      fontSize = 11f,
      color = Color.WHITE
    )

    // Payment badge (right side)
    val badgeWidth = 200f
    val badgeHeight = 36f
    val badgeX = pageWidth - headerLeftX - badgeWidth
    val badgeY = headerTopY - 18f

    // Badge background with rounded corners effect (white with border)
    content.setNonStrokingColor(Color.WHITE)
    content.addRect(badgeX, badgeY, badgeWidth, badgeHeight)
    content.fill()

    // Badge border
    content.setStrokingColor(theme.accentColorMuted)
    content.setLineWidth(1.5f)
    content.addRect(badgeX, badgeY, badgeWidth, badgeHeight)
    content.stroke()

    content.writeText(
      text = "PAYMENT DUE",
      x = badgeX + 16f,
      y = badgeY + 22f,
      font = theme.boldFont,
      fontSize = 10f,
      color = theme.textSecondary
    )

    content.writeText(
      text = "On Receipt",
      x = badgeX + 16f,
      y = badgeY + 10f,
      font = theme.boldFont,
      fontSize = 12f,
      color = theme.accentColor
    )
  }

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

  private fun PDPageContentStream.drawSellerPanel(
    theme: InvoiceTheme,
    businessName: String,
    lines: List<String>,
    topLeftX: Float,
    topY: Float,
    width: Float
  ): Float {
    val padding = 18f
    val lineHeight = 15f
    val titleHeight = 22f // Height for business name title
    val contentHeight = padding * 2 + titleHeight + lines.size * lineHeight
    val bottomY = topY - contentHeight

    // Panel background
    setNonStrokingColor(theme.panelBackground)
    addRect(topLeftX, bottomY, width, contentHeight)
    fill()

    // Business name as title (bold, larger)
    writeText(
      text = "FROM",
      x = topLeftX + padding,
      y = topY - padding,
      font = theme.boldFont,
      fontSize = 9f,
      color = theme.textSecondary
    )

    writeText(
      text = businessName,
      x = topLeftX + padding,
      y = topY - padding - 14f,
      font = theme.boldFont,
      fontSize = 14f,
      color = theme.textPrimary
    )

    var cursorY = topY - padding - titleHeight - 2f
    lines.forEach { line ->
      if (line.isNotBlank()) {
        // Check if line starts with "Agent:" or "Location:" to make it bold
        val isBold = line.startsWith("Agent:") || line.startsWith("Location:")
        val fontSize = if (isBold) 10f else 11f
        val font = if (isBold) theme.boldFont else theme.regularFont
        val color = if (isBold) theme.textPrimary else theme.textSecondary
        
        writeText(
          text = line,
          x = topLeftX + padding,
          y = cursorY,
          font = font,
          fontSize = fontSize,
          color = color
        )
        cursorY -= lineHeight
      } else {
        cursorY -= lineHeight * 0.5f // Smaller gap for blank lines
      }
    }

    // Border
    setStrokingColor(theme.borderColor)
    setLineWidth(1f)
    addRect(topLeftX, bottomY, width, contentHeight)
    stroke()

    return bottomY
  }

  private fun PDPageContentStream.drawInfoPanel(
    theme: InvoiceTheme,
    title: String,
    lines: List<String>,
    topLeftX: Float,
    topY: Float,
    width: Float
  ): Float {
    val padding = 18f
    val lineHeight = 15f
    val titleHeight = 18f
    val contentHeight = padding * 2 + titleHeight + lines.size * lineHeight
    val bottomY = topY - contentHeight

    setNonStrokingColor(theme.panelBackground)
    addRect(topLeftX, bottomY, width, contentHeight)
    fill()

    writeText(
      text = title.uppercase(Locale.ENGLISH),
      x = topLeftX + padding,
      y = topY - padding,
      font = theme.boldFont,
      fontSize = 11f,
      color = theme.textPrimary
    )

    var cursorY = topY - padding - titleHeight - 2f
    lines.forEach { line ->
      if (line.isNotBlank()) {
        writeText(
          text = line,
          x = topLeftX + padding,
          y = cursorY,
          font = theme.regularFont,
          fontSize = 11f,
          color = theme.textSecondary
        )
        cursorY -= lineHeight
      } else {
        cursorY -= lineHeight * 0.5f
      }
    }

    setStrokingColor(theme.borderColor)
    setLineWidth(1f)
    addRect(topLeftX, bottomY, width, contentHeight)
    stroke()

    return bottomY
  }

  private fun PDPageContentStream.drawItemsTable(
    theme: InvoiceTheme,
    topY: Float,
    margin: Float,
    columnWidths: FloatArray,
    products: List<com.pt.ordersystem.ordersystem.domains.product.models.ProductDataForOrder>
  ): Float {
    val headers = listOf("Qty", "Item", "Unit Price", "Line Total")
    val headerHeight = 26f
    val tableWidth = columnWidths.sum()

    setNonStrokingColor(theme.tableHeaderBackground)
    addRect(margin, topY - headerHeight, tableWidth, headerHeight)
    fill()

    var textX = margin + 12f
    headers.forEachIndexed { index, header ->
      writeText(
        text = header,
        x = textX,
        y = topY - headerHeight + 8f,
        font = theme.boldFont,
        fontSize = 11f,
        color = theme.textPrimary
      )
      textX += columnWidths[index]
    }

    setStrokingColor(theme.borderColor)
    addRect(margin, topY - headerHeight, tableWidth, headerHeight)
    stroke()

    var currentY = topY - headerHeight
    products.forEachIndexed { idx, product ->
      val rowValues = listOf(
        product.quantity.toString(),
        product.productName.replace("\n", " "),
        formatCurrency(product.pricePerUnit),
        formatCurrency(product.pricePerUnit.multiply(BigDecimal(product.quantity)))
      )

      val wrappedColumns = rowValues.mapIndexed { columnIndex, value ->
        val maxWidth = columnWidths[columnIndex] - 24f
        wrapWithFont(theme.regularFont, value, maxWidth, 11f)
      }

      val maxLines = wrappedColumns.maxOf { it.size.coerceAtLeast(1) }
      val dynamicRowHeight = maxLines * (11f + 3f) + 12f
      val rowBottom = currentY - dynamicRowHeight

      if (idx % 2 == 0) {
        setNonStrokingColor(theme.tableStripeBackground)
        addRect(margin, rowBottom, tableWidth, dynamicRowHeight)
        fill()
      }

      var cellX = margin + 12f
      wrappedColumns.forEachIndexed { columnIndex, lines ->
        lines.forEachIndexed { lineIdx, line ->
          val lineY = currentY - 10f - lineIdx * (11f + 3f)
          writeText(
            text = line,
            x = cellX,
            y = lineY,
            font = theme.regularFont,
            fontSize = 11f,
            color = theme.textPrimary
          )
        }
        cellX += columnWidths[columnIndex]
      }

      setStrokingColor(theme.borderColor)
      moveTo(margin, rowBottom)
      lineTo(margin + tableWidth, rowBottom)
      stroke()

      currentY = rowBottom
    }

    setStrokingColor(theme.borderColor)
    addRect(margin, currentY, tableWidth, topY - currentY)
    stroke()

    return currentY
  }

  private fun PDPageContentStream.drawSummaryCard(
    theme: InvoiceTheme,
    topY: Float,
    leftX: Float,
    width: Float,
    total: String
  ): Float {
    val padding = 18f
    val height = 78f
    val bottomY = topY - height

    setNonStrokingColor(theme.panelBackground)
    addRect(leftX, bottomY, width, height)
    fill()

    writeText(
      text = "Total Due".uppercase(Locale.ENGLISH),
      x = leftX + padding,
      y = topY - padding,
      font = theme.boldFont,
      fontSize = 11f,
      color = theme.textSecondary
    )

    writeText(
      text = total,
      x = leftX + padding,
      y = topY - padding - 24f,
      font = theme.boldFont,
      fontSize = 18f,
      color = theme.accentColor
    )

    writeText(
      text = "Please settle on receipt.",
      x = leftX + padding,
      y = bottomY + padding,
      font = theme.regularFont,
      fontSize = 10f,
      color = theme.textSecondary
    )

    setStrokingColor(theme.borderColor)
    addRect(leftX, bottomY, width, height)
    stroke()

    return bottomY
  }

  private fun PDPageContentStream.drawNotes(
    theme: InvoiceTheme,
    topY: Float,
    leftX: Float,
    width: Float,
    notes: List<String>
  ): Float {
    val padding = 16f
    val lineHeight = 14f
    val height = padding * 2 + (notes.size + 1) * lineHeight
    val bottomY = topY - height

    setNonStrokingColor(theme.panelBackground)
    addRect(leftX, bottomY, width, height)
    fill()

    writeText(
      text = "Notes".uppercase(Locale.ENGLISH),
      x = leftX + padding,
      y = topY - padding,
      font = theme.boldFont,
      fontSize = 11f,
      color = theme.textPrimary
    )

    var cursorY = topY - padding - lineHeight
    notes.forEach { note ->
      writeText(
        text = note,
        x = leftX + padding,
        y = cursorY,
        font = theme.regularFont,
        fontSize = 11f,
        color = theme.textSecondary
      )
      cursorY -= lineHeight
    }

    setStrokingColor(theme.borderColor)
    addRect(leftX, bottomY, width, height)
    stroke()

    return bottomY
  }
}

