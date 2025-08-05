package com.pt.ordersystem.ordersystem.order

import com.pt.ordersystem.ordersystem.order.models.OrderDbEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<OrderDbEntity, String> {
  fun findAllByUserId(userId: String): List<OrderDbEntity>
}