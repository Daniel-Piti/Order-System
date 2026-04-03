package com.pt.ordersystem.ordersystem.utils

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

object PaginationUtils {
    const val MAX_PAGE_SIZE = 1000
}

enum class SortOrder {
    ASC,
    DESC,
    ;

    companion object {
        fun fromString(sortOrder: String): SortOrder =
            when (sortOrder.trim().lowercase()) {
                "asc" -> ASC
                "desc" -> DESC
                else -> throw IllegalArgumentException("Invalid sort order: $sortOrder (use asc or desc)")
            }
    }
}

data class PageRequestBaseExternal(
    val pageNumber: Int,
    val pageSize: Int,
    val sortBy: String,
    val sortOrder: String,
)

/**
 * Parses query params, validates max page size and sort order, and returns a Spring [PageRequest].
 *
 * [sortBy] must be one of [allowedSortFields] or an [IllegalArgumentException] is thrown.
 */
fun PageRequestBaseExternal.toValidatedPageRequest(
    allowedSortFields: Set<String>,
): PageRequest {
    if (pageSize > PaginationUtils.MAX_PAGE_SIZE) {
        throw IllegalArgumentException("Max page size is ${PaginationUtils.MAX_PAGE_SIZE}")
    }

    val sortOrderEnum = SortOrder.fromString(sortOrder)

    if (sortBy !in allowedSortFields) {
        throw IllegalArgumentException(
            "Invalid sortBy: '$sortBy'. Allowed values: ${allowedSortFields.sorted().joinToString(", ")}",
        )
    }

    val sort = when (sortOrderEnum) {
        SortOrder.ASC -> Sort.by(sortBy).ascending()
        SortOrder.DESC -> Sort.by(sortBy).descending()
    }

    return PageRequest.of(pageNumber, pageSize, sort)
}
