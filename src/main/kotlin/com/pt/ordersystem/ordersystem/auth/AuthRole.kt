package com.pt.ordersystem.ordersystem.auth

object AuthRole {
  const val AUTH_USER = "hasRole('USER')"
  const val AUTH_ADMIN = "hasRole('ADMIN')"
  const val AUTH_AGENT = "hasRole('AGENT')"
}

enum class Roles {
  USER,
  ADMIN,
  AGENT,
}

data class AuthUser(
  val id: String,
  val email: String,
  val roles: List<String>
)
