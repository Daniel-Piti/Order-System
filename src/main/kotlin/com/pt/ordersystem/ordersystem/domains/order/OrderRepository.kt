package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.order.models.Order
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import com.pt.ordersystem.ordersystem.domains.order.models.OrderFailureReason
import com.pt.ordersystem.ordersystem.domains.order.models.OrderSource
import com.pt.ordersystem.ordersystem.domains.order.models.toModel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
class OrderRepository(
  private val orderDao: OrderDao,
) {

  fun searchOrders(
    managerId: String,
    orderSource: OrderSource?,
    agentId: String?,
    status: String?,
    customerId: String?,
    pageable: Pageable,
  ): Page<Order> =
    orderDao.searchOrdersForManager(
      managerId = managerId,
      orderSource = orderSource?.name,
      agentId = agentId,
      status = status?.takeIf { it.isNotBlank() },
      customerId = customerId,
      pageable = pageable,
    ).map { it.toModel() }

  fun findByIdAndManagerIdAndAgentId(orderId: String, managerId: String, agentId: String?): Order =
    orderDao.findByIdAndManagerIdAndAgentId(orderId, managerId, agentId)?.toModel() ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = OrderFailureReason.NOT_FOUND.userMessage,
      technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId, managerId=$managerId, agentId=$agentId",
      severity = SeverityLevel.WARN
    )

  fun findById(orderId: String): Order =
    orderDao.findById(orderId).orElse(null)?.toModel() ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = OrderFailureReason.NOT_FOUND.userMessage,
      technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
      severity = SeverityLevel.WARN
    )

  fun save(entity: OrderDbEntity): Order = orderDao.save(entity).toModel()

  fun bulkExpireEmptyOrders(currentTime: LocalDateTime, updatedAt: LocalDateTime): Int =
    orderDao.bulkExpireEmptyOrders(currentTime, updatedAt)

  fun countManagerLinksCreatedThisMonth(managerId: String, startOfMonth: LocalDateTime): Long =
    orderDao.countManagerLinksCreatedThisMonth(managerId, startOfMonth)

  fun countAgentLinksCreatedThisMonth(managerId: String, startOfMonth: LocalDateTime): Long =
    orderDao.countAgentLinksCreatedThisMonth(managerId, startOfMonth)

  fun countLinksPerAgentThisMonth(managerId: String, startOfMonth: LocalDateTime): List<Map<String, Any>> =
    orderDao.countLinksPerAgentThisMonth(managerId, startOfMonth)

  fun countCompletedOrdersThisMonth(managerId: String, startOfMonth: LocalDateTime): Long =
    orderDao.countCompletedOrdersThisMonth(managerId, startOfMonth)

  fun sumMonthlyIncome(managerId: String, startOfMonth: LocalDateTime): BigDecimal =
    orderDao.sumMonthlyIncome(managerId, startOfMonth)

  fun getYearlyRevenueAndOrders(managerId: String, year: Int): List<Map<String, Any>> =
    orderDao.getYearlyRevenueAndOrders(managerId, year)
}
