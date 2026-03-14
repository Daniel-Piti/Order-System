package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.domains.business.models.BusinessFailureReason
import com.pt.ordersystem.ordersystem.domains.business.models.CreateBusinessRequest
import com.pt.ordersystem.ordersystem.domains.manager.ManagerRepository
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class BusinessValidationService(
    private val managerRepository: ManagerRepository,
    private val businessRepository: BusinessRepository,
) {

    fun validateCreateBusiness(request: CreateBusinessRequest) {
        // Check if manager exists (throws if not found)
        managerRepository.findById(request.managerId)

        // Check if business already exists for this manager
        if (businessRepository.existsByManagerId(request.managerId)) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = BusinessFailureReason.ALREADY_EXISTS.userMessage,
                technicalMessage = BusinessFailureReason.ALREADY_EXISTS.technical + "managerId=${request.managerId}",
                severity = SeverityLevel.INFO
            )
        }
    }
}
