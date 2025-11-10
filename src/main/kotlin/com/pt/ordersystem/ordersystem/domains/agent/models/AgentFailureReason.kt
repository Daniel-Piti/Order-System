package com.pt.ordersystem.ordersystem.domains.agent.models

enum class AgentFailureReason(
  val userMessage: String,
  val technical: String,
) {
  NOT_FOUND(
    userMessage = "Agent not found",
    technical = "Agent not found. ",
  ),
  EMAIL_ALREADY_EXISTS(
    userMessage = "An agent with this email already exists",
    technical = "Agent email conflict. ",
  ),
  LIMIT_REACHED(
    userMessage = "Agent limit reached",
    technical = "Manager has reached the maximum number of agents. ",
  ),
}
