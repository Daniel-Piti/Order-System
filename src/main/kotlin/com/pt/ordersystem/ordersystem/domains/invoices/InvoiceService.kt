package com.pt.ordersystem.ordersystem.domains.invoices

import com.pt.ordersystem.ordersystem.domains.business.BusinessService
import com.pt.ordersystem.ordersystem.domains.invoices.helpers.InvoiceHelper
import com.pt.ordersystem.ordersystem.domains.invoices.helpers.InvoiceRenderHelper
import com.pt.ordersystem.ordersystem.domains.invoices.models.CreateInvoiceRequest
import com.pt.ordersystem.ordersystem.domains.invoices.models.CreateInvoiceResponse
import com.pt.ordersystem.ordersystem.domains.invoices.models.InvoiceDbEntity
import com.pt.ordersystem.ordersystem.domains.manager.ManagerRepository
import com.pt.ordersystem.ordersystem.domains.order.OrderRepository
import com.pt.ordersystem.ordersystem.domains.order.models.OrderDbEntity
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import org.springframework.transaction.annotation.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class InvoiceService(
  private val orderRepository: OrderRepository,
  private val invoiceRepository: InvoiceRepository,
  private val s3StorageService: S3StorageService,
  private val managerRepository: ManagerRepository,
  private val businessService: BusinessService,
) {

  companion object {
    private const val MAX_BATCH_SIZE = 100
  }

  @Transactional
  fun createInvoice(createInvoiceRequest: CreateInvoiceRequest): CreateInvoiceResponse {
    // Validate order exists and belongs to manager
    val order = orderRepository.findByIdAndManagerId(createInvoiceRequest.orderId, createInvoiceRequest.managerId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Order not found",
        technicalMessage = "Order ${createInvoiceRequest.orderId} not found for manager ${createInvoiceRequest.managerId}",
        severity = SeverityLevel.WARN
      )

    validateInvoiceRequest(order, createInvoiceRequest)

    // Validate allocation number if required
    val allocationNumber = InvoiceHelper.validateAndPrepareAllocationNumber(order, createInvoiceRequest.allocationNumber)

    // Generate next invoice sequence number for manager (with proper locking for race condition)
    // Using SELECT FOR UPDATE through transaction isolation to prevent race conditions
    val maxSequence = invoiceRepository.findMaxSequenceNumberByManagerId(createInvoiceRequest.managerId) ?: 0
    val invoiceSequenceNumber = maxSequence + 1

    val manager = managerRepository.findById(order.managerId)
    val business = businessService.getBusinessByManagerId(order.managerId)

    // Generate PDF (invoiceNumber is formatted inside renderPdf)
    val pdfBytes = InvoiceRenderHelper.renderPdf(
      business = business,
      order = order,
      invoiceSequenceNumber = invoiceSequenceNumber,
      paymentMethod = createInvoiceRequest.paymentMethod,
      paymentProof = createInvoiceRequest.paymentProof,
      allocationNumber = allocationNumber
    )

    val uploadData = uploadInvoice(
      manager.id,
      order.id,
      invoiceSequenceNumber,
      createInvoiceRequest.paymentMethod.name,
      createInvoiceRequest.paymentProof,
      allocationNumber,
      pdfBytes,
    )

    return uploadData
  }

  private fun uploadInvoice(
    managerId: String,
    orderId: String,
    invoiceSequenceNumber: Int,
    paymentMethod: String,
    paymentProof: String,
    allocationNumber: String?,
    pdfBytes: ByteArray
  ): CreateInvoiceResponse {
    // Build S3 key using generateS3Key (includes UUID prefix for uniqueness and sanitization)
    val fileName = "invoice-$invoiceSequenceNumber.pdf"
    val s3Key = s3StorageService.generateS3Key("managers/$managerId/invoices", fileName)

    // Upload PDF to S3 first (if this fails, transaction will rollback)
    s3StorageService.uploadFile(s3Key, pdfBytes, "application/pdf")

    // Get the public URL for the uploaded file
    val pdfUrl = s3StorageService.getPublicUrl(s3Key)
      ?: throw ServiceException(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        userMessage = "Unable to generate invoice URL",
        technicalMessage = "S3 URL is null after upload for s3Key: $s3Key",
        severity = SeverityLevel.ERROR
      )

    // Save invoice record to database (within transaction - if save fails, S3 file will be orphaned but that's acceptable)
    val now = LocalDateTime.now()
    val invoice = InvoiceDbEntity(
      managerId = managerId,
      orderId = orderId,
      invoiceSequenceNumber = invoiceSequenceNumber,
      paymentMethod = paymentMethod,
      paymentProof = paymentProof,
      allocationNumber = allocationNumber,
      s3Key = s3Key,
      fileName = fileName,
      fileSizeBytes = pdfBytes.size.toLong(),
      mimeType = "application/pdf",
      createdAt = now,
      updatedAt = now
    )

    val savedInvoice = invoiceRepository.save(invoice)

    return CreateInvoiceResponse(
      invoiceId = savedInvoice.id,
      invoiceName = fileName,
      pdfUrl = pdfUrl
    )
  }

  private fun validateInvoiceRequest(order: OrderDbEntity, createInvoiceRequest: CreateInvoiceRequest) {
    InvoiceHelper.validateOrderEligibilityForInvoice(order)

    // Check if invoice already exists for this order (UNIQUE constraint will also catch this, but better to fail fast)
    if (invoiceRepository.findByOrderId(order.id) != null) {
      throw ServiceException(
        status = HttpStatus.CONFLICT,
        userMessage = "Invoice already exists for this order",
        technicalMessage = "Invoice already exists for order ${order.id}",
        severity = SeverityLevel.INFO
      )
    }

    InvoiceHelper.validatePaymentMethodAndProof(createInvoiceRequest.paymentMethod, createInvoiceRequest.paymentProof)
  }

  @Transactional(readOnly = true)
  fun getInvoicesByOrderIds(managerId: String, orderIds: List<String>): Map<String, String> {
    if (orderIds.isEmpty()) return emptyMap()

    // Limit batch size to prevent performance issues
    if (orderIds.size > MAX_BATCH_SIZE) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Maximum of $MAX_BATCH_SIZE order IDs allowed per request",
        technicalMessage = "Request contains ${orderIds.size} order IDs, maximum allowed is $MAX_BATCH_SIZE",
        severity = SeverityLevel.WARN
      )
    }

    // Find all invoices for the given order IDs that belong to the manager
    val invoices = invoiceRepository.findByOrderIdInAndManagerId(orderIds, managerId)
    
    // Build the map from invoices to S3 URLs, filtering out nulls in a single pass
    return invoices.mapNotNull { invoice ->
        s3StorageService.getPublicUrl(invoice.s3Key)?.let { invoice.orderId to it }
      }.toMap()
  }

}