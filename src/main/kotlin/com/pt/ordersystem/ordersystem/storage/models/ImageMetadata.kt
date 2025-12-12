package com.pt.ordersystem.ordersystem.storage.models

data class ImageMetadata(
  val fileName: String,
  val contentType: String,
  val fileSizeBytes: Long,
  val fileMd5Base64: String
)
