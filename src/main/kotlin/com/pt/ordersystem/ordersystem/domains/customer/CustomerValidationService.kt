package com.pt.ordersystem.ordersystem.domains.customer

import com.pt.ordersystem.ordersystem.domains.agent.AgentRepository
import com.pt.ordersystem.ordersystem.domains.customer.validators.CustomerValidatorsHelper
import org.springframework.stereotype.Service

@Service
class CustomerValidationService(
    private val customerRepository: CustomerRepository,
    private val agentRepository: AgentRepository,
) {

    fun validateCustomerCap(managerId: String, agentId: String?) {
        agentId?.also { agentRepository.findByManagerIdAndId(managerId, it) }
        val count = customerRepository.countByManagerIdAndAgentId(managerId, agentId)
        CustomerValidatorsHelper.validateCustomersCap(count, managerId, agentId)
    }

}
