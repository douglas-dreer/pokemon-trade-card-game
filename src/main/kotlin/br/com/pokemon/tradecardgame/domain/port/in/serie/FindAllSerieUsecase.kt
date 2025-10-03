package br.com.pokemon.tradecardgame.domain.port.`in`.serie

import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindAllSerieQuery
import org.springframework.data.domain.Page

/**
 * Defines the use case for retrieving a paginated list of `Serie` objects.
 *
 * This interface specifies the contract for fetching a list of `Serie` domain objects,
 * allowing pagination and sorting constraints to be applied. The implementation ensures
 * that the retrieval process is compliant with business rules and domain requirements.
 */
interface FindAllSerieUsecase {

    /**
     * Executes the retrieval process for a paginated list of `Serie` objects based on the given query.
     *
     * This method handles the fetching of series data by applying pagination and sorting constraints as defined
     * in the `FindAllSerieQuery`. The result is encapsulated within a `Page` object containing the series data
     * and pagination metadata.
     *
     * @param query The `FindAllSerieQuery` instance specifying pagination criteria, such as the requested page
     *              number and the number of items per page.
     * @return A `Page` object containing the list of `Serie` objects and pagination details.
     */
    fun execute(query: FindAllSerieQuery): Page<Serie>
}