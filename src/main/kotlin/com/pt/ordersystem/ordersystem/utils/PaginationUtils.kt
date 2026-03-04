package com.pt.ordersystem.ordersystem.utils

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

object PaginationUtils {

    const val MAX_PAGE_SIZE = 1000

    private fun validateMaxPageSize(pageSize: Int, maxPageSize: Int = MAX_PAGE_SIZE) {
        if (pageSize > maxPageSize)
            throw IllegalArgumentException("Max page size is $maxPageSize")
    }

    /**
     * Builds a validated [PageRequest] from [PageRequestBase].
     * Optionally restricts [sortBy] to [allowedSortFields] (falls back to [defaultSortBy]) and uses [maxPageSize] for validation.
     */
    fun getValidatedPageRequest(
        pageRequestBase: PageRequestBase,
        allowedSortFields: Set<String>? = null,
        defaultSortBy: String? = null,
        maxPageSize: Int = MAX_PAGE_SIZE,
    ): PageRequest {
        with(pageRequestBase) {
            validateMaxPageSize(pageSize, maxPageSize)

            val safeSortBy = when {
                allowedSortFields != null && defaultSortBy != null ->
                    if (sortBy in allowedSortFields) sortBy else defaultSortBy
                else -> sortBy
            }

            val sort = when (sortOrder) {
                SortOrder.ASC -> Sort.by(safeSortBy).ascending()
                SortOrder.DESC -> Sort.by(safeSortBy).descending()
            }

            return PageRequest.of(pageNumber, pageSize, sort)
        }
    }

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
) {
    fun toPageRequestBase(): PageRequestBase = PageRequestBase(
        pageNumber = pageNumber,
        pageSize = pageSize,
        sortBy = sortBy,
        sortOrder = SortOrder.fromString(sortOrder),
    )
}

data class PageRequestBase(
    val pageNumber: Int,
    val pageSize: Int,
    val sortBy: String,
    val sortOrder: SortOrder,
)