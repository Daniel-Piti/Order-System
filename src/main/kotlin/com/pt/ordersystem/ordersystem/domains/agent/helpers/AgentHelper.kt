package com.pt.ordersystem.ordersystem.domains.agent.helpers

import com.pt.ordersystem.ordersystem.domains.agent.models.NewAgentRequest

object AgentHelper {

    fun normalizeCreateAgentRequest(request: NewAgentRequest): NewAgentRequest =
        request.copy(
            firstName = request.firstName.trim(),
            lastName = request.lastName.trim(),
            email = request.email.lowercase().trim(),
            password = request.password.trim(),
            phoneNumber = request.phoneNumber.trim(),
            streetAddress = request.streetAddress.trim(),
            city = request.city.trim(),
        )

}