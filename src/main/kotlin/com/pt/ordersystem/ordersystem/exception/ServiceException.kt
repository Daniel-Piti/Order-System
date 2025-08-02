package com.pt.ordersystem.ordersystem.exception

import org.springframework.http.HttpStatus

class ServiceException(
  val status: HttpStatus,
  val userMessage: String,
  val technicalMessage: String,
  val severity: SeverityLevel,
) : RuntimeException(technicalMessage)

data class FailureResponse(
  val status: HttpStatus,
  val userMessage: String,
  val technicalMessage: String,
  val severity: SeverityLevel,
)

enum class SeverityLevel {
  DEBUG,
  INFO,
  WARN,
  ERROR,
  FATAL
}
