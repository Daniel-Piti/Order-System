package com.pt.ordersystem.ordersystem.businessStats.models

import java.math.BigDecimal

data class BusinessStatsDto(
  val linksCreatedThisMonth: LinksCreatedStats,
  val ordersByStatus: Map<String, Int>,
  val monthlyIncome: BigDecimal,
  val yearlyData: List<MonthlyData>
)

data class MonthlyData(
  val month: Int,
  val monthName: String,
  val revenue: BigDecimal,
  val completedOrders: Int
)

data class LinksCreatedStats(
  val managerLinks: Int,
  val agentLinks: Int,
  val total: Int,
  val linksPerAgent: Map<Long, AgentLinkInfo>
)

data class AgentLinkInfo(
  val agentId: Long,
  val agentName: String,
  val linkCount: Int
)
