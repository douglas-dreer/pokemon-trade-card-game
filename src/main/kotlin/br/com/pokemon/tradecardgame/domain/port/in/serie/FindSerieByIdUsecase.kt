package br.com.pokemon.tradecardgame.domain.port.`in`.serie

import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindSerieByIdQuery

/**
 * Defines the use case for retrieving a `Serie` by its unique identifier.
 *
 * This interface specifies the contract for fetching a `Serie` domain object
 * using its unique identifier. It ensures that the retrieval process is executed
 * in compliance with the business rules, validation mechanisms, and any necessary
 * integrations. Implementations of this use case are responsible for returning the
 * requested `Serie` if it exists or handling scenarios where it does not.
 */
interface FindSerieByIdUsecase {
    /**
     * Executes the process of retrieving a `Serie` by its unique identifier.
     *
     * This method handles the fetching of a `Serie` object corresponding to the
     * provided query. It ensures that the retrieval process is conducted according
     * to the business rules and validation mechanisms defined by the use case implementation.
     *
     * @param query The `FindSerieByIdQuery` containing the unique identifier of the
     *              series to be retrieved.
     * @return The `Serie` object if found, or null if no series is associated with
     *         the provided identifier.
     */
    fun execute(query: FindSerieByIdQuery): Serie?
}