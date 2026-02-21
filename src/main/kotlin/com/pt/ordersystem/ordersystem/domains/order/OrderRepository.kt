package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDateTime

interface OrderRepository : JpaRepository<OrderDbEntity, String> {
  fun findAllByManagerId(managerId: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndStatus(managerId: String, status: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndAgentId(managerId: String, agentId: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndAgentIdAndStatus(managerId: String, agentId: String, status: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndAgentIdIsNull(managerId: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndAgentIdIsNullAndStatus(managerId: String, status: String, pageable: Pageable): Page<OrderDbEntity>
  fun findByIdAndManagerId(id: String, managerId: String): OrderDbEntity?
  fun findByIdAndManagerIdAndAgentId(id: String, managerId: String, agentId: String): OrderDbEntity?

  fun findAllByManagerIdAndCustomerId(managerId: String, customerId: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndCustomerIdAndStatus(managerId: String, customerId: String, status: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndAgentIdAndCustomerId(managerId: String, agentId: String, customerId: String, pageable: Pageable): Page<OrderDbEntity>
  fun findAllByManagerIdAndAgentIdAndCustomerIdAndStatus(managerId: String, agentId: String, customerId: String, status: String, pageable: Pageable): Page<OrderDbEntity>

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

  // Count manager links created this month (agentId IS NULL)
  @Query(
    value = """
      SELECT COUNT(*) FROM orders
      WHERE manager_id = :managerId
        AND agent_id IS NULL
        AND YEAR(created_at) = YEAR(:startOfMonth)
        AND MONTH(created_at) = MONTH(:startOfMonth)
    """,
    nativeQuery = true
  )
  fun countManagerLinksCreatedThisMonth(
    @Param("managerId") managerId: String,
    @Param("startOfMonth") startOfMonth: LocalDateTime
  ): Long

  // Count agent links created this month (agentId IS NOT NULL)
  @Query(
    value = """
      SELECT COUNT(*) FROM orders
      WHERE manager_id = :managerId
        AND agent_id IS NOT NULL
        AND YEAR(created_at) = YEAR(:startOfMonth)
        AND MONTH(created_at) = MONTH(:startOfMonth)
    """,
    nativeQuery = true
  )
  fun countAgentLinksCreatedThisMonth(
    @Param("managerId") managerId: String,
    @Param("startOfMonth") startOfMonth: LocalDateTime
  ): Long

  // Count links per agent created this month
  @Query(
    value = """
      SELECT agent_id as agentId, COUNT(*) as count
      FROM orders
      WHERE manager_id = :managerId
        AND agent_id IS NOT NULL
        AND YEAR(created_at) = YEAR(:startOfMonth)
        AND MONTH(created_at) = MONTH(:startOfMonth)
      GROUP BY agent_id
    """,
    nativeQuery = true
  )
  fun countLinksPerAgentThisMonth(
    @Param("managerId") managerId: String,
    @Param("startOfMonth") startOfMonth: LocalDateTime
  ): List<Map<String, Any>>

  // Count completed orders done this month
  @Query(
    value = """
      SELECT COUNT(*) FROM orders
      WHERE manager_id = :managerId
        AND status = 'DONE'
        AND done_at IS NOT NULL
        AND YEAR(done_at) = YEAR(:startOfMonth)
        AND MONTH(done_at) = MONTH(:startOfMonth)
    """,
    nativeQuery = true
  )
  fun countCompletedOrdersThisMonth(
    @Param("managerId") managerId: String,
    @Param("startOfMonth") startOfMonth: LocalDateTime
  ): Long

  // Sum total price of orders done this month
  @Query(
    value = """
      SELECT COALESCE(SUM(total_price), 0) FROM orders
      WHERE manager_id = :managerId
        AND status = 'DONE'
        AND done_at IS NOT NULL
        AND YEAR(done_at) = YEAR(:startOfMonth)
        AND MONTH(done_at) = MONTH(:startOfMonth)
    """,
    nativeQuery = true
  )
  fun sumMonthlyIncome(
    @Param("managerId") managerId: String,
    @Param("startOfMonth") startOfMonth: LocalDateTime
  ): BigDecimal

  // Get monthly revenue and completed orders for a year
  @Query(
    value = """
      SELECT 
        MONTH(done_at) as month,
        COALESCE(SUM(total_price), 0) as revenue,
        COUNT(*) as completed_orders
      FROM orders
      WHERE manager_id = :managerId
        AND status = 'DONE'
        AND done_at IS NOT NULL
        AND YEAR(done_at) = :year
      GROUP BY MONTH(done_at)
      ORDER BY month
    """,
    nativeQuery = true
  )
  fun getYearlyRevenueAndOrders(
    @Param("managerId") managerId: String,
    @Param("year") year: Int
  ): List<Map<String, Any>>
}
