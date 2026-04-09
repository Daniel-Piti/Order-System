package com.pt.ordersystem.ordersystem.domains.invoices

import com.pt.ordersystem.ordersystem.domains.invoices.models.Invoice
import com.pt.ordersystem.ordersystem.domains.invoices.models.InvoiceDbEntity
import com.pt.ordersystem.ordersystem.domains.invoices.models.toModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
class InvoiceRepository(
  private val invoiceDao: InvoiceDao,
) {
  fun findByManagerId(managerId: String): List<Invoice> =
    invoiceDao.findByManagerId(managerId).map { it.toModel() }

  fun existsByOrderIdAndInvoiceType(orderId: String, invoiceType: String): Boolean =
    invoiceDao.existsByOrderIdAndInvoiceType(orderId, invoiceType)

  fun findByIdAndManagerId(id: Long, managerId: String): Invoice? =
    invoiceDao.findByIdAndManagerId(id, managerId)?.toModel()

  fun sumTotalAmountByOrderIdAndInvoiceType(orderId: String, invoiceType: String): BigDecimal =
    invoiceDao.sumTotalAmountByOrderIdAndInvoiceType(orderId, invoiceType)

  fun findByOrderIdInAndManagerId(orderIds: List<String>, managerId: String): List<Invoice> =
    invoiceDao.findByOrderIdInAndManagerId(orderIds, managerId).map { it.toModel() }

  fun findByManagerIdAndCreatedAtBetween(
    managerId: String,
    from: LocalDateTime,
    to: LocalDateTime,
  ): List<Invoice> =
    invoiceDao.findByManagerIdAndCreatedAtBetween(managerId, from, to).map { it.toModel() }

  fun searchInvoicesForManager(
    managerId: String,
    from: LocalDateTime,
    to: LocalDateTime,
    customerId: String?,
    pageable: Pageable,
  ): Page<Invoice> =
    invoiceDao.searchInvoicesForManager(
      managerId = managerId,
      from = from,
      to = to,
      customerId = customerId,
      pageable = pageable,
    ).map { it.toModel() }

  fun findMaxSequenceNumberByManagerIdAndInvoiceType(managerId: String, invoiceType: String): Int =
    invoiceDao.findMaxSequenceNumberByManagerIdAndInvoiceType(managerId, invoiceType)

  fun save(entity: InvoiceDbEntity): Invoice = invoiceDao.save(entity).toModel()
}

