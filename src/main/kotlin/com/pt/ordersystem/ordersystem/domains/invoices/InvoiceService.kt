package com.pt.ordersystem.ordersystem.domains.invoices

import com.pt.ordersystem.ordersystem.domains.business.BusinessService
import com.pt.ordersystem.ordersystem.domains.invoices.helpers.InvoiceAggregatorHelper
import com.pt.ordersystem.ordersystem.domains.invoices.helpers.InvoiceHelper
import com.pt.ordersystem.ordersystem.domains.invoices.helpers.InvoiceRenderHelper
import com.pt.ordersystem.ordersystem.domains.invoices.models.CreateCreditNoteByAmountResponse
import com.pt.ordersystem.ordersystem.domains.invoices.models.CreateInvoiceRequest
import com.pt.ordersystem.ordersystem.domains.invoices.signing.InvoicePdfSigner
import com.pt.ordersystem.ordersystem.domains.invoices.models.CreateInvoiceResponse
import com.pt.ordersystem.ordersystem.domains.invoices.models.InvoiceDbEntity
import com.pt.ordersystem.ordersystem.domains.invoices.models.Invoice
import com.pt.ordersystem.ordersystem.domains.invoices.models.InvoiceType
import com.pt.ordersystem.ordersystem.domains.invoices.models.PaymentMethod
import com.pt.ordersystem.ordersystem.domains.manager.ManagerRepository
import com.pt.ordersystem.ordersystem.domains.order.OrderRepository
import com.pt.ordersystem.ordersystem.domains.order.models.Order
import com.pt.ordersystem.ordersystem.domains.order.models.toEntity
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.storage.S3StorageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class InvoiceService(
  private val orderRepository: OrderRepository,
  private val invoiceRepository: InvoiceRepository,
  private val s3StorageService: S3StorageService,
  private val managerRepository: ManagerRepository,
  private val businessService: BusinessService,
  private val invoicePdfSigner: InvoicePdfSigner,
) {

  companion object {
    private const val MAX_BATCH_SIZE = 100
  }

  private data class UploadedInvoiceFile(
    val fileName: String,
    val s3Key: String,
    val fileSizeBytes: Long,
    val pdfUrl: String,
  )

  @Transactional
  fun createInvoice(createInvoiceRequest: CreateInvoiceRequest): CreateInvoiceResponse {
    // Validate order exists and belongs to manager (repository throws if not found)
    val order = orderRepository.findByIdAndManagerIdAndAgentId(createInvoiceRequest.orderId, createInvoiceRequest.managerId, null)
    validateInvoiceRequest(order, createInvoiceRequest)

    val allocationNumber = InvoiceHelper.validateAndPrepareAllocationNumber(order.toEntity(), createInvoiceRequest.allocationNumber)

    // Generate next sequence number per manager + invoice type (separate counters for INVOICE/CREDIT_NOTE)
    // Using SELECT FOR UPDATE through transaction isolation to prevent race conditions
    val maxSequence = invoiceRepository.findMaxSequenceNumberByManagerIdAndInvoiceType(
      createInvoiceRequest.managerId,
      InvoiceType.INVOICE.name,
    )
    val invoiceSequenceNumber = maxSequence + 1

    val manager = managerRepository.findById(order.managerId)
    val business = businessService.getBusinessByManagerId(order.managerId)

    val unsignedPdfBytes = InvoiceRenderHelper.renderPdf(
      business = business,
      order = order.toEntity(),
      invoiceSequenceNumber = invoiceSequenceNumber,
      paymentMethod = createInvoiceRequest.paymentMethod,
      paymentProof = createInvoiceRequest.paymentProof,
      allocationNumber = allocationNumber
    )
    val pdfBytes = invoicePdfSigner.sign(unsignedPdfBytes, business.name)

    val uploadedFile = uploadInvoicePdf(
      managerId = manager.id,
      invoiceSequenceNumber = invoiceSequenceNumber,
      pdfBytes = pdfBytes,
    )

    val now = LocalDateTime.now()
    val invoice = InvoiceDbEntity(
      managerId = manager.id,
      orderId = order.id,
      customerId = order.customerId,
      totalAmount = order.totalPrice,
      invoiceType = InvoiceType.INVOICE.name,
      invoiceSequenceNumber = invoiceSequenceNumber,
      paymentMethod = createInvoiceRequest.paymentMethod.name,
      paymentProof = createInvoiceRequest.paymentProof,
      allocationNumber = allocationNumber,
      s3Key = uploadedFile.s3Key,
      fileName = uploadedFile.fileName,
      fileSizeBytes = uploadedFile.fileSizeBytes,
      mimeType = "application/pdf",
      createdAt = now,
      updatedAt = now,
    )

    val savedInvoice = invoiceRepository.save(invoice)
    return CreateInvoiceResponse(
      invoiceId = savedInvoice.id,
      invoiceName = uploadedFile.fileName,
      pdfUrl = uploadedFile.pdfUrl,
    )
  }

  @Transactional
  fun createCreditNoteByAmount(
    managerId: String,
    invoiceId: Long,
    amount: BigDecimal,
    allocationNumber: String?,
  ): CreateCreditNoteByAmountResponse {
    val primaryInvoice = invoiceRepository.findByIdAndManagerId(invoiceId, managerId)
      ?: throw ServiceException(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Invoice not found",
        technicalMessage = "createCreditNoteByAmount: no invoice id=$invoiceId for manager $managerId",
        severity = SeverityLevel.INFO,
      )

    if (primaryInvoice.invoiceType != InvoiceType.INVOICE) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Credit note must reference the original sales invoice",
        technicalMessage = "createCreditNoteByAmount: invoice ${primaryInvoice.id} has type ${primaryInvoice.invoiceType}",
        severity = SeverityLevel.INFO,
      )
    }

    val order = orderRepository.findByIdAndManagerIdAndAgentId(primaryInvoice.orderId, managerId, null)
    InvoiceHelper.validateOrderEligibilityForInvoice(order.toEntity())

    InvoiceHelper.validateCreditNoteAllocationNumber(
      primaryInvoice.allocationNumber,
      allocationNumber,
    )
    val creditAllocationNumber = allocationNumber?.trim()?.takeIf { it.isNotEmpty() }

    if (amount.scale() > 2) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Credit amount must have at most two decimal places",
        technicalMessage = "createCreditNoteByAmount: amount scale ${amount.scale()} for invoice $invoiceId",
        severity = SeverityLevel.WARN,
      )
    }
    if (amount <= BigDecimal.ZERO) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Credit amount must be greater than zero",
        technicalMessage = "createCreditNoteByAmount: non-positive amount for invoice $invoiceId",
        severity = SeverityLevel.WARN,
      )
    }

    val creditAmount = amount.setScale(2, RoundingMode.HALF_UP)

    val creditedSoFar = invoiceRepository.sumTotalAmountByOrderIdAndInvoiceType(
      order.id,
      InvoiceType.CREDIT_NOTE.name,
    )
    val cap = minOf(order.totalPrice, primaryInvoice.totalAmount)
    val newTotalCredited = creditedSoFar.add(creditAmount)
    if (newTotalCredited > cap) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Credit amount exceeds remaining refundable total for this order",
        technicalMessage = "createCreditNoteByAmount: creditedSoFar=$creditedSoFar + amount=$creditAmount > cap=$cap for order ${order.id}",
        severity = SeverityLevel.INFO,
      )
    }

    val maxSequence = invoiceRepository.findMaxSequenceNumberByManagerIdAndInvoiceType(
      managerId,
      InvoiceType.CREDIT_NOTE.name,
    )
    val creditSequenceNumber = maxSequence + 1

    val now = LocalDateTime.now()
    val creditEntity = InvoiceDbEntity(
      managerId = order.managerId,
      orderId = order.id,
      customerId = order.customerId,
      totalAmount = creditAmount,
      invoiceType = InvoiceType.CREDIT_NOTE.name,
      linkedInvoiceId = primaryInvoice.id,
      invoiceSequenceNumber = creditSequenceNumber,
      paymentMethod = PaymentMethod.CASH.name,
      paymentProof = "CREDIT_NOTE",
      allocationNumber = creditAllocationNumber,
      s3Key = null,
      fileName = null,
      fileSizeBytes = null,
      mimeType = null,
      createdAt = now,
      updatedAt = now,
    )

    val saved = invoiceRepository.save(creditEntity)
    orderRepository.addTotalCreditedAmount(order.id, creditAmount)

    return CreateCreditNoteByAmountResponse(
      invoiceId = saved.id,
      invoiceSequenceNumber = saved.invoiceSequenceNumber,
    )
  }

  private fun uploadInvoicePdf(
    managerId: String,
    invoiceSequenceNumber: Int,
    pdfBytes: ByteArray,
  ): UploadedInvoiceFile {
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

    return UploadedInvoiceFile(
      fileName = fileName,
      s3Key = s3Key,
      fileSizeBytes = pdfBytes.size.toLong(),
      pdfUrl = pdfUrl,
    )
  }

  private fun validateInvoiceRequest(order: Order, createInvoiceRequest: CreateInvoiceRequest) {
    InvoiceHelper.validateOrderEligibilityForInvoice(order.toEntity())

    // At most one regular invoice per order (credit notes share order_id and use CREDIT_NOTE type)
    if (invoiceRepository.existsByOrderIdAndInvoiceType(order.id, InvoiceType.INVOICE.name)) {
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
  fun getInvoicesByOrderIds(managerId: String, orderIds: List<String>): Map<String, List<Invoice>> {
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

    val invoices = invoiceRepository.findByOrderIdInAndManagerId(orderIds, managerId)
    val byOrderId = invoices.groupBy { it.orderId }
    return orderIds.distinct().associateWith { orderId -> byOrderId[orderId] ?: emptyList() }
  }

  @Transactional(readOnly = true)
  fun getInvoiceLinksDocument(managerId: String, fromDate: LocalDate, toDate: LocalDate): ByteArray {
    if (toDate.isBefore(fromDate)) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Date range invalid: 'to' must be on or after 'from'",
        technicalMessage = "getInvoiceLinksDocument: toDate=$toDate is before fromDate=$fromDate",
        severity = SeverityLevel.WARN
      )
    }
    val from = fromDate.atStartOfDay()
    val to = toDate.atTime(LocalTime.MAX)
    val invoices = invoiceRepository.findByManagerIdAndCreatedAtBetween(managerId, from, to)
    if (invoices.isEmpty()) {
      return InvoiceAggregatorHelper.buildInvoiceLinksXlsx(fromDate, toDate, emptyList(), BigDecimal.ZERO)
    }
    val entries = invoices.mapNotNull { invoice ->
      val url = s3StorageService.getPublicUrl(invoice.s3Key) ?: return@mapNotNull null
      val displayName = invoice.fileName ?: "invoice-${invoice.invoiceSequenceNumber}.pdf"
      InvoiceAggregatorHelper.InvoiceLinkEntry(invoice.orderId, url, displayName, invoice.totalAmount)
    }
    val totalAmount = entries.fold(BigDecimal.ZERO) { acc, e -> acc.add(e.totalAmount) }
    return InvoiceAggregatorHelper.buildInvoiceLinksXlsx(fromDate, toDate, entries, totalAmount)
  }

  @Transactional(readOnly = true)
  fun searchInvoices(
    managerId: String,
    fromDate: LocalDate,
    toDate: LocalDate,
    customerId: String?,
    validatedPageParams: PageRequest,
  ): Page<Invoice> {
    if (toDate.isBefore(fromDate)) {
      throw ServiceException(
        status = HttpStatus.BAD_REQUEST,
        userMessage = "Date range invalid: 'to' must be on or after 'from'",
        technicalMessage = "searchInvoices: toDate=$toDate is before fromDate=$fromDate",
        severity = SeverityLevel.WARN
      )
    }

    val from = fromDate.atStartOfDay()
    val to = toDate.atTime(LocalTime.MAX)

    val invoicesPage = invoiceRepository.searchInvoicesForManager(
      managerId = managerId,
      from = from,
      to = to,
      customerId = customerId,
      pageable = validatedPageParams,
    )

    return invoicesPage
  }

}