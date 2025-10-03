package br.com.pokemon.tradecardgame.domain.port.`in`.serie

import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.DeleteSerieByIdCommand
import java.util.*

/**
 * Defines the use case for deleting a `Serie` by its unique identifier.
 *
 * This interface provides the contract for removing a `Serie` instance from the system,
 * including the execution of the deletion operation and the validation of the identifier.
 * Implementations of this interface are responsible for ensuring that the appropriate business
 * rules and constraints are applied during the deletion process.
 */
interface DeleteSerieByIdUsecase {
    /**
     * Executes the deletion of a `Serie` using the provided command.
     *
     * This method is responsible for managing the process of removing a `Serie` identified by
     * the unique identifier encapsulated in the `DeleteSerieByIdCommand`. The implementation
     * ensures that all necessary business rules and validations are applied during the operation.
     *
     * @param command The `DeleteSerieByIdCommand` containing the unique identifier of the
     *                `Serie` to be deleted. The command must include a valid UUID.
     */
    fun execute(command: DeleteSerieByIdCommand)

    /**
     * Validates the provided unique identifier of a `Serie`.
     *
     * This method is responsible for performing validation checks on the given
     * identifier to ensure it adheres to the necessary constraints and rules
     * defined for a `Serie`. The validation process ensures the identifier's
     * integrity before proceeding with other operations such as deletion or retrieval.
     *
     * @param serieId The unique identifier of the series to be validated. Must not be null
     *                and should correspond to a valid UUID.
     */
    fun executeValidation(serieId: UUID)
}