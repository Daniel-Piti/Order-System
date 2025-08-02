package com.pt.ordersystem.ordersystem.user

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Users", description = "User management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
class UserController(
  private val userService: UserService,
) {

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
  @GetMapping("/{email}")
  fun getUser(@PathVariable email: String): ResponseEntity<UserDto> =
    ResponseEntity.ok(userService.getUserByEmail(email).toDto())

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/create")
  fun createUser(@RequestBody newUserRequest: NewUserRequest): ResponseEntity<String> =
    ResponseEntity.ok(userService.createUser(newUserRequest))

  @PreAuthorize("hasAuthority('USER')")
  @PutMapping("/update")
  fun updateUser(
    @RequestParam email: String,
    @RequestBody updatedDetails: UpdateUserRequest
  ): ResponseEntity<String> =
    ResponseEntity.ok(userService.updateUserDetails(email, updatedDetails))

  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/delete")
  fun deleteUser(
    @RequestParam id: String,
    @RequestParam email: String
  ): ResponseEntity<String> {
    userService.deleteUserByIdAndEmail(id, email)
    return ResponseEntity.ok("User with deleted successfully | email=$email")
  }

  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/update_password")
  fun updatePassword(
    @RequestParam email: String,
    @RequestParam("old_password") oldPassword: String,
    @RequestParam("new_password") newPassword: String,
    @RequestParam("new_password_confirmation") newPasswordConfirmation: String
  ): ResponseEntity<String> {
    userService.updatePassword(email, oldPassword, newPassword, newPasswordConfirmation)
    return ResponseEntity.ok("Password updated successfully | email=$email")
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/validate_password")
  fun validatePassword(
    @RequestParam email: String,
    @RequestParam password: String
  ): ResponseEntity<String> {
      val isValid = userService.validatePassword(email, password)
      return if (isValid) ResponseEntity.ok("Password matches")
             else ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password does not match")
    }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/users/reset-password")
  fun resetPassword(@RequestBody email: String): ResponseEntity<String> {
    userService.resetPassword(email)
    return ResponseEntity.ok("Password reset successfully")
  }
}
