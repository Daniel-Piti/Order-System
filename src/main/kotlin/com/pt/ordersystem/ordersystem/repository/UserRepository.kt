package com.pt.ordersystem.ordersystem.repository

import com.pt.ordersystem.ordersystem.dbEntity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface UserRepository : JpaRepository<User, String> {
  fun findByEmail(email: String): User?
}