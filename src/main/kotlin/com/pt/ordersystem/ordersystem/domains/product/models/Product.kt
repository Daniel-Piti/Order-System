package com.pt.ordersystem.ordersystem.domains.product.models

import java.math.BigDecimal

data class Product(
  val id: String,
  val managerId: String,
  val name: String,
  val brandId: Long?,
  val brandName: String?,
  val categoryId: Long?,
  val categoryName: String?,
  val minimumPrice: BigDecimal,
  val price: BigDecimal,
  val description: String,
) {
  fun toPublicDto(): ProductPublicDto = ProductPublicDto(
    id = id,
    managerId = managerId,
    name = name,
    brandId = brandId,
    brandName = brandName,
    categoryId = categoryId,
    categoryName = categoryName,
    price = price,
    description = description,
  )

  fun toInternalDto(): ProductInternalDto = ProductInternalDto(
    id = id,
    managerId = managerId,
    name = name,
    brandId = brandId,
    brandName = brandName,
    categoryId = categoryId,
    categoryName = categoryName,
    minimumPrice = minimumPrice,
    price = price,
    description = description,
  )
}
