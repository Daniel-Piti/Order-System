package com.pt.ordersystem.ordersystem.auth

object AuthRole {
  const val AUTH_MANAGER = "hasRole('MANAGER')"
  const val AUTH_ADMIN = "hasRole('ADMIN')"
  const val AUTH_AGENT = "hasRole('AGENT')"
}

enum class Roles {
  MANAGER,
  ADMIN,
  AGENT,
}

data class AuthUser(
  val id: String,
  val email: String,
  val roles: List<String>
)
