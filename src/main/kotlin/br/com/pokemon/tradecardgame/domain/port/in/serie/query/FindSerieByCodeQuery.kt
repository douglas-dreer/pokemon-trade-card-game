package br.com.pokemon.tradecardgame.domain.port.`in`.serie.query

/**
 * Represents a query for retrieving a series by its unique code.
 *
 * @property code The unique identifier code of the series to be retrieved.
 */
data class FindSerieByCodeQuery(
    val code: String
)
