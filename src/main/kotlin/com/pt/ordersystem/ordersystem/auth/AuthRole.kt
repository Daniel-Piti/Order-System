package com.pt.ordersystem.ordersystem.auth

object AuthRole {
  const val AUTH_USER = "hasRole('USER')"
  const val AUTH_ADMIN = "hasRole('ADMIN')"
}

enum class Roles {
  USER,
  ADMIN,
}

data class AuthUser(
  val userId: String,
  val email: String,
  val roles: List<String>
)
