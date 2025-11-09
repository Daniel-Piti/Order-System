package com.pt.ordersystem.ordersystem.domains.manager

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_ADMIN
import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerDto
import com.pt.ordersystem.ordersystem.domains.manager.models.NewManagerRequest
import com.pt.ordersystem.ordersystem.domains.manager.models.UpdateManagerDetailsRequest
import com.pt.ordersystem.ordersystem.domains.manager.models.toDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Managers", description = "Manager management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/managers")
@PreAuthorize(AUTH_USER)
class ManagerController(
  private val managerService: ManagerService,
) {

  @GetMapping("/me")
  fun getCurrentManager(@AuthenticationPrincipal manager: AuthUser): ResponseEntity<ManagerDto> =
    ResponseEntity.ok(managerService.getManagerByEmail(manager.email).toDto())

  @PutMapping("/me")
  fun updateCurrentManager(
    @RequestBody updatedDetails: UpdateManagerDetailsRequest,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<String> =
    ResponseEntity.ok(managerService.updateManagerDetails(manager.email, updatedDetails))

  @PutMapping("/me/update-password")
  fun updateCurrentManagerPassword(
    @RequestParam("old_password") oldPassword: String,
    @RequestParam("new_password") newPassword: String,
    @RequestParam("new_password_confirmation") newPasswordConfirmation: String,
    @AuthenticationPrincipal manager: AuthUser
  ): ResponseEntity<String> {
    managerService.updatePassword(manager.email, oldPassword, newPassword, newPasswordConfirmation)
    return ResponseEntity.ok("Password updated successfully")
  }

  @PreAuthorize(AUTH_ADMIN)
  @GetMapping
  fun getAllManagers(): ResponseEntity<List<ManagerDto>> {
    val managers = managerService.getAllManagers().map { it.toDto() }
    return ResponseEntity.ok(managers)
  }

  @PreAuthorize(AUTH_ADMIN)
  @PostMapping
  fun createManager(@RequestBody newManagerRequest: NewManagerRequest): ResponseEntity<String> =
    ResponseEntity.status(HttpStatus.CREATED).body(managerService.createManager(newManagerRequest))

  @PreAuthorize(AUTH_ADMIN)
  @PostMapping("/validate-password")
  fun validateManagerPassword(
    @RequestParam email: String,
    @RequestParam password: String
  ): ResponseEntity<String> {
    val isValid = managerService.validateMatchingPassword(email, password)
    return if (isValid)
      ResponseEntity.ok("Password matches")
    else
      ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password does not match")
  }

  @PreAuthorize(AUTH_ADMIN)
  @PutMapping("/reset-password")
  fun resetManagerPassword(
    @RequestParam email: String,
    @RequestParam newPassword: String
  ): ResponseEntity<String> {
    managerService.resetPassword(email, newPassword)
    return ResponseEntity.ok("Password reset successfully")
  }

  @PreAuthorize(AUTH_ADMIN)
  @DeleteMapping
  fun deleteManager(
    @RequestParam id: String,
    @RequestParam email: String
  ): ResponseEntity<String> {
    managerService.deleteManagerByIdAndEmail(id, email)
    return ResponseEntity.ok("Manager deleted successfully | email=$email")
  }
}
