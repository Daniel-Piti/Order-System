package com.pt.ordersystem.ordersystem.login

import com.pt.ordersystem.ordersystem.auth.JwtUtil
import com.pt.ordersystem.ordersystem.auth.Roles
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.login.models.AdminLoginRequest
import com.pt.ordersystem.ordersystem.user.models.UserFailureReason
import com.pt.ordersystem.ordersystem.login.models.LoginResponse
import com.pt.ordersystem.ordersystem.user.UserRepository
import com.pt.ordersystem.ordersystem.user.UserService
import com.pt.ordersystem.ordersystem.user.models.UserLoginRequest
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class LoginService(
  private val userRepository: UserRepository,
  private val passwordEncoder: BCryptPasswordEncoder,
  private val userService: UserService,
  private val jwtUtil: JwtUtil,
) {

  companion object {
    // We can move these to application config or env variables
    private const val ADMIN_USERNAME_HASH = "\$2a\$10\$iKrrfrYNuyfyDTCDIdOgquXgIoT6nK8TJ40Gltux/5p14sS2Im3Li" // 'admin'
    private const val ADMIN_PASSWORD_HASH = "\$2a\$10\$iKrrfrYNuyfyDTCDIdOgquXgIoT6nK8TJ40Gltux/5p14sS2Im3Li" // 'admin'
  }

  fun loginUser(request: UserLoginRequest): LoginResponse {
    val user = userRepository.findByEmail(request.email) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = UserFailureReason.NOT_FOUND.userMessage,
      technicalMessage = UserFailureReason.NOT_FOUND.technical + "email=${request.email}",
      severity = SeverityLevel.INFO,
    )

    if (!passwordEncoder.matches(request.password, user.password))
      throw ServiceException(
        status = HttpStatus.UNAUTHORIZED,
        userMessage = UserFailureReason.INVALID_PASSWORD.userMessage,
        technicalMessage = UserFailureReason.INVALID_PASSWORD.technical + "email=${request.email}",
        severity = SeverityLevel.INFO,
      )

    val token = jwtUtil.generateToken(user.email, user.id, listOf(Roles.USER))

    return LoginResponse(token)
  }

  fun loginAdmin(request: AdminLoginRequest): LoginResponse {
    val isUsernameValid = passwordEncoder.matches(request.adminUserName, ADMIN_USERNAME_HASH)
    val isPasswordValid = passwordEncoder.matches(request.password, ADMIN_PASSWORD_HASH)

    if (!isUsernameValid || !isPasswordValid) {
      throw ServiceException(
        status = HttpStatus.UNAUTHORIZED,
        userMessage = "Invalid credentials",
        technicalMessage = "Admin login failed for username=${request.adminUserName}",
        severity = SeverityLevel.INFO
      )
    }

    val user = request.userEmail?.let { email -> userService.getUserByEmail(email) }

    val token = if (user == null) {
      jwtUtil.generateToken("ADMIN", "ADMIN", listOf(Roles.ADMIN))
    } else {
      jwtUtil.generateToken(user.email, user.id, listOf(Roles.ADMIN, Roles.USER))
    }

    return LoginResponse(token)
  }

}