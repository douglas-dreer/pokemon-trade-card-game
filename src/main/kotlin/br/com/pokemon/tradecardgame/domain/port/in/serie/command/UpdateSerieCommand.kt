package br.com.pokemon.tradecardgame.domain.port.`in`.serie.command

import br.com.pokemon.tradecardgame.domain.enums.RarityType
import br.com.pokemon.tradecardgame.domain.model.Expansion
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import java.time.LocalDateTime
import java.util.*

/**
 * Command data class for updating an existing series.
 *
 * This class is used to encapsulate the required and optional attributes needed to
 * update a series within the domain layer. It transfers data from external input
 * sources to the application's business logic. The command includes properties
 * such as a unique identifier, code, name, rarity, an optional image URL, and the
 * associated expansion data.
 *
 * @property id The unique identifier of the series to be updated.
 * @property code The code that uniquely identifies the series.
 * @property name The name of the series.
 * @property rarity The rarity type associated with the series, represented by [RarityType].
 * @property imageUrl An optional URL pointing to an image representing the series.
 * @property expansion An [Expansion] instance related to this series.
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