package com.pt.ordersystem.ordersystem.domains.invoices

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.invoices.models.CreateInvoiceRequest
import com.pt.ordersystem.ordersystem.domains.invoices.models.CreateInvoiceResponse
import com.pt.ordersystem.ordersystem.domains.invoices.models.ManagerCreateInvoiceRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}
