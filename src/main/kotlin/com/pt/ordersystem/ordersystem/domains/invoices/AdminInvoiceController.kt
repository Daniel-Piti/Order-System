package com.pt.ordersystem.ordersystem.domains.invoices

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_ADMIN
import com.pt.ordersystem.ordersystem.domains.invoices.models.CreateInvoiceRequest
import com.pt.ordersystem.ordersystem.domains.invoices.models.CreateInvoiceResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin Invoices", description = "Admin-only access to invoice management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/invoices")
@PreAuthorize(AUTH_ADMIN)
class AdminInvoiceController(
  private val invoiceService: InvoiceService
) {

  @PostMapping
  fun createInvoice(
    @RequestBody createInvoiceRequest: CreateInvoiceRequest
  ): ResponseEntity<CreateInvoiceResponse> {
    val response = invoiceService.createInvoice(createInvoiceRequest)
    return ResponseEntity.ok(response)
  }

  @GetMapping("/order/{orderId}")
  fun getInvoiceByOrderId(
    @PathVariable orderId: String
  ): ResponseEntity<Map<String, String>> {
    val invoiceUrl = invoiceService.getInvoiceByOrderId(orderId)
    return ResponseEntity.ok(mapOf("url" to invoiceUrl))
  }
}

