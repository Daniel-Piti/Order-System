package com.pt.ordersystem.ordersystem.domains.login

import com.pt.ordersystem.ordersystem.auth.JwtUtil
import com.pt.ordersystem.ordersystem.auth.Roles
import com.pt.ordersystem.ordersystem.config.ApplicationConfig
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.domains.login.models.AdminLoginRequest
import com.pt.ordersystem.ordersystem.domains.login.models.LoginResponse
import com.pt.ordersystem.ordersystem.domains.agent.AgentService
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import com.pt.ordersystem.ordersystem.domains.login.models.AgentLoginRequest
import com.pt.ordersystem.ordersystem.domains.login.models.ManagerLoginRequest
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class LoginService(
  private val passwordEncoder: BCryptPasswordEncoder,
  private val managerService: ManagerService,
  private val agentService: AgentService,
  private val jwtUtil: JwtUtil,
  private val config: ApplicationConfig,
) {

  fun loginManager(request: ManagerLoginRequest): LoginResponse {
    try {
      val manager = managerService.getManagerByEmail(request.email)

      if (!passwordEncoder.matches(request.password, manager.password)) {
        // Log the actual reason for debugging
        println("[AUTH] Invalid password attempt for email=${request.email}")
        throw ServiceException(
          status = HttpStatus.UNAUTHORIZED,
          userMessage = "Invalid email or password",
          technicalMessage = "Authentication failed",
          severity = SeverityLevel.INFO,
        )
      }

      val token = jwtUtil.generateToken(manager.email, manager.id, listOf(Roles.MANAGER))
      return LoginResponse(token)
    } catch (e: ServiceException) {
      // Log the actual reason for debugging
      if (e.status == HttpStatus.NOT_FOUND) {
        println("[AUTH] User not found attempt for email=${request.email}")
      }
      // Always return the same generic message for security
      throw ServiceException(
        status = HttpStatus.UNAUTHORIZED,
        userMessage = "Invalid email or password",
        technicalMessage = "Authentication failed",
        severity = SeverityLevel.INFO,
      )
    }
  }

  fun loginAgent(request: AgentLoginRequest): LoginResponse {
    try {
      val agent = agentService.getAgentByEmail(request.email)

      if (!passwordEncoder.matches(request.password, agent.password)) {
        println("[AUTH] Invalid agent password attempt for email=${request.email}")
        throw ServiceException(
          status = HttpStatus.UNAUTHORIZED,
          userMessage = "Invalid email or password",
          technicalMessage = "Agent authentication failed",
          severity = SeverityLevel.INFO,
        )
      }

      val token = jwtUtil.generateToken(agent.email, agent.id, listOf(Roles.AGENT))
      return LoginResponse(token)
    } catch (e: ServiceException) {
      if (e.status == HttpStatus.NOT_FOUND) {
        println("[AUTH] Agent not found attempt for email=${request.email}")
      }
      throw ServiceException(
        status = HttpStatus.UNAUTHORIZED,
        userMessage = "Invalid email or password",
        technicalMessage = "Agent authentication failed",
        severity = SeverityLevel.INFO,
      )
    }
  }

  fun loginAdmin(request: AdminLoginRequest): LoginResponse {
    val isUsernameValid = passwordEncoder.matches(request.adminUserName, config.adminUsernameHash)
    val isPasswordValid = passwordEncoder.matches(request.password, config.adminPasswordHash)

    if (!isUsernameValid || !isPasswordValid) {
      // Log the actual reason for debugging
      println("[AUTH] Admin login failed for username=${request.adminUserName}")
      throw ServiceException(
        status = HttpStatus.UNAUTHORIZED,
        userMessage = "Invalid admin credentials",
        technicalMessage = "Admin authentication failed",
        severity = SeverityLevel.INFO
      )
    }

    val token = if (request.userEmail != null) {
      val manager = try {
        managerService.getManagerByEmail(request.userEmail)
      } catch (e: ServiceException) {
        if (e.status == HttpStatus.NOT_FOUND) {
          throw ServiceException(
            status = HttpStatus.BAD_REQUEST,
            userMessage = "Manager not found for the provided email",
            technicalMessage = "Admin impersonation failed | managerEmail=${request.userEmail}",
            severity = SeverityLevel.INFO,
          )
        }
        throw e
      }

      jwtUtil.generateToken(manager.email, manager.id, listOf(Roles.ADMIN, Roles.MANAGER))
    } else {
      jwtUtil.generateToken("ADMIN", "ADMIN", listOf(Roles.ADMIN))
    }

    return LoginResponse(token)
  }

}