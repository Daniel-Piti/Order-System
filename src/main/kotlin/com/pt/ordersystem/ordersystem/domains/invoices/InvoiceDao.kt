package com.pt.ordersystem.ordersystem.domains.invoices

import com.pt.ordersystem.ordersystem.domains.invoices.models.InvoiceDbEntity
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface InvoiceDao : JpaRepository<InvoiceDbEntity, Long> {
  fun findByManagerId(managerId: String): List<InvoiceDbEntity>
  fun findByOrderIdAndInvoiceType(orderId: String, invoiceType: String): InvoiceDbEntity?
  fun findByOrderIdInAndManagerId(orderIds: List<String>, managerId: String): List<InvoiceDbEntity>
  fun findByManagerIdAndCreatedAtBetween(
    managerId: String,
    from: LocalDateTime,
    to: LocalDateTime,
  ): List<InvoiceDbEntity>

  @Query(
    """
    SELECT i FROM InvoiceDbEntity i
    WHERE i.managerId = :managerId
    AND i.createdAt >= :from AND i.createdAt <= :to
    AND (:customerId IS NULL OR i.customerId = :customerId)
    """,
  )
  fun searchInvoicesForManager(
    @Param("managerId") managerId: String,
    @Param("from") from: LocalDateTime,
    @Param("to") to: LocalDateTime,
    @Param("customerId") customerId: String?,
    pageable: Pageable,
  ): Page<InvoiceDbEntity>

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT COALESCE(MAX(i.invoiceSequenceNumber), 0) FROM InvoiceDbEntity i WHERE i.managerId = :managerId")
  fun findMaxSequenceNumberByManagerId(@Param("managerId") managerId: String): Int
}
