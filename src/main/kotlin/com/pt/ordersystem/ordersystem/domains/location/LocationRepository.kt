package com.pt.ordersystem.ordersystem.domains.location

import com.pt.ordersystem.ordersystem.domains.location.models.LocationDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LocationRepository : JpaRepository<LocationDbEntity, Long> {
  fun findByManagerId(managerId: String): List<LocationDbEntity>
  fun countByManagerId(managerId: String): Int
  fun findByManagerIdAndId(managerId: String, id: Long): LocationDbEntity?
}