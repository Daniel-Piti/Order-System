package com.pt.ordersystem.ordersystem.domains.productImage.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "product_images")
data class ProductImageDbEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = 0,

  @Column(name = "product_id", nullable = false)
  val productId: String,

  @Column(name = "manager_id", nullable = false)
  val managerId: String,

  @Column(name = "s3_key", nullable = false, length = 512)
  val s3Key: String,

  @Column(name = "file_name", nullable = false)
  val fileName: String,

  @Column(name = "file_size_bytes", nullable = false)
  val fileSizeBytes: Long,

  @Column(name = "mime_type", nullable = false, length = 100)
  val mimeType: String,

  @Column(name = "created_at", nullable = false, updatable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  var updatedAt: LocalDateTime = LocalDateTime.now()
)

