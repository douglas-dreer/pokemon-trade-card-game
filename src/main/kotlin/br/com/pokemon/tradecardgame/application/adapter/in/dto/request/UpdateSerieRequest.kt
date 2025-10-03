package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request

import br.com.pokemon.tradecardgame.domain.model.Expansion
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.UpdateSerieCommand
import java.util.*

/**
 * Represents a request to update an existing `Serie`.
 *
 * This data class is used to encapsulate the required and optional fields needed to perform
 * an update operation on a `Serie`. An instance of `UpdateSerieRequest` typically includes
 * information such as the unique identifier of the `Serie`, its metadata, and associated
 * expansions. It is utilized in application layers that handle user input or API calls
 * targeting the modification of `Serie` entities.
 *
 * @property id The unique identifier of the `Serie` to be updated. This can be null
 *             when the identifier is not provided.
 * @property code A unique code representing the `Serie`, used to identify it within the system.
 * @property name The name of the `Serie`. This is a descriptive label for the entity.
 * @property releaseYear The release year of the `Serie`. It indicates the year the `Serie` was made available.
 * @property imageUrl An optional URL pointing to an image associated with the `Serie`, if available.
 * @property expansions A list of associated `Expansion` objects. Defaults to an empty list if no expansions are present.
 */
data class UpdateSerieRequest(
    val id: UUID?,
    val code: String,
    val name: String,
    val releaseYear: Int,
    val imageUrl: String? = null,
    val expansions: List<Expansion> = emptyList(),
) {
    init {
        require(code.isNotBlank()) { "Code must not be blank" }
        require(name.isNotBlank()) { "Name must not be blank" }
        require(releaseYear > 1998) { "Release year must be greater than 1998" }
    }

    /**
     * Converts the current `UpdateSerieRequest` instance to an `UpdateSerieCommand`.
     *
     * This method maps the properties of the `UpdateSerieRequest` to an equivalent
     * `UpdateSerieCommand` object. The transformation ensures compatibility with
     * the domain layer's requirements by creating a command object that encapsulates
     * the necessary data for updating a `Serie`.
     *
     * @return An instance of `UpdateSerieCommand` containing the mapped properties of the
     * current `UpdateSerieRequest`.
     */
    fun toCommand() = UpdateSerieCommand(
        id = this.id,
        code = this.code,
        name = this.name,
        releaseYear = this.releaseYear,
        imageUrl = this.imageUrl,
        expansions = this.expansions
    )
}
