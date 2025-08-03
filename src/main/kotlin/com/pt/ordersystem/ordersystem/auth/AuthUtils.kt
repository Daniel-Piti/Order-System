package com.pt.ordersystem.ordersystem.auth

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.user.UserFailureReason
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder

object AuthUtils {

  private fun getAuthentication() =
    SecurityContextHolder.getContext().authentication
      ?: throw IllegalStateException("No authentication present")

  private fun getDetailsMap(): Map<*, *> =
    getAuthentication().details as? Map<*, *>
      ?: throw IllegalStateException("Authentication details missing or invalid")

  fun getCurrentUserId(): String =
    getDetailsMap()["userId"] as? String
      ?: throw IllegalStateException("User ID missing in authentication details")

  fun getCurrentUserEmail(): String =
    getDetailsMap()["email"] as? String
      ?: throw IllegalStateException("Email missing in authentication details")

  fun getCurrentUserRole(): String =
    getDetailsMap()["role"] as? String
      ?: throw IllegalStateException("Role missing in authentication details")

  fun checkOwnership(candidateUserId: String) {
    val currentUserId = getCurrentUserId()
    if (candidateUserId != currentUserId) {
      throw ServiceException(
        status = HttpStatus.FORBIDDEN,
        userMessage = UserFailureReason.UNAUTHORIZED_ACCESS.userMessage,
        technicalMessage = "${UserFailureReason.UNAUTHORIZED_ACCESS.technical} currentUserId=$currentUserId, candidateUserId=$candidateUserId",
        severity = SeverityLevel.WARN
      )
    }
  }
}
