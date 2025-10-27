package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<OrderDbEntity, String> {
  fun findAllByUserId(userId: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByUserIdAndStatus(userId: String, status: String, pageable: Pageable): Page<OrderDbEntity>
}