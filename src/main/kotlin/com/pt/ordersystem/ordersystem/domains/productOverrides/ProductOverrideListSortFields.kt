package com.pt.ordersystem.ordersystem.domains.productOverrides

object ProductOverrideListSortFields {
  val ALLOWED: Set<String> = setOf(
    "customer_id",
    "override_price",
    "product_id",
    "agent_id",
  )
}
