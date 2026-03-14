package com.pt.ordersystem.ordersystem.domains.manager.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_ADMIN
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
import com.pt.ordersystem.ordersystem.domains.manager.ManagerValidationService
import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerDto
import com.pt.ordersystem.ordersystem.domains.manager.models.CreateManagerRequest
import com.pt.ordersystem.ordersystem.domains.manager.models.toDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Manager Management", description = "Admin manager management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/managers")
@PreAuthorize(AUTH_ADMIN)
class ManagerAdminController(
    private val managerService: ManagerService,
    private val managerValidationService: ManagerValidationService,
) {

    @GetMapping
    fun getAllManagers(): ResponseEntity<List<ManagerDto>> {
        val managers = managerService.getAllManagers()
        return ResponseEntity.ok(managers.map { it.toDto() })
    }

    @PostMapping
    fun createManager(
        @RequestBody createManagerRequest: CreateManagerRequest
    ): ResponseEntity<ManagerDto> {
        val normalizedRequest = createManagerRequest.validateAndNormalize()

        managerValidationService.validateCreateManager(createManagerRequest.email)
        val manager = managerService.createManager(normalizedRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(manager.toDto())
    }

    @PostMapping("/validate-password")
    fun validateManagerPassword(
        @RequestParam email: String,
        @RequestParam password: String,
    ): ResponseEntity<String> {
        val normalizedEmail = email.trim().lowercase()

        val isValid = managerService.validateMatchingPassword(normalizedEmail, password)
        return if (isValid)
            ResponseEntity.ok("Password matches")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password does not match")
    }

    @PutMapping("/reset-password")
    fun resetManagerPassword(
        @RequestParam email: String,
        @RequestParam newPassword: String,
    ): ResponseEntity<String> {
        val normalizedEmail = email.trim().lowercase()
        val normalizedPassword = newPassword.trim()

        managerService.resetPassword(normalizedEmail, normalizedPassword)
        return ResponseEntity.ok("Password reset successfully")
    }

    @DeleteMapping
    fun deleteManager(
        @RequestParam managerId: String,
        @RequestParam email: String,
    ): ResponseEntity<String> {
        val normalizedEmail = email.trim().lowercase()

        managerValidationService.validateManagerMatchEmail(managerId, normalizedEmail)
        managerService.deleteManagerByIdAndEmail(managerId)
        return ResponseEntity.ok("Manager deleted successfully | email=$email")
    }
}
