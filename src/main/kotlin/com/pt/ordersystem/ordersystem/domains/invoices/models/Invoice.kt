package com.pt.ordersystem.ordersystem.domains.invoices.models

import java.time.LocalDateTime

/**
 * Domain model for an invoice (persisted as [InvoiceDbEntity]).
 */
data class Invoice(
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
  val updatedAt: LocalDateTime,
)

fun InvoiceDbEntity.toModel(): Invoice = Invoice(
  id = id,
  managerId = managerId,
  orderId = orderId,
  invoiceSequenceNumber = invoiceSequenceNumber,
  paymentMethod = PaymentMethod.valueOf(paymentMethod),
  paymentProof = paymentProof,
  allocationNumber = allocationNumber,
  s3Key = s3Key,
  fileName = fileName,
  fileSizeBytes = fileSizeBytes,
  mimeType = mimeType,
  createdAt = createdAt,
  updatedAt = updatedAt,
)
