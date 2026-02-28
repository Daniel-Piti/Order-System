package com.pt.ordersystem.ordersystem.domains.manager.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_ADMIN
import com.pt.ordersystem.ordersystem.domains.manager.ManagerService
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
) {

    @GetMapping
    fun getAllManagers(): ResponseEntity<List<ManagerDto>> {
        val managers = managerService.getAllManagers().map { it.toDto() }
        return ResponseEntity.ok(managers)
    }

    @PostMapping
    fun createManager(@RequestBody createManagerRequest: CreateManagerRequest): ResponseEntity<ManagerDto> {
        val normalizedRequest = createManagerRequest.normalize()
        val manager = managerService.createManager(normalizedRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(manager.toDto())
    }

    @PostMapping("/validate-password")
    fun validateManagerPassword(
        @RequestParam email: String,
        @RequestParam password: String,
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
        @RequestParam newPassword: String,
    ): ResponseEntity<String> {
        managerService.resetPassword(email, newPassword)
        return ResponseEntity.ok("Password reset successfully")
    }

    @DeleteMapping
    fun deleteManager(
        @RequestParam id: String,
        @RequestParam email: String,
    ): ResponseEntity<String> {
        managerService.deleteManagerByIdAndEmail(id, email)
        return ResponseEntity.ok("Manager deleted successfully | email=$email")
    }
}
