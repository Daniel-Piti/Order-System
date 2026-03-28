package com.pt.ordersystem.ordersystem.domains.invoices.models

import java.math.BigDecimal

data class InvoiceWithOrderTotal(
  val invoice: Invoice,
  val pdfUrl: String,
  val orderTotalPrice: BigDecimal,
)
