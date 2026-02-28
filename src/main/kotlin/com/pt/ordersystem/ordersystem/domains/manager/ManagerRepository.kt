package com.pt.ordersystem.ordersystem.domains.manager

import com.pt.ordersystem.ordersystem.domains.manager.models.Manager
import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerDbEntity
import com.pt.ordersystem.ordersystem.domains.manager.models.ManagerFailureReason
import com.pt.ordersystem.ordersystem.domains.manager.models.toModel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository

@Repository
class ManagerRepository(
    private val managerDao: ManagerDao,
) {

    fun findAll(): List<Manager> =
        managerDao.findAll().map { it.toModel() }

    fun findByEmail(email: String): Manager =
        managerDao.findByEmail(email.trim().lowercase())?.toModel() ?: throw ServiceException(
            status = HttpStatus.NOT_FOUND,
            userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
            technicalMessage = ManagerFailureReason.NOT_FOUND.technical + "email=$email",
            severity = SeverityLevel.WARN,
        )

    fun findById(id: String): Manager =
        managerDao.findById(id).orElseThrow {
            ServiceException(
                status = HttpStatus.NOT_FOUND,
                userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
                technicalMessage = ManagerFailureReason.NOT_FOUND.technical + "managerId=$id",
                severity = SeverityLevel.WARN,
            )
        }.toModel()

    fun existsById(id: String): Boolean = managerDao.existsById(id)

    fun existsByEmail(email: String): Boolean = managerDao.existsByEmail(email.trim().lowercase())

    fun save(entity: ManagerDbEntity): Manager = managerDao.save(entity).toModel()

    fun deleteById(id: String) { managerDao.deleteById(id) }

    fun getManagerEntityByEmail(email: String): ManagerDbEntity =
        managerDao.findByEmail(email.trim().lowercase()) ?: throw ServiceException(
            status = HttpStatus.NOT_FOUND,
            userMessage = ManagerFailureReason.NOT_FOUND.userMessage,
            technicalMessage = ManagerFailureReason.NOT_FOUND.technical + "email=$email",
            severity = SeverityLevel.WARN,
        )

}
