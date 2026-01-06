package com.pt.ordersystem.ordersystem.domains.business.models

enum class BusinessFailureReason(
  val userMessage: String,
  val technical: String
) {
  NOT_FOUND(
    userMessage = "Business not found",
    technical = "Business not found | "
  ),
  ALREADY_EXISTS(
    userMessage = "Business already exists for this manager",
    technical = "Business already exists for managerId="
  ),
  MANAGER_NOT_FOUND(
    userMessage = "Manager not found",
    technical = "Manager not found | "
  ),
}
