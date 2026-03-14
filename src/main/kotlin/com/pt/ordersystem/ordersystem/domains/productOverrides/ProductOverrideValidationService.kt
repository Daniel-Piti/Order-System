package com.pt.ordersystem.ordersystem.domains.productOverrides

import com.pt.ordersystem.ordersystem.domains.customer.CustomerRepository
import com.pt.ordersystem.ordersystem.domains.product.ProductRepository
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.CreateProductOverrideRequest
import com.pt.ordersystem.ordersystem.domains.productOverrides.models.ProductOverrideFailureReason
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ProductOverrideValidationService(
    private val productOverrideRepository: ProductOverrideRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
){
    fun validateCreateProductOverride(
        managerId: String,
        agentId: String?,
        request: CreateProductOverrideRequest,
    ) {
        // Manager (agentId=null): customer must belong to manager (any of his or his agents' customers).
        // Agent (agentId!=null): customer must belong to that agent only.
        if (agentId == null) {
            customerRepository.findByManagerIdAndId(managerId, request.customerId)
        } else {
            customerRepository.findByManagerIdAndAgentIdAndId(managerId, agentId, request.customerId)
        }

        val product = productRepository.findByManagerIdAndId(managerId, request.productId)

        if (request.overridePrice < product.minimumPrice) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = ProductOverrideFailureReason.BELOW_MINIMUM_PRICE.message,
                technicalMessage = "Override price ${request.overridePrice} below minimum price ${product.minimumPrice} for product ${request.productId} manager $managerId agentId=$agentId",
                severity = SeverityLevel.WARN
            )
        }

        productOverrideRepository.validateOverrideNotExists(
            managerId = managerId,
            agentId = agentId,
            productId = request.productId,
            customerId = request.customerId,
        )
    }
}
