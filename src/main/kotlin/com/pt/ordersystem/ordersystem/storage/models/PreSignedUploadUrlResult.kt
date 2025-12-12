package com.pt.ordersystem.ordersystem.storage.models

data class PreSignedUploadUrlResult(
  val preSignedUrl: String,
  val s3Key: String
)
