package com.pt.ordersystem.ordersystem.product

import com.pt.ordersystem.ordersystem.product.models.ProductDbEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<ProductDbEntity, String> {
  fun findAllByUserId(userId: String): List<ProductDbEntity>
}