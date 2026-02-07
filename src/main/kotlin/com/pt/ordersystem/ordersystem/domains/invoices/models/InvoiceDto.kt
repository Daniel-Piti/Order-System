package com.pt.ordersystem.ordersystem.domains.invoices.models

import java.time.LocalDateTime

data class InvoiceDto(
  val id: Long,
  val managerId: String,
  val orderId: Long,
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

fun InvoiceDbEntity.toDto(): InvoiceDto {
  return InvoiceDto(
    id = this.id,
    managerId = this.managerId,
    orderId = this.orderId,
    invoiceSequenceNumber = this.invoiceSequenceNumber,
    paymentMethod = PaymentMethod.valueOf(this.paymentMethod),
    paymentProof = this.paymentProof,
    allocationNumber = this.allocationNumber,
    s3Key = this.s3Key,
    fileName = this.fileName,
    fileSizeBytes = this.fileSizeBytes,
    mimeType = this.mimeType,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
  )
}
