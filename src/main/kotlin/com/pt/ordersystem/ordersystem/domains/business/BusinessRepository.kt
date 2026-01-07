package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BusinessRepository : JpaRepository<BusinessDbEntity, String> {
  fun findByManagerId(managerId: String): BusinessDbEntity?
  fun existsByManagerId(managerId: String): Boolean
  fun findByManagerIdIn(managerIds: List<String>): List<BusinessDbEntity>
}
