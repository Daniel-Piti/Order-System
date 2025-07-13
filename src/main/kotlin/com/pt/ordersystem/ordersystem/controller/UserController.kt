package com.pt.ordersystem.ordersystem.controller

import com.pt.ordersystem.ordersystem.dto.NewUserRequest
import com.pt.ordersystem.ordersystem.dto.UserDto
import com.pt.ordersystem.ordersystem.service.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Users", description = "User management API")
@RestController
@RequestMapping("/users")
class UserController(
  private val userService: UserService,
) {

  @GetMapping("/{email}")
  fun getUser(@PathVariable email: String): ResponseEntity<Any> = userService.getUserById(email)

  @PutMapping("/{email}")
  fun updateUserDetails(
    @PathVariable email: String,
    @RequestBody updatedDetails: UserDto
  ): ResponseEntity<String> =
    userService.updateUserDetails(email, updatedDetails)

  @PostMapping("/create")
  fun createUser(@RequestBody newUserRequest: NewUserRequest): ResponseEntity<String> =
    userService.createUser(newUserRequest)

  @PostMapping("/update_password")
  fun updatePasswordForUser(
    email: String,
    oldPassword: String,
    newPassword: String,
    newPasswordConfirmation: String,
  ): ResponseEntity<String> =
    userService.updatePassword(email, oldPassword, newPassword, newPasswordConfirmation)

  @PostMapping("/validate_password")
  fun validatePassword(email: String, password: String): ResponseEntity<String> =
    userService.validatePasswordForUser(email, password)

}
