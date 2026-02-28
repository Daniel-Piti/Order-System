package com.pt.ordersystem.ordersystem.domains.manager

import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ManagerDao : JpaRepository<ManagerDbEntity, String> {

    fun findByEmail(email: String): ManagerDbEntity?

    fun existsByEmail(email: String): Boolean
}
