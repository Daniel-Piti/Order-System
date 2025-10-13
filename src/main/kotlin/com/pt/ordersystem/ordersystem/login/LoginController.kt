package com.pt.ordersystem.ordersystem.login

import com.pt.ordersystem.ordersystem.login.models.LoginResponse
import com.pt.ordersystem.ordersystem.user.models.UserLoginRequest
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Authentication", description = "Login and auth-related endpoints")
@RestController
@RequestMapping("/api/auth/login")
class LoginController(
  private val authenticationService: AuthenticationService,
) {

  @PostMapping
  fun userLogin(@RequestBody request: UserLoginRequest): ResponseEntity<LoginResponse> =
    ResponseEntity.ok(authenticationService.authenticateUser(request))

  @PostMapping("/admin")
  fun adminLogin(@RequestBody request: AdminLoginRequest): ResponseEntity<LoginResponse> =
    ResponseEntity.ok(authenticationService.authenticateAdmin(request))

}

data class AdminLoginRequest(
  @field:Schema(example = "admin")
  val adminUserName: String,

  @field:Schema(example = "admin")
  val password: String,

  @field:Schema(example = "@gmail.com")
  val userEmail: String?,
)
