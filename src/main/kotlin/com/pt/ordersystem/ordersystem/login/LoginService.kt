package com.pt.ordersystem.ordersystem.login

import com.pt.ordersystem.ordersystem.auth.JwtUtil
import com.pt.ordersystem.ordersystem.auth.Roles
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.user.models.UserFailureReason
import com.pt.ordersystem.ordersystem.login.models.LoginResponse
import com.pt.ordersystem.ordersystem.user.UserRepository
import com.pt.ordersystem.ordersystem.user.models.UserLoginRequest
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class LoginService(
  private val userRepository: UserRepository,
  private val passwordEncoder: BCryptPasswordEncoder,
  private val jwtUtil: JwtUtil
) {

  fun login(request: UserLoginRequest): LoginResponse {
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

    val token = jwtUtil.generateToken(user.email, user.id, Roles.USER)

    return LoginResponse(token)
  }
}