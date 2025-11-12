package com.pt.ordersystem.ordersystem.domains.manager

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerDto
import com.pt.ordersystem.ordersystem.domains.manager.models.UpdateManagerDetailsRequest
import com.pt.ordersystem.ordersystem.domains.manager.models.toDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Manager Profile", description = "Endpoints for authenticated managers")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/managers/me")
@PreAuthorize(AUTH_MANAGER)
class ManagerSelfController(
  private val managerService: ManagerService,
) {

  @GetMapping
  fun getCurrentManager(@AuthenticationPrincipal manager: AuthUser): ResponseEntity<ManagerDto> =
    ResponseEntity.ok(managerService.getManagerByEmail(manager.email).toDto())

  @PutMapping
  fun updateCurrentManager(
    @RequestBody updatedDetails: UpdateManagerDetailsRequest,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<String> =
    ResponseEntity.ok(managerService.updateManagerDetails(manager.email, updatedDetails))

  @PutMapping("/update-password")
  fun updateCurrentManagerPassword(
    @RequestParam("old_password") oldPassword: String,
    @RequestParam("new_password") newPassword: String,
    @RequestParam("new_password_confirmation") newPasswordConfirmation: String,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<String> {
    managerService.updatePassword(manager.email, oldPassword, newPassword, newPasswordConfirmation)
    return ResponseEntity.ok("Password updated successfully")
  }
}

