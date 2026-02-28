package com.pt.ordersystem.ordersystem.storage

object S3Helper {
    private lateinit var publicDomain: String

    fun init(publicDomain: String) {
        this.publicDomain = publicDomain
    }

    fun getPublicUrl(s3Key: String?): String? {
        if (s3Key == null) return null
        val domain = if (publicDomain.endsWith("/")) publicDomain else "$publicDomain/"
        return "${domain}${s3Key}"
    }

}
