package com.pt.ordersystem.ordersystem.auth

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.user.UserFailureReason
import com.pt.ordersystem.ordersystem.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
  private val userRepository: UserRepository,
  private val passwordEncoder: BCryptPasswordEncoder,
  private val jwtUtil: JwtUtil
) {

  fun login(request: LoginRequest): JwtResponse {
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

    val token = jwtUtil.generateToken(user.email, Roles.USER)
    return JwtResponse(token)
  }
}