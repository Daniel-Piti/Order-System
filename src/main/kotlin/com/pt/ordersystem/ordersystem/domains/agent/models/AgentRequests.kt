package com.pt.ordersystem.ordersystem.domains.agent.models

import com.pt.ordersystem.ordersystem.domains.agent.helpers.AgentValidators

data class NewAgentRequest(
  val firstName: String,
  val lastName: String,
  val email: String,
  val password: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
) {
  fun validateAndNormalize(): NewAgentRequest {
    AgentValidators.validateNewAgentRequest(this)

    return this.copy(
      firstName = firstName.trim(),
      lastName = lastName.trim(),
      email = email.trim().lowercase(),
      phoneNumber = phoneNumber.trim(),
      streetAddress = streetAddress.trim(),
      city = city.trim(),
    )
  }
}

data class UpdateAgentRequest(
  val firstName: String,
  val lastName: String,
  val phoneNumber: String,
  val streetAddress: String,
  val city: String,
) {
  fun validateAndNormalize(): UpdateAgentRequest {
    AgentValidators.validateUpdateAgentRequest(this)

    return this.copy(
      firstName = firstName.trim(),
      lastName = lastName.trim(),
      phoneNumber = phoneNumber.trim(),
      streetAddress = streetAddress.trim(),
      city = city.trim(),
    )
  }
}
