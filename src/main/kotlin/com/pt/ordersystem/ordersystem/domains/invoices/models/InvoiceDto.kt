package com.pt.ordersystem.ordersystem.domains.invoices.models

import com.pt.ordersystem.ordersystem.storage.S3Helper
import java.math.BigDecimal
import java.time.LocalDateTime

data class InvoiceDto(
  val id: Long,
  val orderId: String,
  val customerId: String?,
  val totalAmount: BigDecimal,
  val invoiceType: InvoiceType,
  val linkedInvoiceId: Long?,
  val invoiceSequenceNumber: Int,
  val paymentMethod: PaymentMethod,
  val createdAt: LocalDateTime,
  val pdfUrl: String,
)

fun Invoice.toDto(): InvoiceDto = InvoiceDto(
  id = id,
  orderId = orderId,
  customerId = customerId,
  totalAmount = totalAmount,
  invoiceType = invoiceType,
  linkedInvoiceId = linkedInvoiceId,
  invoiceSequenceNumber = invoiceSequenceNumber,
  paymentMethod = paymentMethod,
  createdAt = createdAt,
  pdfUrl = S3Helper.getPublicUrl(s3Key) ?: "",
)
