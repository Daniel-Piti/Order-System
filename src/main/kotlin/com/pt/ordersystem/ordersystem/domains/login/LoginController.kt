package com.pt.ordersystem.ordersystem.domains.login

import com.pt.ordersystem.ordersystem.domains.login.models.AgentLoginRequest
import com.pt.ordersystem.ordersystem.domains.login.models.AdminLoginRequest
import com.pt.ordersystem.ordersystem.domains.login.models.ManagerLoginRequest
import com.pt.ordersystem.ordersystem.domains.login.models.LoginResponse
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
  private val loginService: LoginService,
) {

  @PostMapping("/manager")
  fun managerLogin(@RequestBody request: ManagerLoginRequest): ResponseEntity<LoginResponse> =
    ResponseEntity.ok(loginService.loginManager(request))

  @PostMapping("/agent")
  fun agentLogin(@RequestBody request: AgentLoginRequest): ResponseEntity<LoginResponse> =
    ResponseEntity.ok(loginService.loginAgent(request))

  @PostMapping("/admin")
  fun adminLogin(@RequestBody request: AdminLoginRequest): ResponseEntity<LoginResponse> =
    ResponseEntity.ok(loginService.loginAdmin(request))

}
