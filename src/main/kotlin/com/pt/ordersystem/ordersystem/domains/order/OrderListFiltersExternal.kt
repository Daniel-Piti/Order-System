package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.order.models.OrderSource

/**
 * Request-bound filters for order listing (query params).
 * Mirrors [OrderListFilters] but without managerId (comes from auth).
 */
data class OrderListFiltersExternal(
  val status: String? = null,
  val orderSource: OrderSource? = null,
  val agentId: String? = null,
  val customerId: String? = null,
) {
  fun toDomain(managerId: String): OrderListFilters {
    // Validate + normalize agentId usage
    val normalizedAgentId = when (orderSource) {
      OrderSource.AGENT -> requireNotNull(agentId) { "agentId is required when orderSource is AGENT" }
      else -> null
    }

    return OrderListFilters(
      managerId = managerId,
      orderSource = orderSource,
      agentId = normalizedAgentId,
      status = status,
      customerId = customerId,
    )
  }
}

