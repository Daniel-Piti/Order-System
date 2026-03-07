package com.pt.ordersystem.ordersystem.domains.order.models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class SelectedLocationJsonConverter : AttributeConverter<SelectedLocation?, String?> {
  private val mapper = jacksonObjectMapper()

  override fun convertToDatabaseColumn(attribute: SelectedLocation?): String? {
    return if (attribute == null) null else mapper.writeValueAsString(attribute)
  }

  override fun convertToEntityAttribute(json: String?): SelectedLocation? {
    return if (json.isNullOrBlank()) null else mapper.readValue(json)
  }
}
