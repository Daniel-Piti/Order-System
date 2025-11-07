package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface OrderRepository : JpaRepository<OrderDbEntity, String> {
  fun findAllByUserId(userId: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByUserIdAndStatus(userId: String, status: String, pageable: Pageable): Page<OrderDbEntity>

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
    value = """
      UPDATE orders
      SET status = 'EXPIRED',
          updated_at = :updatedAt
      WHERE status = 'EMPTY'
        AND link_expires_at < :currentTime
    """,
    nativeQuery = true
  )
  fun bulkExpireEmptyOrders(
    currentTime: LocalDateTime,
    updatedAt: LocalDateTime,
  ): Int
}