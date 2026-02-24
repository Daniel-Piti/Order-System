package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerDao : JpaRepository<CustomerDbEntity, String> {
  fun findByManagerId(managerId: String): List<CustomerDbEntity>
  fun findByManagerIdAndId(managerId: String, id: String): CustomerDbEntity?
  fun findByManagerIdAndPhoneNumber(managerId: String, phoneNumber: String): CustomerDbEntity?
  fun findByManagerIdAndAgentId(managerId: String, agentId: String): List<CustomerDbEntity>
  fun findByManagerIdAndAgentIdAndId(managerId: String, agentId: String, id: String): CustomerDbEntity?
  fun countByAgentId(agentId: String): Long
  fun countByManagerIdAndAgentIdIsNull(managerId: String): Long
}
