package com.pt.ordersystem.ordersystem.domains.product.models

import java.math.BigDecimal

interface ProductMapper {
  fun getId(): String
  fun getManagerId(): String
  fun getName(): String
  fun getBrandId(): Long?
  fun getBrandName(): String?
  fun getCategoryId(): Long?
  fun getCategoryName(): String?
  fun getMinimumPrice(): BigDecimal
  fun getPrice(): BigDecimal
  fun getDescription(): String
}

fun ProductMapper.toProduct(): Product = Product(
  id = getId(),
  managerId = getManagerId(),
  name = getName(),
  brandId = getBrandId(),
  brandName = getBrandName(),
  categoryId = getCategoryId(),
  categoryName = getCategoryName(),
  minimumPrice = getMinimumPrice(),
  price = getPrice(),
  description = getDescription(),
)
