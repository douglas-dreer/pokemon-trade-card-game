package br.com.pokemon.tradecardgame.domain.model

import com.google.gson.Gson
import java.time.LocalDateTime
import java.util.*

/**
 * Represents a set of trading cards grouped as an expansion within a series.
 *
 * This class is used in the domain layer to encapsulate details about a specific expansion,
 * including its unique identifiers, associated trading cards, release date, and optional metadata.
 *
 * @property id The unique identifier for the expansion.
 * @property code A unique code representing the expansion.
 * @property name The name of the expansion.
 * @property imageUrl An optional URL pointing to the expansion's image.
 * @property cards A list of cards that belong to the expansion, defaults to an empty list.
 * @property dateReleased An optional timestamp indicating the release date of the expansion.
 * @property createdAt A timestamp indicating when the expansion was created, if available.
 * @property updatedAt A timestamp indicating the last time the expansion was updated, if available.
 */
data class Expansion(
    val id: UUID,
    val code: String,
    val name: String,
    val imageUrl: String? = null,
    val cards: List<Card> = emptyList(),
    val dateReleased: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    companion object {
        val gson: Gson = Gson()
    }

    /**
     * Serializes the current instance of the class into a JSON string representation.
     *
     * This method uses the Gson library to convert the object and its properties into
     * a JSON-formatted string. The resulting JSON string can be used for purposes such
     * as persistence, logging, or data transmission.
     *
     * @return A JSON string representation of the current object.
     */
    fun toJSON() = gson.toJson(this)
}
