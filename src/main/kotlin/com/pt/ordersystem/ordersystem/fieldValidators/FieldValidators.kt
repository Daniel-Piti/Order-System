package com.pt.ordersystem.ordersystem.fieldValidators

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDate

object FieldValidators {

  private val MAX_PRICE = BigDecimal("1000000")

  fun validateEmail(email: String) {
    val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    if (!emailPattern.matches(email)) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Invalid email format",
        technicalMessage = "Email `$email` failed regex validation",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun validateStrongPassword(password: String) {
    val lengthCheck = password.length >= 8
    val upperCheck = password.any { it.isUpperCase() }
    val lowerCheck = password.any { it.isLowerCase() }
    val digitCheck = password.any { it.isDigit() }
    val specialCheck = password.any { !it.isLetterOrDigit() }

    if (!(lengthCheck && upperCheck && lowerCheck && digitCheck && specialCheck)) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Password does not meet security requirements",
        technicalMessage = "Password must contain at least 8 characters, 1 uppercase, 1 lowercase, 1 digit, and 1 special character",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun validatePhoneNumber(phone: String) {
    if (!phone.matches("^[0-9]{8,10}$".toRegex())) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Phone number must contain only 8-10 digits (no dashes or special characters)",
        technicalMessage = "Phone number `$phone` contains invalid characters or wrong length",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun validateNumericString(value: String, length: Int, fieldName: String) {
    if (!value.matches("^\\d{$length}$".toRegex())) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "$fieldName must be exactly $length digits",
        technicalMessage = "$fieldName `$value` must be exactly $length digits, got ${value.length}",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun validateNonEmpty(value: String, fieldName: String) {
    if (value.isBlank()) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "$fieldName cannot be blank",
        technicalMessage = "$fieldName was blank or empty",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun validateDateNotFuture(date: LocalDate, fieldName: String = "Date") {
    if (date.isAfter(LocalDate.now())) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "$fieldName cannot be in the future",
        technicalMessage = "$fieldName `$date` is after today",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun validateNonNegative(value: BigDecimal, fieldName: String = "Value") {
    if (value < BigDecimal.ZERO) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "$fieldName must be greater than or equal to 0",
        technicalMessage = "$fieldName `$value` is negative",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun validatePriceDecimalPlaces(value: BigDecimal, fieldName: String = "Value") {
    if (value.scale() > 2) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "$fieldName can have at most 2 decimal places",
        technicalMessage = "$fieldName `$value` has ${value.scale()} decimal places, maximum allowed is 2",
        severity = SeverityLevel.WARN
      )
    }
  }

  fun validatePrice(price: BigDecimal) {
    validateNonNegative(price, "Price")

    if (price > MAX_PRICE) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Price cannot exceed 1,000,000",
        technicalMessage = "Price `$price` is greater than allowed maximum $MAX_PRICE",
        severity = SeverityLevel.WARN
      )
    }

    validatePriceDecimalPlaces(price, "Price")
  }

  fun validateNewPasswordEqualConfirmationPassword(newPassword: String, newPasswordConfirmation: String) {
    if (newPassword != newPasswordConfirmation)
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "New password and confirmation do not match",
        technicalMessage = "Mismatch between passwords",
        severity = SeverityLevel.INFO
      )
  }

  fun validateNewPasswordNotEqualOldPassword(oldPassword: String, newPassword: String) {
    if (oldPassword == newPassword) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "New password cannot be the same as the old password",
        technicalMessage = "New password = old password",
        severity = SeverityLevel.INFO
      )
    }
  }

}