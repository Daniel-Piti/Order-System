package com.pt.ordersystem.ordersystem.login

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Authentication", description = "Login and auth-related endpoints")
@RestController
@RequestMapping("/auth")
class UserLoginController(
  private val authService: LoginService
) {

  @PostMapping("/login")
  fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
    return ResponseEntity.ok(authService.login(request))
  }

}