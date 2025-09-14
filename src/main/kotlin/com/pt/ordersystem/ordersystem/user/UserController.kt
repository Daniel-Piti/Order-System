package com.pt.ordersystem.ordersystem.user

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_ADMIN
import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_USER
import com.pt.ordersystem.ordersystem.auth.AuthUtils
import com.pt.ordersystem.ordersystem.user.models.UserDto
import com.pt.ordersystem.ordersystem.user.models.NewUserRequest
import com.pt.ordersystem.ordersystem.user.models.UpdateUserRequest
import com.pt.ordersystem.ordersystem.user.models.toDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Users", description = "User management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
class UserController(
  private val userService: UserService,
) {

  @PreAuthorize(AUTH_USER)
  @GetMapping("/me")
  fun getCurrentUser(): ResponseEntity<UserDto> {
    val email = AuthUtils.getCurrentUserEmail()
    return ResponseEntity.ok(userService.getUserByEmail(email).toDto())
  }

  @PreAuthorize(AUTH_ADMIN)
  @PostMapping("/create")
  fun createUser(@RequestBody newUserRequest: NewUserRequest): ResponseEntity<String> =
    ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(newUserRequest))

  @PreAuthorize(AUTH_USER)
  @PutMapping("/update")
  fun updateCurrentUser(@RequestBody updatedDetails: UpdateUserRequest): ResponseEntity<String> {
    val email = AuthUtils.getCurrentUserEmail()
    return ResponseEntity.ok(userService.updateUserDetails(email, updatedDetails))
  }

  @PreAuthorize(AUTH_ADMIN)
  @DeleteMapping("/delete")
  fun deleteUser(
    @RequestParam id: String,
    @RequestParam email: String
  ): ResponseEntity<String> {
    userService.deleteUserByIdAndEmail(id, email)
    return ResponseEntity.ok("User deleted successfully | email=$email")
  }

  @PreAuthorize(AUTH_USER)
  @PostMapping("/update_password")
  fun updateCurrentUserPassword(
    @RequestParam("old_password") oldPassword: String,
    @RequestParam("new_password") newPassword: String,
    @RequestParam("new_password_confirmation") newPasswordConfirmation: String
  ): ResponseEntity<String> {
    val email = AuthUtils.getCurrentUserEmail()
    userService.updatePassword(email, oldPassword, newPassword, newPasswordConfirmation)
    return ResponseEntity.ok("Password updated successfully")
  }

  @PreAuthorize(AUTH_ADMIN)
  @PostMapping("/validate_password")
  fun validatePassword(
    @RequestParam email: String,
    @RequestParam password: String
  ): ResponseEntity<String> {
      val isValid = userService.validatePassword(email, password)
      return if (isValid) ResponseEntity.ok("Password matches")
             else ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password does not match")
    }

  @PreAuthorize(AUTH_ADMIN)
  @PostMapping("/reset-password")
  fun resetPassword(@RequestBody email: String): ResponseEntity<String> {
    userService.resetPassword(email)
    return ResponseEntity.ok("Password reset successfully")
  }
}
