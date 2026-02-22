package com.pt.ordersystem.ordersystem.domains.agent

import com.pt.ordersystem.ordersystem.domains.agent.models.Agent
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentDbEntity
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentFailureReason
import com.pt.ordersystem.ordersystem.domains.agent.models.toModel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository

@Repository
class AgentRepository(
  private val agentDao: AgentDao,
) {

  fun findByManagerId(managerId: String): List<Agent> =
    agentDao.findByManagerId(managerId).map { it.toModel() }

  fun findByManagerIdAndId(managerId: String, id: String): Agent =
    agentDao.findByManagerIdAndId(managerId, id)?.toModel() ?: throw ServiceException(
      status = HttpStatus.NOT_FOUND,
      userMessage = AgentFailureReason.NOT_FOUND.userMessage,
      technicalMessage = AgentFailureReason.NOT_FOUND.technical + "managerId=$managerId, agentId=$id",
      severity = SeverityLevel.WARN,
    )

  fun findById(id: String): Agent {
    val entity = agentDao.findById(id).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = AgentFailureReason.NOT_FOUND.userMessage,
        technicalMessage = AgentFailureReason.NOT_FOUND.technical + "agentId=$id",
        severity = SeverityLevel.WARN,
      )
    }
    return entity.toModel()
  }

  fun findEntityById(id: String): AgentDbEntity =
    agentDao.findById(id).orElseThrow {
      ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = AgentFailureReason.NOT_FOUND.userMessage,
        technicalMessage = AgentFailureReason.NOT_FOUND.technical + "agentId=$id",
        severity = SeverityLevel.WARN,
      )
  }

  fun findEntityByEmail(email: String): AgentDbEntity? = agentDao.findByEmail(email)

  fun save(entity: AgentDbEntity): Agent = agentDao.save(entity).toModel()

  fun deleteById(id: String): Unit =
    agentDao.deleteById(id)

  fun existsByEmail(email: String): Boolean =
    agentDao.existsByEmail(email)

  fun countByManagerId(managerId: String): Long =
    agentDao.countByManagerId(managerId)
}
