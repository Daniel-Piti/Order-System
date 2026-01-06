package com.pt.ordersystem.ordersystem.domains.order

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_ADMIN
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin Order Invoices", description = "Admin-only access to order invoices")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize(AUTH_ADMIN)
class AdminOrderInvoiceController(
  private val orderInvoiceService: OrderInvoiceService
) {

  @GetMapping("/{orderId}/invoice")
  fun generateInvoiceForOrder(@PathVariable orderId: String): ResponseEntity<ByteArray> {
    val document = orderInvoiceService.generateInvoiceForOrder(orderId)

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${document.fileName}\"")
      .contentType(MediaType.APPLICATION_PDF)
      .body(document.content)
  }
}

