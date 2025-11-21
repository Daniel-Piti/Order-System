package com.pt.ordersystem.ordersystem.notification.email

import com.pt.ordersystem.ordersystem.domains.order.OrderService
import com.pt.ordersystem.ordersystem.notification.email.emails.OrderCancelledEmail
import com.pt.ordersystem.ordersystem.notification.email.emails.OrderDoneEmail
import com.pt.ordersystem.ordersystem.notification.email.emails.OrderPlacedEmail
import com.pt.ordersystem.ordersystem.notification.email.emails.OrderUpdatedEmail
import com.pt.ordersystem.ordersystem.notification.email.models.EmailOrderCancelledNotificationRequest
import com.pt.ordersystem.ordersystem.notification.email.models.EmailOrderDoneNotificationRequest
import com.pt.ordersystem.ordersystem.notification.email.models.EmailOrderPlacedNotificationRequest
import com.pt.ordersystem.ordersystem.notification.email.models.EmailOrderUpdatedNotificationRequest
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

/**
 * Service for sending email notifications.
 * 
 * To create a new email:
 * 1. Create an object in emails/ with buildSubject(data) and buildHtml(data) methods
 * 2. Add HTML template in resources/templates/notification/
 * 3. Add service method here that calls: emailObject.validate(data), emailObject.buildSubject(data), emailObject.buildHtml(data), sendEmail(...)
 */
@Service
class EmailNotificationService(
  private val orderService: OrderService,
  private val mailSender: JavaMailSender
) {
  @Value("\${spring.mail.from-email}")
  private lateinit var fromEmail: String

  private val logger = LoggerFactory.getLogger(EmailNotificationService::class.java)

  fun sendOrderPlacedNotification(request: EmailOrderPlacedNotificationRequest) {
    val order = orderService.getOrderByIdInternal(request.orderId)
    OrderPlacedEmail.validate(order)
    val subject = OrderPlacedEmail.buildSubject(order)
    val htmlBody = OrderPlacedEmail.buildHtml(order)
    sendEmail(subject, htmlBody, request.recipientEmail)
  }

  fun sendOrderDoneNotification(request: EmailOrderDoneNotificationRequest) {
    val order = orderService.getOrderByIdInternal(request.orderId)
    OrderDoneEmail.validate(order)
    val subject = OrderDoneEmail.buildSubject(order)
    val htmlBody = OrderDoneEmail.buildHtml(order)
    sendEmail(subject, htmlBody, request.recipientEmail)
  }

  fun sendOrderCancelledNotification(request: EmailOrderCancelledNotificationRequest) {
    val order = orderService.getOrderByIdInternal(request.orderId)
    OrderCancelledEmail.validate(order)
    val subject = OrderCancelledEmail.buildSubject(order)
    val htmlBody = OrderCancelledEmail.buildHtml(order)
    sendEmail(subject, htmlBody, request.recipientEmail)
  }

  fun sendOrderUpdatedNotification(request: EmailOrderUpdatedNotificationRequest) {
    val order = orderService.getOrderByIdInternal(request.orderId)
    OrderUpdatedEmail.validate(order)
    val subject = OrderUpdatedEmail.buildSubject(order)
    val htmlBody = OrderUpdatedEmail.buildHtml(order)
    sendEmail(subject, htmlBody, request.recipientEmail)
  }

  /**
   * Sends an email with the provided subject, HTML body, and recipient.
   * This is the most generic email sending function.
   */
  private fun sendEmail(subject: String, htmlBody: String, to: String) {
    try {
      val message: MimeMessage = mailSender.createMimeMessage()
      val helper = MimeMessageHelper(message, true, "UTF-8")

      helper.setTo(to)
      helper.setSubject(subject)
      helper.setFrom(fromEmail)
      helper.setText(htmlBody, true) // true = HTML

      mailSender.send(message)

      logger.info("Email sent | to=$to | subject=$subject")
    } catch (e: Exception) {
      logger.error("Failed to send email | to=$to | subject=$subject | error=$e")
      throw e
    }
  }

}
