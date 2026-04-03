package com.pt.ordersystem.ordersystem.domains.invoices

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.invoices.models.CreateInvoiceRequest
import com.pt.ordersystem.ordersystem.domains.invoices.models.CreateInvoiceResponse
import com.pt.ordersystem.ordersystem.domains.invoices.models.InvoiceDto
import com.pt.ordersystem.ordersystem.domains.invoices.models.ManagerCreateInvoiceRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import com.pt.ordersystem.ordersystem.utils.PageRequestBaseExternal
import com.pt.ordersystem.ordersystem.utils.toValidatedPageRequest
import java.time.LocalDate

@Tag(name = "Invoices", description = "Manager invoice management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/invoices")
@PreAuthorize(AUTH_MANAGER)
class ManagerInvoiceController(
  private val invoiceService: InvoiceService
) {

  @PostMapping
  fun createInvoice(
    @AuthenticationPrincipal manager: AuthUser,
    @RequestBody createInvoiceRequest: ManagerCreateInvoiceRequest
  ): ResponseEntity<CreateInvoiceResponse> {
    val request = CreateInvoiceRequest(
      managerId = manager.id,
      orderId = createInvoiceRequest.orderId,
      paymentMethod = createInvoiceRequest.paymentMethod,
      paymentProof = createInvoiceRequest.paymentProof,
      allocationNumber = createInvoiceRequest.allocationNumber
    )
    val response = invoiceService.createInvoice(request)
    return ResponseEntity.ok(response)
  }

  @PostMapping("/by-order-ids")
  fun getInvoicesByOrderIds(
    @AuthenticationPrincipal manager: AuthUser,
    @RequestBody orderIds: List<String>
  ): ResponseEntity<Map<String, String>> {
    val invoiceMap = invoiceService.getInvoicesByOrderIds(manager.id, orderIds)
    return ResponseEntity.ok(invoiceMap)
  }

  /**
   * Returns an Excel (.xlsx) file with clickable invoice links (e.g. "invoice-1.pdf") and order totals
   * for the given date range (by invoice creation date).
   * Query params: from (yyyy-MM-dd), to (yyyy-MM-dd).
   */
  @GetMapping(value = ["/document"], produces = ["application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"])
  fun getInvoiceLinksDocument(
    @AuthenticationPrincipal manager: AuthUser,
    @RequestParam from: String,
    @RequestParam to: String
  ): ResponseEntity<ByteArray> {
    val fromDate = LocalDate.parse(from)
    val toDate = LocalDate.parse(to)
    val xlsx = invoiceService.getInvoiceLinksDocument(manager.id, fromDate, toDate)
    val filename = "invoice-links-${from}-${to}.xlsx"
    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
      .header("Content-Disposition", "attachment; filename=\"$filename\"")
      .body(xlsx)
  }

  @GetMapping("/search")
  fun searchInvoices(
    @AuthenticationPrincipal manager: AuthUser,
    @RequestParam from: String,
    @RequestParam to: String,
    @RequestParam(required = false) customerId: String?,
    pageParams: PageRequestBaseExternal,
  ): ResponseEntity<Page<InvoiceDto>> {
    val fromDate = LocalDate.parse(from)
    val toDate = LocalDate.parse(to)

    val validatedPageRequest = pageParams.toValidatedPageRequest(InvoiceListSortFields.ALLOWED)
    val page = invoiceService.searchInvoices(
      managerId = manager.id,
      fromDate = fromDate,
      toDate = toDate,
      customerId = customerId,
      validatedPageParams = validatedPageRequest,
    )
    return ResponseEntity.ok(page)
  }
}
