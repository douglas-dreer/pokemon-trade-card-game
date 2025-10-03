package br.com.pokemon.tradecardgame.domain.port.`in`.serie.query


/**
 * Represents a query for retrieving a paginated list of series.
 *
 * @property page The current page number for the query, starting from 0 for the first page.
 * @property pageSize The number of items per page.
 */
data class FindAllSerieQuery(
    val page: Int,
    val pageSize: Int
)
