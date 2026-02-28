package com.pt.ordersystem.ordersystem.domains.location

import com.pt.ordersystem.ordersystem.domains.location.models.Location
import com.pt.ordersystem.ordersystem.domains.location.models.LocationDbEntity
import com.pt.ordersystem.ordersystem.domains.location.models.LocationFailureReason
import com.pt.ordersystem.ordersystem.domains.location.models.toModel
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository

@Repository
class LocationRepository(
    private val locationDao: LocationDao,
) {

    fun findByManagerIdAndId(managerId: String, id: Long): Location =
        locationDao.findByManagerIdAndId(managerId, id)?.toModel() ?: throw ServiceException(
            status = HttpStatus.NOT_FOUND,
            userMessage = LocationFailureReason.NOT_FOUND.userMessage,
            technicalMessage = LocationFailureReason.NOT_FOUND.technical + "locationId=$id",
            severity = SeverityLevel.WARN,
        )

    fun findByManagerId(managerId: String): List<Location> =
        locationDao.findByManagerId(managerId).map { it.toModel() }

    fun countByManagerId(managerId: String): Long = locationDao.countByManagerId(managerId)

    fun save(entity: LocationDbEntity): Location = locationDao.save(entity).toModel()

    fun deleteById(id: Long) = locationDao.deleteById(id)

}
