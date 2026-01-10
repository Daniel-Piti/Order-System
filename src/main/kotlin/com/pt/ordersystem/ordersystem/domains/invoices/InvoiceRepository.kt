package com.pt.ordersystem.ordersystem.domains.invoices

import com.pt.ordersystem.ordersystem.domains.invoices.models.InvoiceDbEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface InvoiceRepository : JpaRepository<InvoiceDbEntity, Long> {
  fun findByManagerId(managerId: String): List<InvoiceDbEntity>
  fun findByOrderId(orderId: String): InvoiceDbEntity?
  
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT COALESCE(MAX(i.invoiceSequenceNumber), 0) FROM InvoiceDbEntity i WHERE i.managerId = :managerId")
  fun findMaxSequenceNumberByManagerId(@Param("managerId") managerId: String): Int?
}

