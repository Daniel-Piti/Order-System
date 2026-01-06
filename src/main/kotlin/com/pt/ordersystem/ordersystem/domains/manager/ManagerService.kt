package com.pt.ordersystem.ordersystem.domains.manager

import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerDbEntity
import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerFailureReason
import com.pt.ordersystem.ordersystem.domains.manager.models.NewManagerRequest
import com.pt.ordersystem.ordersystem.domains.manager.models.UpdateManagerDetailsRequest
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import com.pt.ordersystem.ordersystem.utils.GeneralUtils
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ManagerService(
  private val managerRepository: ManagerRepository,
  private val passwordEncoder: BCryptPasswordEncoder,
) {

  fun getAllManagers(): List<ManagerDbEntity> = managerRepository.findAll()

  fun getManagerByEmail(email: String): ManagerDbEntity =
    managerRepository.findByEmail(email) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
      technicalMessage = ManagerFailureReason.NOT_FOUND.technical + "email=$email",
      severity = SeverityLevel.WARN
    )

  fun getManagerById(managerId: String): ManagerDbEntity =
    managerRepository.findById(managerId).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ManagerFailureReason.NOT_FOUND.technical + "managerId=$managerId",
        severity = SeverityLevel.WARN
      )
    }

  fun createManager(newManagerRequest: NewManagerRequest): String {

    with(newManagerRequest) {
      FieldValidators.validateNonEmpty(firstName, "'first name'")
      FieldValidators.validateNonEmpty(lastName, "'last name'")
      FieldValidators.validateEmail(email)
      FieldValidators.validateStrongPassword(password)
      FieldValidators.validatePhoneNumber(phoneNumber)
      FieldValidators.validateDateNotFuture(dateOfBirth)
      FieldValidators.validateNonEmpty(streetAddress, "'street address'")
      FieldValidators.validateNonEmpty(city, "'city'")
    }

    val managerEntity = ManagerDbEntity(
      id = GeneralUtils.genId(),
      firstName = newManagerRequest.firstName,
      lastName = newManagerRequest.lastName,
      email = newManagerRequest.email,
      password = passwordEncoder.encode(newManagerRequest.password),
      phoneNumber = newManagerRequest.phoneNumber,
      dateOfBirth = newManagerRequest.dateOfBirth,
      streetAddress = newManagerRequest.streetAddress,
      city = newManagerRequest.city,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now()
    )

    val manager = managerRepository.save(managerEntity)

    return manager.id
  }

  fun updateManagerDetails(email: String, updatedManagerDetails: UpdateManagerDetailsRequest): String {

    with(updatedManagerDetails) {
      FieldValidators.validateNonEmpty(firstName, "'first name'")
      FieldValidators.validateNonEmpty(lastName, "'last name'")
      FieldValidators.validatePhoneNumber(phoneNumber)
      FieldValidators.validateDateNotFuture(dateOfBirth)
      FieldValidators.validateNonEmpty(streetAddress, "'street address'")
      FieldValidators.validateNonEmpty(city, "'city'")
    }

    val manager = managerRepository.findByEmail(email) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
      technicalMessage = ManagerFailureReason.NOT_FOUND.technical + "email=$email",
      severity = SeverityLevel.WARN
    )

    val managerToSave = manager.copy(
      firstName = updatedManagerDetails.firstName,
      lastName = updatedManagerDetails.lastName,
      phoneNumber = updatedManagerDetails.phoneNumber,
      dateOfBirth = updatedManagerDetails.dateOfBirth,
      streetAddress = updatedManagerDetails.streetAddress,
      city = updatedManagerDetails.city,
      updatedAt = LocalDateTime.now()
    )

    val updatedManager = managerRepository.save(managerToSave)

    return updatedManager.id
  }

  fun deleteManagerByIdAndEmail(id: String, email: String) {
    val manager = managerRepository.findById(id).orElse(null)

    if (manager == null || manager.email != email) {
      throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ManagerFailureReason.NOT_FOUND.technical + "id=$id, email=$email",
        severity = SeverityLevel.WARN
      )
    }

    managerRepository.delete(manager)
  }

  fun validateMatchingPassword(email: String, password: String): Boolean {
    val manager = managerRepository.findByEmail(email) ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
      technicalMessage = ManagerFailureReason.NOT_FOUND.technical + "email=$email",
      severity = SeverityLevel.WARN
    )

    return passwordEncoder.matches(password, manager.password)
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

    FieldValidators.validateStrongPassword(newPassword)

    val manager = managerRepository.findByEmail(email)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
        technicalMessage = ManagerFailureReason.NOT_FOUND.technical + "email=$email",
        severity = SeverityLevel.WARN
      )

    if (!passwordEncoder.matches(oldPassword, manager.password)) {
      throw ServiceException(
        status = HttpStatus.UNAUTHORIZED,
        userMessage = "Old password is incorrect",
        technicalMessage = "Password mismatch for manager with email=$email",
        severity = SeverityLevel.WARN
      )
    }

    val updatedManager = manager.copy(
      password = passwordEncoder.encode(newPassword),
      updatedAt = LocalDateTime.now()
    )
    managerRepository.save(updatedManager)
  }

  fun resetPassword(email: String, newPassword: String) {
    val manager = managerRepository.findByEmail(email)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
        technicalMessage = "Manager not found with email=$email",
        severity = SeverityLevel.INFO
      )

    manager.password = passwordEncoder.encode(newPassword)
    managerRepository.save(manager)
  }
}
