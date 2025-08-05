package com.pt.ordersystem.ordersystem.user

import com.pt.ordersystem.ordersystem.user.models.UserDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserDbEntity, String> {
  fun findByEmail(email: String): UserDbEntity?
}