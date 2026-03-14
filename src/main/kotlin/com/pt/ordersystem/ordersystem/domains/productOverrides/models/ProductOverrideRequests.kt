package com.pt.ordersystem.ordersystem.domains.productOverrides.models

import com.pt.ordersystem.ordersystem.fieldValidators.FieldValidators
import java.math.BigDecimal

data class CreateProductOverrideRequest(
  val productId: String,
  val customerId: String,
  val overridePrice: BigDecimal
) {
  fun validate() {
    FieldValidators.validatePriceRange(this.overridePrice)
  }
}

data class UpdateProductOverrideRequest(
  val overridePrice: BigDecimal
) {
  fun validate() {
    FieldValidators.validatePriceRange(this.overridePrice)
  }
}

