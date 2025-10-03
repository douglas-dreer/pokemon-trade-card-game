package br.com.pokemon.tradecardgame.domain.port.`in`.serie

import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindSerieByCodeQuery

/**
 * Defines the use case for retrieving a `Serie` by its unique code.
 *
 * This interface specifies the contract for fetching a `Serie` domain object using its unique code.
 * It ensures that the retrieval process is executed in alignment with the domain's business rules,
 * validation mechanisms, and any necessary integrations. Implementations are responsible for returning
 * the requested `Serie` if it exists, or handling cases where it does not.
 */
interface FindSerieByCodeUsecase {
    fun execute(query: FindSerieByCodeQuery): Serie?
}