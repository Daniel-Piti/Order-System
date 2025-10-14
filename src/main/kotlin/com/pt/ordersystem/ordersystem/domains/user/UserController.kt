package com.pt.ordersystem.ordersystem.domains.user

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_ADMIN
import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.user.models.UserDto
import com.pt.ordersystem.ordersystem.domains.user.models.NewUserRequest
import com.pt.ordersystem.ordersystem.domains.user.models.UpdateUserDetailsRequest
import com.pt.ordersystem.ordersystem.domains.user.models.toDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Users", description = "User management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@PreAuthorize(AUTH_USER)
class UserController(
  private val userService: UserService,
) {

  @GetMapping("/me")
  fun getCurrentUser(@AuthenticationPrincipal user: AuthUser): ResponseEntity<UserDto> =
    ResponseEntity.ok(userService.getUserByEmail(user.email).toDto())

  @PreAuthorize(AUTH_ADMIN)
  @PostMapping
  fun createUser(@RequestBody newUserRequest: NewUserRequest): ResponseEntity<String> =
    ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(newUserRequest))

  @PutMapping
  fun updateCurrentUserPersonalDetails(
    @RequestBody updatedDetails: UpdateUserDetailsRequest,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> =
    ResponseEntity.ok(userService.updateUserDetails(user.email, updatedDetails))

  @PostMapping("/update-password")
  fun updateCurrentUserPassword(
    @RequestParam("old_password") oldPassword: String,
    @RequestParam("new_password") newPassword: String,
    @RequestParam("new_password_confirmation") newPasswordConfirmation: String,
    @AuthenticationPrincipal user: AuthUser
  ): ResponseEntity<String> {
    userService.updatePassword(user.email, oldPassword, newPassword, newPasswordConfirmation)
    return ResponseEntity.ok("Password updated successfully")
  }

  @PreAuthorize(AUTH_ADMIN)
  @PostMapping("/validate-password")
  fun validateUserPassword(
    @RequestParam email: String,
    @RequestParam password: String
  ): ResponseEntity<String> {
    val isValid = userService.validateMatchingPassword(email, password)
    return if (isValid)
      ResponseEntity.ok("Password matches")
    else
      ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password does not match")
  }

  @PreAuthorize(AUTH_ADMIN)
  @PostMapping("/reset-password")
  fun resetUserPassword(@RequestBody email: String): ResponseEntity<String> {
    userService.resetPassword(email)
    return ResponseEntity.ok("Password reset successfully")
  }

  @PreAuthorize(AUTH_ADMIN)
  @DeleteMapping
  fun deleteUser(
    @RequestParam id: String,
    @RequestParam email: String
  ): ResponseEntity<String> {
    userService.deleteUserByIdAndEmail(id, email)
    return ResponseEntity.ok("User deleted successfully | email=$email")
  }

}
