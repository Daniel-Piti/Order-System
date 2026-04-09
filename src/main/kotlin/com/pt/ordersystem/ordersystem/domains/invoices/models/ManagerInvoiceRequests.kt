package com.pt.ordersystem.ordersystem.domains.invoices.models

import java.math.BigDecimal

data class ManagerCreateInvoiceRequest(
  val orderId: String,
  val paymentMethod: PaymentMethod,
  val paymentProof: String,
  val allocationNumber: String?
)

data class CreateCreditNoteByAmountRequest(
  val invoiceId: Long,
  val amount: BigDecimal,
  val allocationNumber: String?,
)
