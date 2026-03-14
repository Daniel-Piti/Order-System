package com.pt.ordersystem.ordersystem.domains.manager

import com.pt.ordersystem.ordersystem.domains.manager.helpers.ManagerValidators
import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerDbEntity
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class ManagerValidationService(
    private val managerRepository: ManagerRepository,
    private val passwordEncoder: BCryptPasswordEncoder,

    ) {

    fun validateCreateManager(email: String) {
        val exists = managerRepository.existsByEmail(email)
        ManagerValidators.validateManagerExists(exists, email)
    }

    fun validateManagerMatchEmail(managerId: String, email: String) {
        val manager = managerRepository.findById(managerId)
        ManagerValidators.validateMatchingEmail(manager, email)
    }

    fun validateUpdateManagerPasswordFields(oldPassword: String, newPassword: String, newPasswordConfirmation: String) {
        FieldValidators.validateNewPasswordEqualConfirmationPassword(newPassword, newPasswordConfirmation)
        FieldValidators.validateNewPasswordNotEqualOldPassword(oldPassword, newPassword)
        FieldValidators.validateStrongPassword(newPassword)
    }
    
    fun validateManagerPasswordMatches(oldPassword: String, managerEntity: ManagerDbEntity) {
        if (!passwordEncoder.matches(oldPassword, managerEntity.password)) {
            throw ServiceException(
                status = HttpStatus.UNAUTHORIZED,
                userMessage = "Old password is incorrect",
                technicalMessage = "Password mismatch for manager with email=${managerEntity.email}",
                severity = SeverityLevel.WARN,
            )
        }
    }

}