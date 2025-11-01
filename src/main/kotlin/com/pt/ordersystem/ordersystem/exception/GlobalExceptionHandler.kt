package com.pt.ordersystem.ordersystem.exception

import com.pt.ordersystem.ordersystem.config.ConfigProvider
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

@RestControllerAdvice
class GlobalExceptionHandler(
  private val configProvider: ConfigProvider
) {

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

  @ExceptionHandler(MaxUploadSizeExceededException::class)
  fun handleMaxUploadSizeException(ex: MaxUploadSizeExceededException): ResponseEntity<FailureResponse> {
    val failureResponse = FailureResponse(
      status = HttpStatus.PAYLOAD_TOO_LARGE,
      userMessage = "File size exceeds the maximum allowed limit of ${configProvider.maxUploadFileSizeMb}MB",
      technicalMessage = ex.message ?: "File upload size limit exceeded",
      severity = SeverityLevel.WARN
    )
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(failureResponse)
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