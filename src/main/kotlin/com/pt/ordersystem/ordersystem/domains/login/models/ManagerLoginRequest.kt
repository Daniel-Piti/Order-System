package com.pt.ordersystem.ordersystem.domains.login.models

import io.swagger.v3.oas.annotations.media.Schema

data class ManagerLoginRequest(
  @field:Schema(example = "manager@email.com")
  val email: String,

  @field:Schema(example = "Aa123456!")
  val password: String,
)
