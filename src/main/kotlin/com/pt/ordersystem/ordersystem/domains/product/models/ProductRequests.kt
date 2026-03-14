package com.pt.ordersystem.ordersystem.domains.product.models

import com.pt.ordersystem.ordersystem.domains.product.helpers.ProductValidators
import com.pt.ordersystem.ordersystem.storage.models.ImageMetadata
import java.math.BigDecimal

data class ProductInfo(
  val name: String,
  val brandId: Long?,
  val categoryId: Long?,
  val minimumPrice: BigDecimal,
  val price: BigDecimal,
  val description: String,
) {
  fun validateAndNormalize(): ProductInfo {
    ProductValidators.validateProductInfo(this)
    ProductValidators.validatePriceHigherOrEqualToMinPrice(this.price, this.minimumPrice)

    return this.copy(
      name = this.name.trim(),
      description = this.description.trim(),
    )
  }
}

data class CreateProductRequest(
  val productInfo: ProductInfo,
  val imagesMetadata: List<ImageMetadata> = emptyList(),
) {
  fun validateAndNormalize(): CreateProductRequest {
    ProductValidators.validateProductInfo(this.productInfo)
    ProductValidators.validatePriceHigherOrEqualToMinPrice(productInfo.price, productInfo.minimumPrice)

    return this.copy(
      productInfo = productInfo.copy(
        name = this.productInfo.name.trim(),
        description = this.productInfo.description.trim(),
      ),
      imagesMetadata = imagesMetadata.map {
        it.copy(fileName = it.fileName.trim())
      }
    )
  }
}

data class CreateProductResponse(
  val product: ProductPrivateDto,
  val imagesPreSignedUrls: List<String>,
)

data class UploadProductImagesResponse(
  val imagesPreSignedUrls: List<String>,
)
