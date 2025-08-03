package com.pt.ordersystem.ordersystem.login

import com.pt.ordersystem.ordersystem.auth.JwtUtil
import com.pt.ordersystem.ordersystem.auth.Roles
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Admin authentication")
@RequestMapping("/auth/admin")
class AdminLoginController(
  private val passwordEncoder: BCryptPasswordEncoder,
  private val jwtUtil: JwtUtil
) {

  @PostMapping("/login")
  fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
    val adminEmail = "admin"
    val hashedPassword = "\$2a$10\$jbeZ..DYskTPOO6U0huCfui4qtHfo76EqRiQp2GJ0MBiesWdOObRy"
    if(request.email != adminEmail || !passwordEncoder.matches(request.password, hashedPassword)){
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials")
    }

    val token = jwtUtil.generateToken(adminEmail, "ADMIN", Roles.ADMIN)
    return ResponseEntity.ok(JwtResponse(token))
  }
}