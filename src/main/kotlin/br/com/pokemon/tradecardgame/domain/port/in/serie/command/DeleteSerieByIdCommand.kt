package br.com.pokemon.tradecardgame.domain.port.`in`.serie.command

import java.util.UUID

/**
 * Represents the command to delete a specific series by its unique identifier.
 *
 * @property id The unique identifier of the series to be deleted.
 */
data class DeleteSerieByIdCommand(
    val id: UUID
) {

}