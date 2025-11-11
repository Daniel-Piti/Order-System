package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : JpaRepository<CustomerDbEntity, String> {
  fun findByManagerId(managerId: String): List<CustomerDbEntity>
  fun findByManagerIdAndId(managerId: String, id: String): CustomerDbEntity?
  fun findByManagerIdAndPhoneNumber(managerId: String, phoneNumber: String): CustomerDbEntity?
  fun findByManagerIdAndAgentIdIsNullAndId(managerId: String, id: String): CustomerDbEntity?
  fun findByManagerIdAndAgentId(managerId: String, agentId: Long): List<CustomerDbEntity>
  fun findByManagerIdAndAgentIdAndId(managerId: String, agentId: Long, id: String): CustomerDbEntity?
  fun countByAgentId(agentId: Long): Long
  fun countByManagerIdAndAgentIdIsNull(managerId: String): Long
}

