package com.pt.ordersystem.ordersystem.domains.manager

import com.pt.ordersystem.ordersystem.domains.manager.helpers.ManagerValidators
import com.pt.ordersystem.ordersystem.domains.manager.models.Manager
import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerDbEntity
import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerFailureReason
import com.pt.ordersystem.ordersystem.domains.manager.models.CreateManagerRequest
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

    fun getAllManagers(): List<Manager> =
        managerRepository.findAll()

    fun getManagerByEmail(email: String): Manager =
        managerRepository.findByEmail(email)

    fun validateManagerExists(managerId: String) {
        if (!managerRepository.existsById(managerId)) {
            throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
                technicalMessage = ManagerFailureReason.NOT_FOUND.technical + "managerId=$managerId",
                severity = SeverityLevel.WARN,
            )
        }
    }

    fun createManager(createManagerRequest: CreateManagerRequest): Manager {
        ManagerValidators.validateCreateManagerRequestFields(createManagerRequest)

        if (managerRepository.existsByEmail(createManagerRequest.email)) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = ManagerFailureReason.EMAIL_ALREADY_EXISTS.userMessage,
                technicalMessage = ManagerFailureReason.EMAIL_ALREADY_EXISTS.technical + "email=${createManagerRequest.email}",
                severity = SeverityLevel.INFO,
            )
        }

        val now = LocalDateTime.now()
        val entity = ManagerDbEntity(
            id = GeneralUtils.genId(),
            firstName = createManagerRequest.firstName,
            lastName = createManagerRequest.lastName,
            email = createManagerRequest.email,
            password = passwordEncoder.encode(createManagerRequest.password),
            phoneNumber = createManagerRequest.phoneNumber,
            dateOfBirth = createManagerRequest.dateOfBirth,
            streetAddress = createManagerRequest.streetAddress,
            city = createManagerRequest.city,
            createdAt = now,
            updatedAt = now,
        )

        return managerRepository.save(entity)
    }

    fun updateManagerDetails(email: String, updateManagerDetailsRequest: UpdateManagerDetailsRequest): Manager {
        ManagerValidators.validateUpdateManagerRequestFields(updateManagerDetailsRequest)

        val normalizedEmail = email.trim().lowercase()
        val manager = managerRepository.getManagerEntityByEmail(normalizedEmail)

        val entity = manager.copy(
            firstName = updateManagerDetailsRequest.firstName,
            lastName = updateManagerDetailsRequest.lastName,
            phoneNumber = updateManagerDetailsRequest.phoneNumber,
            dateOfBirth = updateManagerDetailsRequest.dateOfBirth,
            streetAddress = updateManagerDetailsRequest.streetAddress,
            city = updateManagerDetailsRequest.city,
            updatedAt = LocalDateTime.now(),
        )

        return managerRepository.save(entity)
    }

    fun deleteManagerByIdAndEmail(id: String, email: String) {
        val manager = managerRepository.findById(id)

        val normalizedEmail = email.trim().lowercase()

        if (manager.email != normalizedEmail) {
            throw ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
                technicalMessage = ManagerFailureReason.NOT_FOUND.technical + "id=$id, email=$email",
                severity = SeverityLevel.WARN,
            )
        }

        managerRepository.deleteById(id)
    }

    fun validateMatchingPassword(email: String, password: String): Boolean {
        val normalizedEmail = email.trim().lowercase()
        val managerEntity = managerRepository.getManagerEntityByEmail(normalizedEmail)
        return passwordEncoder.matches(password, managerEntity.password)
    }

    fun updatePassword(
        email: String,
        oldPassword: String,
        newPassword: String,
        newPasswordConfirmation: String,
    ) {
        val normalizedEmail = email.trim().lowercase()

        FieldValidators.validateNewPasswordEqualConfirmationPassword(newPassword, newPasswordConfirmation)
        FieldValidators.validateNewPasswordNotEqualOldPassword(oldPassword, newPassword)
        FieldValidators.validateStrongPassword(newPassword)

        val managerEntity = managerRepository.getManagerEntityByEmail(normalizedEmail)

        if (!passwordEncoder.matches(oldPassword, managerEntity.password)) {
            throw ServiceException(
                status = HttpStatus.UNAUTHORIZED,
                userMessage = "Old password is incorrect",
                technicalMessage = "Password mismatch for manager with email=$normalizedEmail",
                severity = SeverityLevel.WARN,
            )
        }

        val updated = managerEntity.copy(
            password = passwordEncoder.encode(newPassword),
            updatedAt = LocalDateTime.now(),
        )
        managerRepository.save(updated)
    }

    fun resetPassword(email: String, newPassword: String) {
        val normalizedEmail = email.trim().lowercase()
        val trimmedPassword = newPassword.trim()

        FieldValidators.validateStrongPassword(trimmedPassword)

        val managerEntity = managerRepository.getManagerEntityByEmail(normalizedEmail)

        val updated = managerEntity.copy(
            password = passwordEncoder.encode(trimmedPassword),
            updatedAt = LocalDateTime.now(),
        )
        managerRepository.save(updated)
    }

}
