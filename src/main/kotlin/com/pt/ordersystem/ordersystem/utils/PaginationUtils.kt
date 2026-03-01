package com.pt.ordersystem.ordersystem.utils

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

object PaginationUtils {

    const val MAX_PAGE_SIZE = 1000

    private fun validateMaxPageSize(pageSize: Int) {
        if (pageSize > MAX_PAGE_SIZE)
            throw IllegalArgumentException("Max page size is $MAX_PAGE_SIZE")
    }

    fun getValidatedPageRequest(pageRequestBase: PageRequestBase): PageRequest {
        with(pageRequestBase) {
            validateMaxPageSize(pageSize)

            val sort = when (sortOrder) {
                SortOrder.ASC -> Sort.by(sortBy).ascending()
                SortOrder.DESC -> Sort.by(sortBy).descending()
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