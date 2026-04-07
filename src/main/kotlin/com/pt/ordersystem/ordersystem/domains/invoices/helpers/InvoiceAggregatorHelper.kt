package com.pt.ordersystem.ordersystem.domains.invoices.helpers

import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFHyperlink
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDate

object InvoiceAggregatorHelper {

    fun buildInvoiceLinksXlsx(
        fromDate: LocalDate,
        toDate: LocalDate,
        entries: List<InvoiceLinkEntry>,
        totalAmount: BigDecimal
    ): ByteArray {
        XSSFWorkbook().use { wb ->
            val sheet = wb.createSheet("חשבוניות")
            setSheetRightToLeft(sheet)
            val helper: CreationHelper = wb.creationHelper

            val centerStyle = wb.createCellStyle().apply {
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            }

            // #8e7cc3
            val purple = XSSFColor(
                byteArrayOf(0x8e.toByte(), 0x7c.toByte(), 0xc3.toByte()),
                DefaultIndexedColorMap()
            )

            val purpleHeaderStyle = wb.createCellStyle().apply {
                cloneStyleFrom(centerStyle)
                setFillForegroundColor(purple)
                fillPattern = FillPatternType.SOLID_FOREGROUND
                val font = wb.createFont()
                font.bold = true
                font.color = IndexedColors.WHITE.index
                setFont(font)
            }

            val dataTextStyle = wb.createCellStyle().apply {
                cloneStyleFrom(centerStyle)
            }

            val linkStyle = wb.createCellStyle().apply {
                cloneStyleFrom(centerStyle)
                val font = wb.createFont()
                font.underline = XSSFFont.U_SINGLE
                font.color = IndexedColors.BLUE.index
                setFont(font)
            }

            var rowNum = 0
            sheet.createRow(rowNum++).apply {
                createCell(0).apply {
                    setCellValue("מתאריך")
                    cellStyle = purpleHeaderStyle
                }
                createCell(1).apply {
                    setCellValue(fromDate.toString())
                    cellStyle = purpleHeaderStyle
                }
                createCell(2).apply {
                    setCellValue("")
                    cellStyle = purpleHeaderStyle
                }
            }
            sheet.createRow(rowNum++).apply {
                createCell(0).apply {
                    setCellValue("עד תאריך")
                    cellStyle = purpleHeaderStyle
                }
                createCell(1).apply {
                    setCellValue(toDate.toString())
                    cellStyle = purpleHeaderStyle
                }
                createCell(2).apply {
                    setCellValue("")
                    cellStyle = purpleHeaderStyle
                }
            }
            rowNum++ // empty row (gap)
            val tableHeaderRow = sheet.createRow(rowNum++)
            listOf("מזהה הזמנה", "קישור לחשבונית", "סכום").forEachIndexed { i, label ->
                tableHeaderRow.createCell(i).apply {
                    setCellValue(label)
                    cellStyle = purpleHeaderStyle
                }
            }
            for (e in entries) {
                val row = sheet.createRow(rowNum++)
                row.createCell(0).apply {
                    setCellValue(e.orderId)
                    cellStyle = dataTextStyle
                }
                val linkCell = row.createCell(1) as XSSFCell
                linkCell.setCellValue(e.displayName)
                val hyperlink = helper.createHyperlink(HyperlinkType.URL) as XSSFHyperlink
                hyperlink.address = e.url
                linkCell.hyperlink = hyperlink
                linkCell.cellStyle = linkStyle
                row.createCell(2).apply {
                    setCellValue(e.totalAmount.toDouble())
                    cellStyle = dataTextStyle
                }
            }
            val totalRow = sheet.createRow(rowNum++)
            totalRow.createCell(0).apply {
                setCellValue("סה״כ")
                cellStyle = purpleHeaderStyle
            }
            totalRow.createCell(1).apply {
                setCellValue("")
                cellStyle = purpleHeaderStyle
            }
            totalRow.createCell(2).apply {
                setCellValue(totalAmount.toDouble())
                cellStyle = purpleHeaderStyle
            }
            for (i in 0..2) sheet.setColumnWidth(i, sheet.getColumnWidth(i).coerceAtLeast(4000))
            val out = ByteArrayOutputStream()
            wb.write(out)
            return out.toByteArray()
        }
    }

    fun setSheetRightToLeft(sheet: org.apache.poi.xssf.usermodel.XSSFSheet) {
        try {
            val ctWorksheet = sheet.ctWorksheet
            val sheetViews = ctWorksheet.sheetViews
            if (sheetViews != null && sheetViews.sizeOfSheetViewArray() > 0) {
                sheetViews.getSheetViewArray(0).rightToLeft = true
            }
        } catch (_: Exception) {
            // RTL is optional; file still valid without it
        }
    }

    data class InvoiceLinkEntry(
        val orderId: String,
        val url: String,
        val displayName: String,
        val totalAmount: BigDecimal
    )
}