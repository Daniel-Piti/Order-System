package com.pt.ordersystem.ordersystem.domains.manager

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
    private val managerValidationService: ManagerValidationService,
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

    fun updateManagerDetails(managerId: String, updateManagerDetailsRequest: UpdateManagerDetailsRequest): Manager {

        val managerEntity = managerRepository.findEntityById(managerId)

        val updatedManager = managerEntity.copy(
            firstName = updateManagerDetailsRequest.firstName,
            lastName = updateManagerDetailsRequest.lastName,
            phoneNumber = updateManagerDetailsRequest.phoneNumber,
            dateOfBirth = updateManagerDetailsRequest.dateOfBirth,
            streetAddress = updateManagerDetailsRequest.streetAddress,
            city = updateManagerDetailsRequest.city,
            updatedAt = LocalDateTime.now(),
        )

        return managerRepository.save(updatedManager)
    }

    fun deleteManagerByIdAndEmail(managerId: String) =
        managerRepository.deleteById(managerId)

    fun validateMatchingPassword(email: String, password: String): Boolean {
        val managerEntity = managerRepository.getManagerEntityByEmail(email)
        return passwordEncoder.matches(password, managerEntity.password)
    }

    fun updatePassword(
        email: String,
        oldPassword: String,
        newPassword: String,
    ) {
        val managerEntity = managerRepository.getManagerEntityByEmail(email)

        managerValidationService.validateManagerPasswordMatches(oldPassword, managerEntity)

        val updated = managerEntity.copy(
            password = passwordEncoder.encode(newPassword),
            updatedAt = LocalDateTime.now(),
        )

        managerRepository.save(updated)
    }

    fun resetPassword(email: String, newPassword: String) {
        FieldValidators.validateStrongPassword(newPassword)

        val managerEntity = managerRepository.getManagerEntityByEmail(email)

        val updated = managerEntity.copy(
            password = passwordEncoder.encode(newPassword),
            updatedAt = LocalDateTime.now(),
        )

        managerRepository.save(updated)
    }

}
