package com.pt.ordersystem.ordersystem.login.models

import io.swagger.v3.oas.annotations.media.Schema

data class AdminLoginRequest(
  @field:Schema(example = "admin")
  val adminUserName: String,

  @field:Schema(example = "admin")
  val password: String,

  @field:Schema(example = "@gmail.com")
  val userEmail: String?,
)
