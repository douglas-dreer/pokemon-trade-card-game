package br.com.pokemon.tradecardgame.domain.port.`in`.serie

import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.UpdateSerieCommand
import java.util.*

/**
 * Defines the use case for updating an existing `Serie` by its unique identifier.
 *
 * This interface specifies the contract for handling the update process of a `Serie`
 * entity, including executing the update operation and performing validations on the
 * provided data. Implementations of this use case are responsible for ensuring the
 * integrity of the update process by applying domain rules, validation policies, and
 * any necessary transformations.
 */
interface UpdateSerieByIdUsecase {
    /**
     * Executes the process of updating an existing `Serie` identified by its unique identifier.
     *
     * This method handles the update flow for a `Serie`, integrating domain-specific validation,
     * business logic, and any additional transformation or processing required. The update process
     * modifies the attributes of the `Serie` based on the data provided in the `UpdateSerieCommand`.
     *
     * @param id The unique identifier of the `Serie` to be updated. Must be a valid non-null UUID.
     * @param command The `UpdateSerieCommand` object containing the new data for the `Serie`,
     *                including properties like code, name, release year, and other optional attributes.
     * @return The updated `Serie` domain object reflecting the changes applied to it.
     */
    fun execute(id: UUID, command: UpdateSerieCommand): Serie

    /**
     * Executes the validation process for a specific `Serie` identified by its unique identifier.
     *
     * This method validates the integrity, correctness, and compliance of the provided `Serie`
     * instance against the domain rules and business requirements.
     *
     * @param id The unique identifier of the `Serie` being validated. Must not be null and must
     *           correspond to a valid UUID.
     * @param serie The `Serie` instance to validate, containing details such as code, name,
     *              release year, and any other metadata associated with the `Serie`.
     */
    fun executeValitation(id: UUID, serie: Serie)
}