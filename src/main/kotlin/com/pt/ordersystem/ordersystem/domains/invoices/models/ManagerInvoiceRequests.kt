package com.pt.ordersystem.ordersystem.domains.invoices.models

data class ManagerCreateInvoiceRequest(
  val orderId: Long,
  val paymentMethod: PaymentMethod,
  val paymentProof: String,
  val allocationNumber: String? = null
)
