package com.pt.ordersystem.ordersystem.domains.agent

import com.pt.ordersystem.ordersystem.domains.agent.models.AgentDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AgentDao : JpaRepository<AgentDbEntity, String> {
  fun findByManagerId(managerId: String): List<AgentDbEntity>
  fun findByManagerIdAndId(managerId: String, id: String): AgentDbEntity?
  fun findByEmail(email: String): AgentDbEntity?
  fun existsByEmail(email: String): Boolean
  fun countByManagerId(managerId: String): Long
}
