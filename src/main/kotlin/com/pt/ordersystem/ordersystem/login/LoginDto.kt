package com.pt.ordersystem.ordersystem.login

data class LoginRequest(
  val email: String,
  val password: String
)

data class JwtResponse(
  val token: String
)