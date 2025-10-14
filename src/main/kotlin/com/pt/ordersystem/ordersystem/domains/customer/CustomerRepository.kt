package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : JpaRepository<CustomerDbEntity, String> {
  fun findByUserId(userId: String): List<CustomerDbEntity>
  fun findByUserIdAndId(userId: String, id: String): CustomerDbEntity?
  fun findByUserIdAndPhoneNumber(userId: String, phoneNumber: String): CustomerDbEntity?
}

