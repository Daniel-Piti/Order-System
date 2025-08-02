package com.pt.ordersystem.ordersystem.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {
  fun findByEmail(email: String): User?
}