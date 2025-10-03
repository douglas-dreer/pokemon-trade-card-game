package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.FindAllSerieUsecase
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindAllSerieQuery
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

/**
 * Implementation of the `FindAllSerieUsecase` interface for retrieving all `Serie` entities.
 *
 * This class provides the concrete implementation of fetching a paginated list of domain-level
 * `Serie` objects. Using the `SerieRepositoryPort`, it retrieves the underlying series data
 * and maps it from the entity layer to the domain model. The pagination parameters, such as page
 * number and page size, ensure controlled data retrieval.
 *
 * @constructor Initializes the implementation with a `SerieRepositoryPort` dependency
 *              to interact with the repository layer.
 * @property repository The repository port used for accessing the persistence layer to
 *                      fetch and map series data.
 */
@Service
class FindAllSerieUsecaseImpl(
    private val repository: SerieRepositoryPort
) : FindAllSerieUsecase {
    /**
     * Executes the process of retrieving a paginated list of `Serie` domain objects based on the given query parameters.
     *
     * This method leverages the repository layer to fetch `SerieEntity` instances, maps them to the domain model,
     * and returns them as a paginated result. The query parameters allow controlling the pagination by specifying
     * the page number and the number of items per page.
     *
     * @param query The query object containing pagination parameters such as page number and page size.
     * @return A `Page` containing the mapped `Serie` domain objects, representing the paginated result.
     */
    override fun execute(query: FindAllSerieQuery): Page<Serie> {
        return repository.findAllSeries(query.page, query.pageSize).map { it.toDomain() }
    }
}