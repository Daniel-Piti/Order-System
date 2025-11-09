package com.pt.ordersystem.ordersystem.domains.brand.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "brands")
data class BrandDbEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "manager_id", nullable = false)
    val managerId: String,
    
    @Column(name = "name", nullable = false)
    val name: String,
    
    @Column(name = "s3_key", length = 512)
    val s3Key: String?,
    
    @Column(name = "file_name")
    val fileName: String?,
    
    @Column(name = "file_size_bytes")
    val fileSizeBytes: Long?,
    
    @Column(name = "mime_type", length = 100)
    val mimeType: String?,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
)
