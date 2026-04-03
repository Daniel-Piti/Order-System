package com.pt.ordersystem.ordersystem.domains.invoices

import com.pt.ordersystem.ordersystem.domains.invoices.models.InvoiceDbEntity
import com.pt.ordersystem.ordersystem.utils.PageRequestBaseExternal

/**
 * Allowed [PageRequestBaseExternal.sortBy] values for invoice search pagination (JPA field on [InvoiceDbEntity]).
 */
object InvoiceListSortFields {
  val ALLOWED: Set<String> = setOf("createdAt")
}
