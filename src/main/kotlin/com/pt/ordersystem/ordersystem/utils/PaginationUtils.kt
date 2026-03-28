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
 * Parses query params, validates max page size and sort order, optionally clamps [sortBy] to [allowedSortFields],
 * and returns a Spring [PageRequest].
 */
fun PageRequestBaseExternal.toValidatedPageRequest(
    allowedSortFields: Set<String>? = null,
    defaultSortBy: String? = null,
): PageRequest {
    if (pageSize > PaginationUtils.MAX_PAGE_SIZE) {
        throw IllegalArgumentException("Max page size is ${PaginationUtils.MAX_PAGE_SIZE}")
    }

    val sortOrderEnum = SortOrder.fromString(sortOrder)

    val safeSortBy = when {
        allowedSortFields != null && defaultSortBy != null ->
            if (sortBy in allowedSortFields) sortBy else defaultSortBy
        else -> sortBy
    }

    val sort = when (sortOrderEnum) {
        SortOrder.ASC -> Sort.by(safeSortBy).ascending()
        SortOrder.DESC -> Sort.by(safeSortBy).descending()
    }

    return PageRequest.of(pageNumber, pageSize, sort)
}
