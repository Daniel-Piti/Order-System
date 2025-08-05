package com.pt.ordersystem.ordersystem.location

import com.pt.ordersystem.ordersystem.location.models.LocationDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LocationRepository : JpaRepository<LocationDbEntity, String> {
  fun findByUserId(userId: String): List<LocationDbEntity>
  fun countByUserId(userId: String): Int
}