package com.pt.ordersystem.ordersystem.user

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.user.models.UserDbEntity
import com.pt.ordersystem.ordersystem.user.models.UserFailureReason
import com.pt.ordersystem.ordersystem.user.models.NewUserRequest
import com.pt.ordersystem.ordersystem.user.models.UpdateUserRequest
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService(
  private val userRepository: UserRepository,
  private val passwordEncoder: BCryptPasswordEncoder,
) {

  companion object {
    const val DEFAULT_PASSWORD = "Aa123456!"
  }

  fun getUserByEmail(email: String): UserDbEntity =
    userRepository.findByEmail(email) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = UserFailureReason.NOT_FOUND.userMessage,
      technicalMessage = UserFailureReason.NOT_FOUND.technical + "email=$email",
      severity = SeverityLevel.WARN
    )

  fun createUser(newUserRequest: NewUserRequest): String {
    if (!isPasswordStrong(newUserRequest.password))
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = UserFailureReason.PASSWORD_TOO_WEAK.userMessage,
        technicalMessage = UserFailureReason.PASSWORD_TOO_WEAK.technical + newUserRequest.mainAddress,
        severity = SeverityLevel.WARN
      )

    val newUserDbEntity = UserDbEntity(
      id = GeneralUtils.genId(),
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

    val user = userRepository.save(newUserDbEntity)

    return user.id
  }

  fun updateUserDetails(email: String, updatedDetails: UpdateUserRequest): String {
    println(SecurityContextHolder.getContext().authentication)
    println(SecurityContextHolder.getContext().authentication.details)
    println(SecurityContextHolder.getContext().authentication.authorities)
    println(SecurityContextHolder.getContext().authentication.credentials)
    println(SecurityContextHolder.getContext().authentication.principal)
    println(SecurityContextHolder.getContext().authentication.isAuthenticated)
    println(SecurityContextHolder.getContext().authentication.name)

    val user = userRepository.findByEmail(email) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = UserFailureReason.NOT_FOUND.userMessage,
      technicalMessage = UserFailureReason.NOT_FOUND.technical + "email=$email",
      severity = SeverityLevel.WARN
    )

    val userToSave = user.copy(
      firstName = updatedDetails.firstName,
      lastName = updatedDetails.lastName,
      phoneNumber = updatedDetails.phoneNumber,
      dateOfBirth = updatedDetails.dateOfBirth,
      mainAddress = updatedDetails.mainAddress,
      updatedAt = LocalDateTime.now()
    )

    val updatedUser = userRepository.save(userToSave)

    return updatedUser.id
  }

  fun deleteUserByIdAndEmail(id: String, email: String) {
    val user = userRepository.findById(id).orElse(null)

    if (user == null || user.email != email) {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = UserFailureReason.NOT_FOUND.userMessage,
        technicalMessage = UserFailureReason.NOT_FOUND.technical + "id=$id, email=$email",
        severity = SeverityLevel.WARN
      )
    }
      userRepository.delete(user)
  }

  fun validatePassword(email: String, password: String): Boolean {
    val user = userRepository.findByEmail(email) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = UserFailureReason.NOT_FOUND.userMessage,
      technicalMessage = UserFailureReason.NOT_FOUND.technical + "email=$email",
      severity = SeverityLevel.WARN
    )

    return passwordEncoder.matches(password, user.password)
  }

  fun updatePassword(
    email: String,
    oldPassword: String,
    newPassword: String,
    newPasswordConfirmation: String
  ) {
    if (newPassword != newPasswordConfirmation)
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "New password and confirmation do not match",
        technicalMessage = "Mismatch between passwords",
        severity = SeverityLevel.INFO
      )

    if (oldPassword == newPassword) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "New password cannot be the same as the old password",
        technicalMessage = "New password = old password",
        severity = SeverityLevel.INFO
      )
    }

    if (!isPasswordStrong(newPassword)) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = UserFailureReason.PASSWORD_TOO_WEAK.userMessage,
        technicalMessage = UserFailureReason.PASSWORD_TOO_WEAK.technical + "email=$email",
        severity = SeverityLevel.INFO
      )
    }

    val user = userRepository.findByEmail(email)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = UserFailureReason.NOT_FOUND.userMessage,
        technicalMessage = UserFailureReason.NOT_FOUND.technical + "email=$email",
        severity = SeverityLevel.WARN
      )

    if (!passwordEncoder.matches(oldPassword, user.password)) {
      throw ServiceException(
        status = HttpStatus.UNAUTHORIZED,
        userMessage = "Old password is incorrect",
        technicalMessage = "Password mismatch for user with email=$email",
        severity = SeverityLevel.WARN
      )
    }

    val updatedUser = user.copy(
      password = passwordEncoder.encode(newPassword),
      updatedAt = LocalDateTime.now()
    )
    userRepository.save(updatedUser)
  }

  fun isPasswordStrong(password: String): Boolean {
    val lengthCheck = password.length >= 8
    val upperCheck = password.any { it.isUpperCase() }
    val lowerCheck = password.any { it.isLowerCase() }
    val digitCheck = password.any { it.isDigit() }
    val specialCheck = password.any { !it.isLetterOrDigit() }

    return lengthCheck && upperCheck && lowerCheck && digitCheck && specialCheck
  }

  fun resetPassword(email: String) {
    val user = userRepository.findByEmail(email)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = UserFailureReason.NOT_FOUND.userMessage,
        technicalMessage = "User not found with email=$email",
        severity = SeverityLevel.INFO
      )

    user.password = passwordEncoder.encode(DEFAULT_PASSWORD)
    userRepository.save(user)
  }
}
