package com.pt.ordersystem.ordersystem.user

enum class UserFailureReason(val userMessage: String, val technical: String) {
  NOT_FOUND(
    userMessage = "User not found",
    technical = "User not found | "
  ),
  INVALID_PASSWORD(
    userMessage = "Invalid password",
    technical = "Invalid password | "
  ),
  PASSWORD_TOO_WEAK(
    userMessage = "Password need to contain 8 characters, uppercase, lowercase, digit and special character",
    technical = "Password does not meet strength requirements | ",
  ),
  EMAIL_ALREADY_EXISTS(
    userMessage = "Email already exists",
    technical = "Email already exists | "
  ),
  UNAUTHORIZED_ACCESS(
    userMessage = "Unauthorized access",
    technical = "Unauthorized access | "
  ),
}
