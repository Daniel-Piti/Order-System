package com.pt.ordersystem.ordersystem.domains.invoices.helpers

import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.PDPageContentStream
import java.awt.Color
import java.text.Bidi

/**
 * Extension functions and helpers for PDPageContentStream to simplify PDF drawing operations.
 */
internal fun PDPageContentStream.drawRoundedRect(
  x: Float,
  y: Float,
  width: Float,
  height: Float,
  radius: Float,
  fill: Boolean = false,
  stroke: Boolean = true
) {
  val r = radius.coerceAtMost(minOf(width, height) / 2f)
  
  // Start from bottom-left corner (after radius)
  moveTo(x + r, y)
  // Bottom edge
  lineTo(x + width - r, y)
  // Bottom-right corner
  curveTo(x + width, y, x + width, y, x + width, y + r)
  // Right edge
  lineTo(x + width, y + height - r)
  // Top-right corner
  curveTo(x + width, y + height, x + width, y + height, x + width - r, y + height)
  // Top edge
  lineTo(x + r, y + height)
  // Top-left corner
  curveTo(x, y + height, x, y + height, x, y + height - r)
  // Left edge
  lineTo(x, y + r)
  // Bottom-left corner
  curveTo(x, y, x, y, x + r, y)
  closePath()
  
  if (fill) {
    fill()
  }
  if (stroke) {
    stroke()
  }
}

internal fun PDPageContentStream.writeText(
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

internal fun PDPageContentStream.writeTextRightAligned(
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

internal fun PDPageContentStream.writeTextCentered(
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

internal fun PDPageContentStream.writeWrappedTextRightAligned(
  text: String,
  rightX: Float,
  y: Float,
  maxWidth: Float,
  font: PDFont,
  fontSize: Float,
  color: Color,
  lineHeight: Float
): Float {
  val lines = wrapTextForPdf(text, maxWidth.coerceAtLeast(50f).coerceAtMost(rightX - 50f), font, fontSize)
  var currentY = y
  lines.forEach { line ->
    writeTextRightAligned(line, rightX, currentY, font, fontSize, color)
    currentY -= lineHeight
  }
  return currentY
}

internal fun PDPageContentStream.wrapTextForPdf(
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

// Helper functions for text operations
internal fun getTextWidth(text: String, font: PDFont, fontSize: Float): Float {
  return font.getStringWidth(shapeTextForPdf(text)) / 1000f * fontSize
}

internal fun shapeTextForPdf(text: String): String {
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

internal fun breakLongWord(word: String, maxWidth: Float, font: PDFont, fontSize: Float): List<String> {
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

