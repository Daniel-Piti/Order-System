package com.pt.ordersystem.ordersystem.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(ServiceException::class)
  fun handleServiceException(ex: ServiceException): ResponseEntity<FailureResponse> {
    val failureResponse = FailureResponse(
      status = ex.status,
      userMessage = ex.userMessage,
      technicalMessage = ex.technicalMessage,
      severity = ex.severity
    )
    return ResponseEntity.status(ex.status).body(failureResponse)
  }

  @ExceptionHandler(Exception::class)
  fun handleGenericException(ex: Exception): ResponseEntity<FailureResponse> {
    val failureResponse = FailureResponse(
      status = HttpStatus.INTERNAL_SERVER_ERROR,
      userMessage = "An unexpected error occurred. Please try again later.",
      technicalMessage = ex.message ?: "No technical details available.",
      severity = SeverityLevel.ERROR
    )
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(failureResponse)
  }
}