package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response

import br.com.pokemon.tradecardgame.domain.model.Expansion
import java.time.LocalDateTime
import java.util.*

/**
 * Represents the response model for a collectible card series.
 *
 * This data class is used to transfer information about a card series in a structured
 * format. It includes details such as the unique identifier, series code, name, release year,
 * any associated expansions, and relevant metadata like creation and update timestamps.
 *
 * @property id The unique identifier for the series, nullable for cases where the ID may not be present.
 * @property code A unique code used to represent the series.
 * @property name The name of the series.
 * @property releaseYear The year in which the series was released.
 * @property imageUrl An optional string containing the URL of an image associated with the series.
 * @property expansions A list of expansions linked to this series, defaults to an empty list.
 * @property createdAt A timestamp indicating when the series was created, defaulting to the current time.
 * @property updatedAt A nullable timestamp representing the last update time for the series.
 */
data class SerieResponse(
    val id: UUID?,
    val code: String,
    val name: String,
    val releaseYear: Int,
    val imageUrl: String? = null,
    val expansions: List<Expansion> = emptyList(),
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null
)
