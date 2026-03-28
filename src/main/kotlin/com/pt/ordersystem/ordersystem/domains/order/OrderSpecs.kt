package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import com.pt.ordersystem.ordersystem.domains.order.models.OrderSource
import org.springframework.data.jpa.domain.Specification
import jakarta.persistence.criteria.Predicate

/**
 * Filters for order listing. One query for all cases.
 *
 * Scope rules:
 * 1. orderSource = null → all orders under manager (manager + agents): filter by manager_id only.
 * 2. orderSource = MANAGER → manager-only orders: manager_id + order_source = MANAGER (we do not filter by agent_id; manager can create orders for agent's customers).
 * 3. orderSource = AGENT → that agent's orders: manager_id + order_source = AGENT + agent_id.
 *
 * @param managerId required – scope to this manager
 * @param orderSource null = all, MANAGER = manager-only, AGENT = that agent's orders (requires agentId)
 * @param agentId required when orderSource = AGENT
 * @param status optional – filter by order status
 * @param customerId optional – filter by customer
 */
data class OrderListFilters(
    val managerId: String,
    val orderSource: OrderSource? = null,
    val agentId: String? = null,
    val status: String? = null,
    val customerId: String? = null,
) {
    init {
        require(orderSource != OrderSource.AGENT || agentId != null) {
            "agentId is required when orderSource is AGENT"
        }
    }

    fun toSpecification(): Specification<OrderDbEntity> = Specification { root, _, cb ->
        val predicates = mutableListOf<Predicate>()

        predicates.add(cb.equal(root.get<String>("managerId"), managerId))

        when (orderSource) {
            null -> { /* all orders – no order_source filter */ }
            OrderSource.MANAGER -> predicates.add(cb.equal(root.get<String>("orderSource"), OrderSource.MANAGER.name))
            OrderSource.AGENT -> {
                predicates.add(cb.equal(root.get<String>("orderSource"), OrderSource.AGENT.name))
                predicates.add(cb.equal(root.get<String?>("agentId"), agentId))
            }
            OrderSource.PUBLIC -> predicates.add(cb.equal(root.get<String>("orderSource"), OrderSource.PUBLIC.name))
        }

        status?.takeIf { it.isNotBlank() }?.let { s ->
            predicates.add(cb.equal(root.get<String>("status"), s))
        }

        customerId?.let { c ->
            predicates.add(cb.equal(root.get<String?>("customerId"), c))
        }

        cb.and(*predicates.toTypedArray())
    }
}
