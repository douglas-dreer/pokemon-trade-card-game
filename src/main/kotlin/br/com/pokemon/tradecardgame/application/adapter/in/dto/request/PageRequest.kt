package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request

import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindAllSerieQuery

data class PageRequest(
    var page: Int,
    val pageSize: Int,
    val sort: String? = "ASC",
    val direction: String?
) {
    init {
        page = if (page > 0) page -1 else 0
    }
    fun toQuery() = FindAllSerieQuery(page, pageSize)
}
