package com.pt.ordersystem.ordersystem.domains.business

import com.pt.ordersystem.ordersystem.domains.business.models.Business
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessDbEntity
import com.pt.ordersystem.ordersystem.domains.business.models.BusinessFailureReason
import com.pt.ordersystem.ordersystem.domains.business.models.toModel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository

@Repository
class BusinessRepository(
    private val businessDao: BusinessDao,
) {

    fun findByManagerId(managerId: String): Business =
        businessDao.findByManagerId(managerId)?.toModel() ?: throw ServiceException(
            status = HttpStatus.NOT_FOUND,
            userMessage = BusinessFailureReason.NOT_FOUND.userMessage,
            technicalMessage = BusinessFailureReason.NOT_FOUND.technical + "managerId=$managerId",
            severity = SeverityLevel.WARN,
        )

    fun findEntityByManagerId(managerId: String): BusinessDbEntity =
        businessDao.findByManagerId(managerId) ?: throw ServiceException(
            status = HttpStatus.NOT_FOUND,
            userMessage = BusinessFailureReason.NOT_FOUND.userMessage,
            technicalMessage = BusinessFailureReason.NOT_FOUND.technical + "managerId=$managerId",
            severity = SeverityLevel.WARN,
        )

    fun findByManagerIdIn(managerIds: List<String>): List<Business> =
        businessDao.findByManagerIdIn(managerIds).map { it.toModel() }

    fun existsByManagerId(managerId: String): Boolean =
        businessDao.existsByManagerId(managerId)

    fun save(entity: BusinessDbEntity): Business =
        businessDao.save(entity).toModel()
}
