package com.pt.ordersystem.ordersystem.auth

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.user.models.UserFailureReason
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder

object AuthUtils {

  private fun getPrincipal(): AuthUser =
    SecurityContextHolder.getContext().authentication?.principal as? AuthUser
      ?: throw IllegalStateException("Authentication principal missing or invalid")

  fun getCurrentUserId(): String = getPrincipal().userId

  fun getCurrentUserEmail(): String = getPrincipal().email

  fun getCurrentUserRoles(): List<String> = getPrincipal().roles

  fun checkOwnership(candidateUserId: String) {
    val currentUserId = getCurrentUserId()
    if (candidateUserId != currentUserId) {
      throw ServiceException(
        status = HttpStatus.FORBIDDEN,
        userMessage = "You are not authorized to access this resource",
        technicalMessage = "currentUserId=$currentUserId, candidateUserId=$candidateUserId",
        severity = SeverityLevel.WARN
      )
    }
  }
}
