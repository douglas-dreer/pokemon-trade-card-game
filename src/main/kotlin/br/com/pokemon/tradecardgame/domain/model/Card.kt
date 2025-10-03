package br.com.pokemon.tradecardgame.domain.model

import br.com.pokemon.tradecardgame.domain.enums.RarityType
import java.time.LocalDateTime
import java.util.*

/**
 * Represents a trading card with associated metadata for collectibles.
 *
 * This data class is used in the domain layer to encapsulate the properties of a single card.
 * Cards are typically part of an expansion and have various characteristics such as rarity,
 * unique identifiers, and optional images.
 *
 * @property id The unique identifier for the card.
 * @property code The code associated with the card for identification within its series or expansion.
 * @property name The name of the card.
 * @property rarity The rarity type of the card, defined by the RarityType enum.
 * @property imageUrl The URL of an image representing the card, if available.
 * @property expansion The expansion to which the card belongs.
 * @property createdAt The timestamp indicating when the card was created, if available.
 * @property updatedAt The timestamp indicating the last time the card was updated, if available.
 */
data class Card(
    val id: UUID,
    val code: String,
    val name: String,
    val rarity: RarityType,
    val imageUrl: String? = null,
    val expansion: Expansion,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
