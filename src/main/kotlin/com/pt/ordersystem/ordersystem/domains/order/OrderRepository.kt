package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.order.models.Order
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import com.pt.ordersystem.ordersystem.domains.order.models.OrderFailureReason
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

  fun findAllByManagerId(managerId: String, pageable: Pageable): Page<Order> =
    orderDao.findAllByManagerId(managerId, pageable).map { it.toModel() }

  fun findAllByManagerIdAndStatus(managerId: String, status: String, pageable: Pageable): Page<Order> =
    orderDao.findAllByManagerIdAndStatus(managerId, status, pageable).map { it.toModel() }

  fun findAllByManagerIdAndAgentId(managerId: String, agentId: String, pageable: Pageable): Page<Order> =
    orderDao.findAllByManagerIdAndAgentId(managerId, agentId, pageable).map { it.toModel() }

  fun findAllByManagerIdAndAgentIdAndStatus(
    managerId: String,
    agentId: String,
    status: String,
    pageable: Pageable,
  ): Page<Order> =
    orderDao.findAllByManagerIdAndAgentIdAndStatus(managerId, agentId, status, pageable).map { it.toModel() }

  fun findAllByManagerIdAndAgentIdIsNull(managerId: String, pageable: Pageable): Page<Order> =
    orderDao.findAllByManagerIdAndAgentIdIsNull(managerId, pageable).map { it.toModel() }

  fun findAllByManagerIdAndAgentIdIsNullAndStatus(managerId: String, status: String, pageable: Pageable): Page<Order> =
    orderDao.findAllByManagerIdAndAgentIdIsNullAndStatus(managerId, status, pageable).map { it.toModel() }

  fun findByIdAndManagerIdAndAgentId(orderId: String, managerId: String, agentId: String?): Order =
    orderDao.findByIdAndManagerIdAndAgentId(orderId, managerId, agentId)?.toModel() ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = OrderFailureReason.NOT_FOUND.userMessage,
      technicalMessage = OrderFailureReason.NOT_FOUND.technical +
              "orderId=$orderId, managerId=$managerId${if (agentId != null) ", agentId=$agentId" else ""}",
      severity = SeverityLevel.WARN
    )

  fun findAllByManagerIdAndCustomerId(managerId: String, customerId: String, pageable: Pageable): Page<Order> =
    orderDao.findAllByManagerIdAndCustomerId(managerId, customerId, pageable).map { it.toModel() }

  fun findAllByManagerIdAndCustomerIdAndStatus(
    managerId: String,
    customerId: String,
    status: String,
    pageable: Pageable,
  ): Page<Order> =
    orderDao.findAllByManagerIdAndCustomerIdAndStatus(managerId, customerId, status, pageable).map { it.toModel() }

  fun findAllByManagerIdAndAgentIdAndCustomerId(
    managerId: String,
    agentId: String,
    customerId: String,
    pageable: Pageable,
  ): Page<Order> =
    orderDao.findAllByManagerIdAndAgentIdAndCustomerId(managerId, agentId, customerId, pageable).map { it.toModel() }

  fun findAllByManagerIdAndAgentIdAndCustomerIdAndStatus(
    managerId: String,
    agentId: String,
    customerId: String,
    status: String,
    pageable: Pageable,
  ): Page<Order> =
    orderDao.findAllByManagerIdAndAgentIdAndCustomerIdAndStatus(
      managerId,
      agentId,
      customerId,
      status,
      pageable,
    ).map { it.toModel() }

  fun findById(orderId: String): Order =
    orderDao.findById(orderId).orElse(null)?.toModel() ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = OrderFailureReason.NOT_FOUND.userMessage,
      technicalMessage = OrderFailureReason.NOT_FOUND.technical + "orderId=$orderId",
      severity = SeverityLevel.WARN
    )

  fun findByIdIn(orderIds: List<String>): List<Order> =
    orderDao.findAllById(orderIds).map { it.toModel() }

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
