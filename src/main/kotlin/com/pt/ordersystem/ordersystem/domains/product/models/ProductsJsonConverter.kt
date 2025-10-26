package com.pt.ordersystem.ordersystem.domains.product.models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class ProductsJsonConverter : AttributeConverter<List<ProductDataForOrder>, String> {
  private val mapper = jacksonObjectMapper()

  override fun convertToDatabaseColumn(products: List<ProductDataForOrder>?): String {
    return if (products == null) "[]" else mapper.writeValueAsString(products)
  }

  override fun convertToEntityAttribute(json: String?): List<ProductDataForOrder> {
    return if (json.isNullOrBlank()) emptyList() else mapper.readValue(json)
  }
}

