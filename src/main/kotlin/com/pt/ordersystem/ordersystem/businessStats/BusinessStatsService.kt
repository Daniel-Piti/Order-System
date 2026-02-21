package com.pt.ordersystem.ordersystem.businessStats

import com.pt.ordersystem.ordersystem.domains.agent.AgentService
import com.pt.ordersystem.ordersystem.businessStats.models.AgentLinkInfo
import com.pt.ordersystem.ordersystem.businessStats.models.BusinessStatsDto
import com.pt.ordersystem.ordersystem.businessStats.models.LinksCreatedStats
import com.pt.ordersystem.ordersystem.businessStats.models.MonthlyData
import com.pt.ordersystem.ordersystem.domains.order.OrderRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.Month

@Service
class BusinessStatsService(
  private val orderRepository: OrderRepository,
  private val agentService: AgentService,
) {

  fun getBusinessStats(managerId: String, year: Int? = null, month: Int? = null): BusinessStatsDto {
    val linksCreated = getLinksCreatedStats(managerId, year, month)
    val monthlyIncome = getMonthlyIncome(managerId, year, month)
    val yearlyData = getYearlyData(managerId, year)

    return BusinessStatsDto(
      linksCreatedThisMonth = linksCreated,
      monthlyIncome = monthlyIncome,
      yearlyData = yearlyData
    )
  }

  fun getLinksCreatedStats(managerId: String, year: Int? = null, month: Int? = null): LinksCreatedStats {
    val targetDate = if (year != null && month != null) {
      LocalDateTime.of(year, month, 1, 0, 0, 0)
    } else {
      LocalDateTime.now()
    }
    val startOfMonth = targetDate.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)

    val managerLinks = orderRepository.countManagerLinksCreatedThisMonth(managerId, startOfMonth).toInt()
    val agentLinks = orderRepository.countAgentLinksCreatedThisMonth(managerId, startOfMonth).toInt()
    val totalLinks = managerLinks + agentLinks

    val linksPerAgentData = orderRepository.countLinksPerAgentThisMonth(managerId, startOfMonth)
    val allAgents = agentService.getAgentsForManager(managerId)
    
    val linkCountsMap = linksPerAgentData.associate { row ->
      val agentId = row["agentId"] as? String ?: ""
      val linkCount = (row["count"] as? Number)?.toInt() ?: 0
      agentId to linkCount
    }
    
    val linksPerAgent = allAgents.associate { agent ->
      val agentId = agent.id
      val linkCount = linkCountsMap[agentId] ?: 0
      agentId to AgentLinkInfo(
        agentId = agentId,
        agentName = "${agent.firstName} ${agent.lastName}",
        linkCount = linkCount
      )
    }

    return LinksCreatedStats(
      managerLinks = managerLinks,
      agentLinks = agentLinks,
      total = totalLinks,
      linksPerAgent = linksPerAgent
    )
  }

  fun getMonthlyIncome(managerId: String, year: Int? = null, month: Int? = null): BigDecimal {
    val targetDate = if (year != null && month != null) {
      LocalDateTime.of(year, month, 1, 0, 0, 0)
    } else {
      LocalDateTime.now()
    }
    val startOfMonth = targetDate.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
    return orderRepository.sumMonthlyIncome(managerId, startOfMonth)
  }

  fun getCompletedOrdersCount(managerId: String, year: Int? = null, month: Int? = null): Int {
    val targetDate = if (year != null && month != null) {
      LocalDateTime.of(year, month, 1, 0, 0, 0)
    } else {
      LocalDateTime.now()
    }
    val startOfMonth = targetDate.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
    return orderRepository.countCompletedOrdersThisMonth(managerId, startOfMonth).toInt()
  }

  fun getYearlyData(managerId: String, year: Int? = null): List<MonthlyData> {
    val targetYear = year ?: LocalDateTime.now().year
    val yearlyDataRaw = orderRepository.getYearlyRevenueAndOrders(managerId, targetYear)
    val yearlyDataMap = yearlyDataRaw.associate { row ->
      val month = (row["month"] as? Number)?.toInt() ?: 0
      val revenue = (row["revenue"] as? BigDecimal) ?: BigDecimal.ZERO
      val completedOrders = (row["completed_orders"] as? Number)?.toInt() ?: 0
      month to Pair(revenue, completedOrders)
    }

    return (1..12).map { monthNum ->
      val (revenue, completedOrders) = yearlyDataMap[monthNum] ?: Pair(BigDecimal.ZERO, 0)
      val monthName = Month.of(monthNum).name.lowercase().replaceFirstChar { it.uppercase() }
      MonthlyData(
        month = monthNum,
        monthName = monthName,
        revenue = revenue,
        completedOrders = completedOrders
      )
    }
  }

}
