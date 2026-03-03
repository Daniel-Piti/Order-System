package com.pt.ordersystem.ordersystem.domains.product.helpers

import com.pt.ordersystem.ordersystem.domains.product.models.ProductInfo
import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import org.springframework.http.HttpStatus
import java.math.BigDecimal

object ProductValidators {

    private const val MAXIMUM_PRODUCTS_FOR_MANAGER = 1000L

    fun validateProductInfo(productInfo: ProductInfo) {
        with(productInfo) {
            FieldValidators.validateNonEmpty(name, "'name'")
            FieldValidators.validatePriceRange(minimumPrice)
            FieldValidators.validatePriceRange(price)
        }
    }

    fun validatePriceHigherOrEqualToMinPrice(price: BigDecimal, minimumPrice: BigDecimal) {
        if (price < minimumPrice) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = "Price cannot be lower than minimum price",
                technicalMessage = "price=$price < minimumPrice=$minimumPrice",
                severity = SeverityLevel.WARN
            )
        }
    }

    fun validateMaxProductPerCustomer(productCount: Long, managerId: String) {
        if (productCount >= MAXIMUM_PRODUCTS_FOR_MANAGER) {
            throw ServiceException(
                status = HttpStatus.CONFLICT,
                userMessage = "You have reached the product limit ($MAXIMUM_PRODUCTS_FOR_MANAGER)",
                technicalMessage = "Manager ($managerId) has reached $MAXIMUM_PRODUCTS_FOR_MANAGER products",
                severity = SeverityLevel.WARN
            )
        }
    }
}