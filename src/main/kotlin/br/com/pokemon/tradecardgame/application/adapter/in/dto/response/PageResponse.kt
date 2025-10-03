package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
) {
    constructor(page: Page<T>) : this(
        content = page.content,
        page = page.number,
        pageSize = page.size,
        totalElements = page.totalElements,
        totalPages = page.totalPages,
        last = page.isLast
    )
}
