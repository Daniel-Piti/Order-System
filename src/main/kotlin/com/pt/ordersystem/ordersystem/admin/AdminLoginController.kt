package com.pt.ordersystem.ordersystem.admin

import com.pt.ordersystem.ordersystem.admin.models.AdminLoginRequest
import com.pt.ordersystem.ordersystem.auth.JwtUtil
import com.pt.ordersystem.ordersystem.auth.Roles
import com.pt.ordersystem.ordersystem.user.UserService
import com.pt.ordersystem.ordersystem.login.models.LoginResponse
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
  private val jwtUtil: JwtUtil,
  private val userService: UserService,
) {

  companion object {
    // You can move these to application config or env variables
    private const val ADMIN_USERNAME_HASH = "\$2a\$10\$iKrrfrYNuyfyDTCDIdOgquXgIoT6nK8TJ40Gltux/5p14sS2Im3Li"
    private const val ADMIN_PASSWORD_HASH = "\$2a\$10\$iKrrfrYNuyfyDTCDIdOgquXgIoT6nK8TJ40Gltux/5p14sS2Im3Li"
  }

  @PostMapping("/login")
  fun login(@RequestBody request: AdminLoginRequest): ResponseEntity<Any> {

    val isUsernameValid = passwordEncoder.matches(request.adminUserName, ADMIN_USERNAME_HASH)
    val isPasswordValid = passwordEncoder.matches(request.password, ADMIN_PASSWORD_HASH)

    if (!isUsernameValid || !isPasswordValid) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body("Invalid credentials")
    }

    val user = request.userEmail.takeIf { it.isNotBlank() }?.let {
      userService.getUserByEmail(it)
    }

    val userId = user?.id ?: "ADMIN"
    val userEmail = user?.email ?: "ADMIN"

    val token = jwtUtil.generateToken(userId, userEmail, Roles.ADMIN)
    return ResponseEntity.ok(LoginResponse(token))
  }

}
