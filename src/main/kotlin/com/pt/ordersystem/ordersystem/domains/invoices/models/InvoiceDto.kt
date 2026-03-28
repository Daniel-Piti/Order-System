package com.pt.ordersystem.ordersystem.domains.invoices.models

import java.time.LocalDateTime

data class InvoiceDto(
  val id: Long,
  val managerId: String,
  val orderId: String,
  val invoiceSequenceNumber: Int,
  val paymentMethod: PaymentMethod,
  val paymentProof: String,
  val allocationNumber: String?,
  val s3Key: String?,
  val fileName: String?,
  val fileSizeBytes: Long?,
  val mimeType: String?,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime
)
