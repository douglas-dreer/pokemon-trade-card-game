package br.com.pokemon.tradecardgame.domain.port.`in`.serie.command

import br.com.pokemon.tradecardgame.domain.model.Expansion
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import java.time.LocalDateTime
import java.util.*

/**
 * Represents a command used to update the details of a collectible card series.
 *
 * This data class encapsulates the information required to update an existing series,
 * including its identifier, code, name, release year, image URL, and associated expansions.
 * The `UpdateSerieCommand` is typically used to transfer updates from external layers (e.g., APIs)
 * to the domain or persistence layers.
 *
 * @property id The unique identifier of the series being updated, nullable for scenarios where the ID
 * is optional.
 * @property code A unique code representing the series.
 * @property name The name of the series.
 * @property releaseYear The year when the series was originally released.
 * @property imageUrl An optional URL pointing to an image for the series.
 * @property expansions A list of expansions belonging to the series, defaulting to an empty list.
 */
data class UpdateSerieCommand(
    val id: UUID?,
    val code: String,
    val name: String,
    val releaseYear: Int,
    val imageUrl: String? = null,
    val expansions: List<Expansion> = emptyList(),
) {
    /**
     * Converts the current `UpdateSerieCommand` instance to a `Serie` domain model object.
     *
     * This method maps the properties from the `UpdateSerieCommand` to a `Serie` object
     * in the domain layer, updating the `updatedAt` field to the current timestamp.
     * It is used to transform the command data into a domain representation suitable
     * for business logic operations.
     *
     * @return A `Serie` instance representing the mapped data of the series in the domain model.
     */
    fun toDomain(): Serie = Serie(
        id = this.id,
        code = this.code,
        name = this.name,
        releaseYear = this.releaseYear,
        imageUrl = this.imageUrl,
        updatedAt = LocalDateTime.now()
    )

    /**
     * Converts the current `UpdateSerieCommand` instance to a `SerieEntity` persistence model object.
     *
     * This method maps the properties from the `UpdateSerieCommand` to a `SerieEntity` object,
     * which represents the series in the persistence layer. Additionally, the `updatedAt` field
     * is set to the current timestamp to reflect the latest update time. This transformation is
     * typically used when persisting or updating data in the database.
     *
     * @return A `SerieEntity` instance representing the mapped data of the series for persistence.
     */
    fun toEntity() = SerieEntity(
        id = this.id,
        code = this.code,
        name = this.name,
        releaseYear = this.releaseYear,
        imageUrl = this.imageUrl,
        updatedAt = LocalDateTime.now()
    )
}