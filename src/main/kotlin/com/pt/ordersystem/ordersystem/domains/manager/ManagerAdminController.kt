package com.pt.ordersystem.ordersystem.domains.manager

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_ADMIN
import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerDto
import com.pt.ordersystem.ordersystem.domains.manager.models.NewManagerRequest
import com.pt.ordersystem.ordersystem.domains.manager.models.toDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Manager Management", description = "Admin manager management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/managers")
@PreAuthorize(AUTH_ADMIN)
class ManagerAdminController(
  private val managerService: ManagerService,
) {

  @GetMapping
  fun getAllManagers(): ResponseEntity<List<ManagerDto>> {
    val managers = managerService.getAllManagers().map { it.toDto() }
    return ResponseEntity.ok(managers)
  }

  @PostMapping
  fun createManager(@RequestBody newManagerRequest: NewManagerRequest): ResponseEntity<String> =
    ResponseEntity.status(HttpStatus.CREATED).body(managerService.createManager(newManagerRequest))

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

  @PutMapping("/reset-password")
  fun resetManagerPassword(
    @RequestParam email: String,
    @RequestParam newPassword: String
  ): ResponseEntity<String> {
    managerService.resetPassword(email, newPassword)
    return ResponseEntity.ok("Password reset successfully")
  }

  @DeleteMapping
  fun deleteManager(
    @RequestParam id: String,
    @RequestParam email: String
  ): ResponseEntity<String> {
    managerService.deleteManagerByIdAndEmail(id, email)
    return ResponseEntity.ok("Manager deleted successfully | email=$email")
  }
}

