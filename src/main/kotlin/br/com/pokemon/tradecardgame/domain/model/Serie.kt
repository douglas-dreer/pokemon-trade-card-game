package br.com.pokemon.tradecardgame.domain.model

import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response.SerieResponse
import java.time.LocalDateTime
import java.util.*

/**
 * Represents a collectible card series within the domain model.
 *
 * This data class encapsulates the properties of a series, including its unique identifier,
 * code, name, release year, associated expansions, and other metadata. It serves as the primary
 * representation of a card series used in the domain layer.
 *
 * @property id The unique identifier for the series, nullable for instances where the ID may be generated later.
 * @property code A unique code representing the series.
 * @property name The name of the series.
 * @property releaseYear The year when the series was released.
 * @property imageUrl An optional URL pointing to an image that represents the series.
 * @property expansions A list of expansions that belong to the series, defaults to an empty list.
 * @property createdAt A timestamp indicating when the series was created, defaulting to the current time.
 * @property updatedAt A nullable timestamp indicating the last time the series was updated.
 */
data class Serie(
    val id: UUID?,
    val code: String,
    val name: String,
    val releaseYear: Int,
    val imageUrl: String? = null,
    val expansions: List<Expansion> = emptyList(),
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null
) {
    fun toResponse() = SerieResponse(
        id = this.id,
        code = this.code,
        name = this.name,
        releaseYear = this.releaseYear,
        imageUrl = this.imageUrl,
        expansions = this.expansions,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
