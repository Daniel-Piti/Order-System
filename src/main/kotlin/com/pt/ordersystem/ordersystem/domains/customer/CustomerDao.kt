package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CustomerDao : JpaRepository<CustomerDbEntity, String> {
  fun findByAgentId(agentId: String): List<CustomerDbEntity>
  fun findByManagerId(managerId: String): List<CustomerDbEntity>
  fun findByManagerIdAndId(managerId: String, id: String): CustomerDbEntity?
  fun findByManagerIdAndAgentIdAndId(managerId: String, agentId: String?, id: String): CustomerDbEntity?
  fun countByManagerIdAndAgentId(managerId: String, agentId: String?): Long

  @Modifying
  @Query("DELETE FROM CustomerDbEntity e WHERE e.managerId = :managerId AND e.agentId = :agentId")
  fun deleteByManagerIdAndAgentId(managerId: String, agentId: String)
}
