package com.pt.ordersystem.ordersystem.domains.invoices.models

data class CreateInvoiceRequest(
  val managerId: String,
  val orderId: String,
  val paymentMethod: PaymentMethod,
  val paymentProof: String,
  val allocationNumber: String? = null
)

data class CreateInvoiceResponse(
  val invoiceId: Long,
  val invoiceName: String,
  val pdfUrl: String
)
