package com.pt.ordersystem.ordersystem.domains.category.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "categories")
data class CategoryDbEntity(
    @Id
    val id: String,
    
    @Column(name = "user_id", nullable = false)
    val userId: String,
    
    @Column(name = "category", nullable = false)
    val category: String,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
)


