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

  fun findByAgentId(agentId: String): List<Customer> =
    customerDao.findByAgentId(agentId).map { it.toModel() }

  fun findByManagerId(managerId: String): List<Customer> =
    customerDao.findByManagerId(managerId).map { it.toModel() }

  fun findByManagerIdAndId(managerId: String, customerId: String): Customer {
    val entity = customerDao.findByManagerIdAndId(managerId, customerId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.userMessage,
        technicalMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.technical + "managerId=$managerId customerId=$customerId",
        severity = SeverityLevel.WARN,
      )
    return entity.toModel()
  }

  fun findByManagerIdAndAgentIdAndId(managerId: String, agentId: String?, customerId: String): Customer {
    val entity = customerDao.findByManagerIdAndAgentIdAndId(managerId, agentId, customerId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.userMessage,
        technicalMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.technical + "managerId=$managerId agentId=$agentId customerId=$customerId",
        severity = SeverityLevel.WARN,
      )
    return entity.toModel()
  }

  fun findEntityByManagerIdAndAgentIdAndId(managerId: String, agentId: String?, customerId: String): CustomerDbEntity =
    customerDao.findByManagerIdAndAgentIdAndId(managerId, agentId, customerId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.userMessage,
        technicalMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.technical + "managerId=$managerId agentId=$agentId customerId=$customerId",
        severity = SeverityLevel.WARN,
      )

  fun countByManagerIdAndAgentId(managerId: String, agentId: String?): Long =
    customerDao.countByManagerIdAndAgentId(managerId, agentId)

  fun deleteByManagerIdAndAgentId(managerId: String, agentId: String) {
    customerDao.deleteByManagerIdAndAgentId(managerId, agentId)
  }

  fun save(entity: CustomerDbEntity): Customer {
    val saved = customerDao.save(entity)
    return saved.toModel()
  }

  fun deleteById(id: String) {
    customerDao.deleteById(id)
  }
}
