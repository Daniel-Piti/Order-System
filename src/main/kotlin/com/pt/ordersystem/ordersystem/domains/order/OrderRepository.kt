package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface OrderRepository : JpaRepository<OrderDbEntity, String> {
  fun findAllByManagerId(managerId: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndStatus(managerId: String, status: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndAgentId(managerId: String, agentId: Long, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndAgentIdAndStatus(managerId: String, agentId: Long, status: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndAgentIdIsNull(managerId: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndAgentIdIsNullAndStatus(managerId: String, status: String, pageable: Pageable): Page<OrderDbEntity>
  fun findByIdAndManagerId(id: String, managerId: String): OrderDbEntity?
  fun findByIdAndManagerIdAndAgentId(id: String, managerId: String, agentId: Long): OrderDbEntity?

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
