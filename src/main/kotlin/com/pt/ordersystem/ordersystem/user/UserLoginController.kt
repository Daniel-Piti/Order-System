package com.pt.ordersystem.ordersystem.user

import com.pt.ordersystem.ordersystem.login.LoginService
import com.pt.ordersystem.ordersystem.user.models.UserLoginRequest
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Authentication", description = "Login and auth-related endpoints")
@RestController
@RequestMapping("/api/auth")
class UserLoginController(
  private val authService: LoginService
) {

  @PostMapping("/login")
  fun login(@RequestBody request: UserLoginRequest): ResponseEntity<Any> {
    return ResponseEntity.ok(authService.login(request))
  }

}