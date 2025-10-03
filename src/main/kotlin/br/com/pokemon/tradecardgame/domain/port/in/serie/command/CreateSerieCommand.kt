package br.com.pokemon.tradecardgame.domain.port.`in`.serie.command

import br.com.pokemon.tradecardgame.domain.model.Expansion
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import java.time.LocalDateTime

/**
 * Command data class for creating a new series.
 *
 * This class encapsulates the data required to create a series, including mandatory properties
 * such as code, name, rarity, and release year, as well as optional properties like the URL
 * for the series image and a list of associated expansions. It serves as a part of the
 * application's command layer, transferring data from external requests to the business logic layer.
 *
 * @property code A unique identifier for the series.
 * @property name The name of the series.
 * @property imageUrl An optional URL pointing to an image representing the series.
 * @property expansions A list of [Expansion] instances included in the series, defaults to an empty list.
 * @property releaseYear The year the series was released.
 */
data class CreateSerieCommand(
    val code: String,
    val name: String,
    val imageUrl: String? = null,
    val expansions: List<Expansion> = emptyList(),
    val releaseYear: Int
) {
    /**
     * Maps the current `CreateSerieCommand` instance to a `SerieEntity` object.
     *
     * The resulting `SerieEntity` object contains the properties of the command mapped
     * to match the database entity structure. Additionally, the `expansions` list is
     * converted to a string of expansion codes, and the `createdAt` and `updatedAt`
     * timestamps are set to the current local date and time.
     *
     * @return A `SerieEntity` instance representing the mapped data of the series.
     */
    fun toEntity() = SerieEntity(
        id = null,
        code = this.code,
        name = this.name,
        releaseYear = this.releaseYear,
        imageUrl = this.imageUrl,
        expansions = this.expansions.joinToString { it.code },
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    /**
     * Maps the current `CreateSerieCommand` instance to a `Serie` domain model object.
     *
     * This method converts the fields from the `CreateSerieCommand` into a `Serie` domain
     * object. The `id` property of the resulting `Serie` is set as `null`, and the `createdAt`
     * and `updatedAt` timestamps are set to the current local date and time, ensuring that
     * the created domain object is prepared for further use in the application logic.
     *
     * @return A `Serie` instance representing the mapped data of the series in the domain model.
     */
    fun toDomain() = Serie(
        id = null,
        code = this.code,
        name = this.name,
        imageUrl = this.imageUrl,
        expansions = this.expansions,
        releaseYear = this.releaseYear,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}