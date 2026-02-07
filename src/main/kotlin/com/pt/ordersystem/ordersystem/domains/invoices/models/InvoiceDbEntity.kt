package com.pt.ordersystem.ordersystem.domains.invoices.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "invoices")
data class InvoiceDbEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = 0,

  @Column(name = "manager_id", nullable = false, length = 255)
  val managerId: String,

  @Column(name = "order_id", nullable = false)
  val orderId: Long,

  @Column(name = "invoice_sequence_number", nullable = false)
  val invoiceSequenceNumber: Int,

  @Column(name = "payment_method", nullable = false, length = 50)
  val paymentMethod: String,

  @Column(name = "payment_proof", nullable = false, length = 512)
  val paymentProof: String,

  @Column(name = "allocation_number", length = 9)
  val allocationNumber: String? = null,

  @Column(name = "s3_key", length = 512)
  val s3Key: String? = null,

  @Column(name = "file_name", length = 255)
  val fileName: String? = null,

  @Column(name = "file_size_bytes")
  val fileSizeBytes: Long? = null,

  @Column(name = "mime_type", length = 100)
  val mimeType: String? = null,

  @Column(name = "created_at", nullable = false, updatable = false)
  val createdAt: LocalDateTime,

  @Column(name = "updated_at", nullable = false)
  var updatedAt: LocalDateTime
)

