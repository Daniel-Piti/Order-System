package com.pt.ordersystem.ordersystem.businessStats

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.businessStats.models.BusinessStatsDto
import com.pt.ordersystem.ordersystem.businessStats.models.LinksCreatedStats
import com.pt.ordersystem.ordersystem.businessStats.models.MonthlyData
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@Tag(name = "Business Stats", description = "Business statistics API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/business-stats")
@PreAuthorize(AUTH_MANAGER)
class BusinessStatsController(
  private val businessStatsService: BusinessStatsService
) {

  @GetMapping
  fun getBusinessStats(
    @AuthenticationPrincipal manager: AuthUser,
    @RequestParam(required = false) year: Int?,
    @RequestParam(required = false) month: Int?
  ): ResponseEntity<BusinessStatsDto> {
    val stats = businessStatsService.getBusinessStats(manager.id, year, month)
    return ResponseEntity.ok(stats)
  }

  @GetMapping("/links-created")
  fun getLinksCreatedStats(
    @AuthenticationPrincipal manager: AuthUser,
    @RequestParam(required = false) year: Int?,
    @RequestParam(required = false) month: Int?
  ): ResponseEntity<LinksCreatedStats> {
    val stats = businessStatsService.getLinksCreatedStats(manager.id, year, month)
    return ResponseEntity.ok(stats)
  }

  @GetMapping("/orders-by-status")
  fun getOrdersByStatus(
    @AuthenticationPrincipal manager: AuthUser,
    @RequestParam(required = false) year: Int?,
    @RequestParam(required = false) month: Int?
  ): ResponseEntity<Map<String, Int>> {
    val stats = businessStatsService.getOrdersByStatus(manager.id, year, month)
    return ResponseEntity.ok(stats)
  }

  @GetMapping("/monthly-income")
  fun getMonthlyIncome(
    @AuthenticationPrincipal manager: AuthUser,
    @RequestParam(required = false) year: Int?,
    @RequestParam(required = false) month: Int?
  ): ResponseEntity<BigDecimal> {
    val income = businessStatsService.getMonthlyIncome(manager.id, year, month)
    return ResponseEntity.ok(income)
  }

  @GetMapping("/yearly-data")
  fun getYearlyData(
    @AuthenticationPrincipal manager: AuthUser,
    @RequestParam(required = false) year: Int?
  ): ResponseEntity<List<MonthlyData>> {
    val data = businessStatsService.getYearlyData(manager.id, year)
    return ResponseEntity.ok(data)
  }

}
