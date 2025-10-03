package br.com.pokemon.tradecardgame.domain.port.`in`.serie.query

import java.util.*

/**
 * Represents a query for retrieving a series by its unique identifier.
 *
 * @property id The unique identifier of the series to be retrieved.
 */
data class FindSerieByIdQuery(
    val id: UUID
)
