package com.pt.ordersystem.ordersystem.notification.email

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_ADMIN
import com.pt.ordersystem.ordersystem.notification.email.models.EmailNotificationResponse
import com.pt.ordersystem.ordersystem.notification.email.models.EmailOrderDoneNotificationRequest
import com.pt.ordersystem.ordersystem.notification.email.models.EmailOrderPlacedNotificationRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Email Notifications", description = "Email notification APIs")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/notifications/email")
@PreAuthorize(AUTH_ADMIN)
class EmailNotificationController(
  private val emailNotificationService: EmailNotificationService,
) {

  @PostMapping("/order-placed")
  fun sendOrderPlacedNotification(
    @Valid @RequestBody request: EmailOrderPlacedNotificationRequest,
  ): ResponseEntity<EmailNotificationResponse> {
    val response = emailNotificationService.sendOrderPlacedNotification(request)
    return ResponseEntity.ok(response)
  }

  @PostMapping("/order-done")
  fun sendOrderDoneNotification(
    @Valid @RequestBody request: EmailOrderDoneNotificationRequest,
  ): ResponseEntity<EmailNotificationResponse> {
    val response = emailNotificationService.sendOrderDoneNotification(request)
    return ResponseEntity.ok(response)
  }
}
