package com.pt.ordersystem.ordersystem.product

import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<ProductDbEntity, String> {
  fun findAllByUserId(userId: String): List<ProductDbEntity>
}