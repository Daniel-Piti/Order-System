package com.pt.ordersystem.ordersystem.domains.product.models

import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
import java.math.BigDecimal

data class ProductInfo(
  val name: String,
  val brandId: Long?,
  val categoryId: Long?,
  val minimumPrice: BigDecimal,
  val price: BigDecimal,
  val description: String,
)

data class CreateProductRequest(
  val productInfo: ProductInfo,
  val imagesMetadata: List<ImageMetadata> = emptyList(),
)

data class CreateProductResponse(
  val productId: String,
  val imagesPreSignedUrls: List<String>,
)

data class UploadProductImagesResponse(
  val imagesPreSignedUrls: List<String>,
)
