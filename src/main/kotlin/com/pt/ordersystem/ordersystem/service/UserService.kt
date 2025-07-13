package com.pt.ordersystem.ordersystem.service

import com.pt.ordersystem.ordersystem.dbEntity.User
import com.pt.ordersystem.ordersystem.dbEntity.toDto
import com.pt.ordersystem.ordersystem.dto.NewUserRequest
import com.pt.ordersystem.ordersystem.dto.UserDto
import com.pt.ordersystem.ordersystem.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(
  private val userRepository: UserRepository,
  private val passwordEncoder: BCryptPasswordEncoder,
) {

  fun getUserById(email: String): ResponseEntity<Any> {
    val user = userRepository.findByEmail(email)
      ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email $email not found")
    return ResponseEntity.ok(user.toDto())
  }

  fun updateUserDetails(email: String, updatedDetails: UserDto): ResponseEntity<String> {
    val user = userRepository.findByEmail(email)
      ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email $email not found")

    val updatedUser = user.copy(
      firstName = updatedDetails.firstName,
      lastName = updatedDetails.lastName,
      email = updatedDetails.email,
      phoneNumber = updatedDetails.phoneNumber,
      dateOfBirth = updatedDetails.dateOfBirth,
      mainAddress = updatedDetails.mainAddress,
      updatedAt = LocalDateTime.now()
    )

    userRepository.save(updatedUser)

    return ResponseEntity.ok("Details updated successfully")
  }


  fun createUser(newUserRequest: NewUserRequest): ResponseEntity<String> {
    if (!isPasswordStrong(newUserRequest.password))
      return ResponseEntity.badRequest().body("Password need to contain 8 characters, uppercase, lowercase, digit and special character")

    val newUser = User(
      id = UUID.randomUUID().toString(),
      firstName = newUserRequest.firstName,
      lastName = newUserRequest.lastName,
      email = newUserRequest.email,
      password = passwordEncoder.encode(newUserRequest.password),
      phoneNumber = newUserRequest.phoneNumber,
      dateOfBirth = newUserRequest.dateOfBirth,
      mainAddress = newUserRequest.mainAddress,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    val user = userRepository.save(newUser)

    return ResponseEntity.ok("User created with ID: ${user.id}")
  }

  fun validatePasswordForUser(email: String, password: String): ResponseEntity<String> {
    val user = userRepository.findByEmail(email)
      ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email $email not found")

    return if (passwordEncoder.matches(password, user.password)) ResponseEntity.ok("Password matches")
           else ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password does not match for user ${user.email}")
  }

  fun updatePassword(
    email: String,
    oldPassword: String,
    newPassword: String,
    newPasswordConfirmation: String
  ): ResponseEntity<String> {

    if (newPassword != newPasswordConfirmation)
      return ResponseEntity.badRequest().body("New password and confirmation do not match")

    if (oldPassword == newPassword)
      return ResponseEntity.badRequest().body("New password cannot be the same as the old password")

    if (!isPasswordStrong(newPassword))
      return ResponseEntity.badRequest().body("Password need to contain 8 characters, uppercase, lowercase, digit and special character")

    val user = userRepository.findByEmail(email)
      ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email $email not found")

    if (!passwordEncoder.matches(oldPassword, user.password))
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Old password is incorrect")

    val updatedUser = user.copy(
      password = passwordEncoder.encode(newPassword),
      updatedAt = LocalDateTime.now()
    )

    userRepository.save(updatedUser)

    return ResponseEntity.ok("Password updated successfully")
  }

  fun isPasswordStrong(password: String): Boolean {
    val lengthCheck = password.length >= 8
    val upperCheck = password.any { it.isUpperCase() }
    val lowerCheck = password.any { it.isLowerCase() }
    val digitCheck = password.any { it.isDigit() }
    val specialCheck = password.any { !it.isLetterOrDigit() }

    return lengthCheck && upperCheck && lowerCheck && digitCheck && specialCheck
  }

}