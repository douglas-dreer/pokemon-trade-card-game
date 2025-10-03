package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request

import br.com.pokemon.tradecardgame.domain.model.Expansion
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.CreateSerieCommand

/**
 * Represents a request to create a new `Serie`.
 *
 * This data class encapsulates the information required to define a new `Serie`.
 * A `Serie` is a collection of expansions associated with particular metadata like
 * its code, name, release year, and optional image. It includes timestamps to track
 * when the series was created and last updated.
 *
 * @property code A unique code representing the `Serie`, used for identification within the system.
 * @property name The name of the `Serie`, serving as its descriptive label.
 * @property releaseYear The year in which the `Serie` was released.
 * @property imageUrl An optional URL pointing to an image associated with the `Serie`.
 * @property expansions A list of associated `Expansion` objects that are part of the `Serie`. Defaults to an empty list if no expansions are included.
 */
data class CreateSerieRequest(
    val code: String,
    val name: String,
    val releaseYear: Int,
    val imageUrl: String? = null,
    val expansions: List<Expansion> = emptyList()
) {
    init {
        require(code.isNotBlank()) { "Code must not be blank" }
        require(name.isNotBlank()) { "Name must not be blank" }
        require(releaseYear > 1998) { "Release year must be greater than 1998" }
    }

    /**
     * Converts the current `CreateSerieRequest` instance to a `CreateSerieCommand`.
     *
     * This method maps the properties of the `CreateSerieRequest` to an equivalent
     * `CreateSerieCommand` object. The transformation ensures that the data is prepared
     * for the domain layer by encapsulating it in a command object, which includes
     * mandatory fields like `code`, `name`, and `releaseYear`, along with optional fields
     * such as `imageUrl` and `expansions`.
     *
     * @return A `CreateSerieCommand` containing the mapped data of the current `CreateSerieRequest`.
     */
    fun toCommand() = CreateSerieCommand(
        code = code,
        name = name,
        imageUrl = imageUrl,
        expansions = expansions,
        releaseYear = releaseYear
    )
}
