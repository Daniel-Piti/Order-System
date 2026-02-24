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

  fun findByAgentIdAndId(agentId: String, customerId: String): Customer {
    val entity = customerDao.findByAgentIdAndId(agentId, customerId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.userMessage,
        technicalMessage = CustomerFailureReason.CUSTOMER_NOT_FOUND.technical + "agentId=$agentId customerId=$customerId",
        severity = SeverityLevel.WARN,
      )
    return entity.toModel()
  }

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

  fun findByManagerIdAndAgentId(managerId: String, agentId: String): List<Customer> =
    customerDao.findByManagerIdAndAgentId(managerId, agentId).map { it.toModel() }

  fun findByManagerIdAndAgentIdAndId(managerId: String, agentId: String?, id: String): Customer {
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
