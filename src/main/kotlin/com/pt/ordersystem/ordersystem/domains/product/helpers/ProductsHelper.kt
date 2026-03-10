package com.pt.ordersystem.ordersystem.domains.product.helpers

import com.pt.ordersystem.ordersystem.domains.product.models.Product
import java.math.BigDecimal
import java.math.RoundingMode

object ProductsHelper {

    fun applyOverridesPrice(
        products: List<Product>,
        overrideMap: Map<String, BigDecimal>,
        discountPercentage: Int,
    ): List<Product> =
        products.map { product ->
            var priceAfterOverride = overrideMap[product.id] ?: product.price
            if (discountPercentage > 0) {
                val discountMultiplier = BigDecimal(100 - discountPercentage).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
                priceAfterOverride = priceAfterOverride.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP)
            }
            val finalPrice = priceAfterOverride.max(product.minimumPrice)
            product.copy(price = finalPrice)
        }

}
