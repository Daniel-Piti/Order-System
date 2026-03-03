package com.pt.ordersystem.ordersystem.domains.productImage.helpers

import com.pt.ordersystem.ordersystem.exception.ServiceException
import com.pt.ordersystem.ordersystem.exception.SeverityLevel
import org.springframework.http.HttpStatus

object ProductImageValidators {

    private const val MAX_IMAGES_PER_PRODUCT = 5

    fun validateMaxImagesForProduct(imagesCount: Int) {
        if (imagesCount > MAX_IMAGES_PER_PRODUCT) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                userMessage = "Maximum $MAX_IMAGES_PER_PRODUCT images allowed per product",
                technicalMessage = "Attempting to add=$imagesCount, max allowed=$MAX_IMAGES_PER_PRODUCT",
                severity = SeverityLevel.WARN
            )
        }
    }

}