package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.domains.customer.models.Customer
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerDbEntity
import com.pt.ordersystem.ordersystem.domains.customer.models.CustomerFailureReason
import com.pt.ordersystem.ordersystem.domains.customer.models.toModel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository

@Repository
class CustomerRepository(
  private val customerDao: CustomerDao,
) {

  fun findByManagerId(managerId: String): List<Customer> =
    customerDao.findByManagerId(managerId).map { it.toModel() }

  fun findByManagerIdAndId(managerId: String, id: String): Customer {
    val entity = customerDao.findByManagerIdAndId(managerId, id)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.userMessage,
        technicalMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.technical + "managerId=$managerId customerId=$id",
        severity = SeverityLevel.WARN,
      )
    return entity.toModel()
  }

  fun findByManagerIdAndPhoneNumber(managerId: String, phoneNumber: String): Customer? =
    customerDao.findByManagerIdAndPhoneNumber(managerId, phoneNumber)?.toModel()

  fun findByManagerIdAndAgentId(managerId: String, agentId: String): List<Customer> =
    customerDao.findByManagerIdAndAgentId(managerId, agentId).map { it.toModel() }

  fun findByManagerIdAndAgentIdAndId(managerId: String, agentId: String, id: String): Customer {
    val entity = customerDao.findByManagerIdAndAgentIdAndId(managerId, agentId, id)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.userMessage,
        technicalMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.technical + "managerId=$managerId agentId=$agentId customerId=$id",
        severity = SeverityLevel.WARN,
      )
    return entity.toModel()
  }

  fun countByAgentId(agentId: String): Long =
    customerDao.countByAgentId(agentId)

  fun countByManagerIdAndAgentIdIsNull(managerId: String): Long =
    customerDao.countByManagerIdAndAgentIdIsNull(managerId)

  fun save(entity: CustomerDbEntity): Customer {
    val saved = customerDao.save(entity)
    return saved.toModel()
  }

  fun deleteById(id: String) {
    customerDao.deleteById(id)
  }
}
