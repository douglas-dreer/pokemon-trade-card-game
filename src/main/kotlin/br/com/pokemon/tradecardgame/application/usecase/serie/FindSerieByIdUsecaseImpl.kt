package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.FindSerieByIdUsecase
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindSerieByIdQuery
import org.springframework.stereotype.Service

/**
 * Implementation of the `FindSerieByIdUsecase` interface responsible for retrieving
 * a `Serie` by its unique identifier.
 *
 * This class utilizes a `SerieRepositoryPort` to fetch the desired series entity
 * from the persistence layer, map it to the domain model, and return it as a
 * `Serie` object. If no series is found with the given identifier, the method
 * returns `null`.
 *
 * @constructor Creates an instance of `FindSerieByIdUsecaseImpl` with a specified
 * `SerieRepositoryPort` implementation for data retrieval.
 *
 * @param repository The repository port responsible for interacting with the
 * persistence layer to fetch series data.
 */
@Service
class FindSerieByIdUsecaseImpl(
    private val repository: SerieRepositoryPort
): FindSerieByIdUsecase {
    /**
     * Executes the operation to retrieve a series by its unique identifier.
     *
     * This method interacts with the repository to fetch a series entity by its ID,
     * maps it to the domain model, and returns it. If no series is found for the
     * given identifier, the method returns null.
     *
     * @param query The query containing the unique identifier of the series to be retrieved.
     * @return The corresponding domain model [Serie] if found, or null if not found.
     */
    override fun execute(query: FindSerieByIdQuery): Serie? {
        return repository.findSerieById(query.id)?.toDomain()
    }
}