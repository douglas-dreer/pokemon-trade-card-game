package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.FindSerieByCodeUsecase
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindSerieByCodeQuery
import org.springframework.stereotype.Service

/**
 * Implementation of the `FindSerieByCodeUsecase` interface, responsible for retrieving a `Serie`
 * domain object based on its unique code.
 *
 * This class serves as the application layer's use case implementation, coordinating the process of
 * fetching a `Serie` through the specified `SerieRepositoryPort`. It interacts with the repository
 * to retrieve the series entity by its code and maps it to the domain model using the `toDomain` method.
 *
 * @property repository The repository port used to access and manage `Serie` data.
 */
@Service
class FindSerieByCodeUsecaseImpl(
    private val repository: SerieRepositoryPort
) : FindSerieByCodeUsecase {
    /**
     * Executes the use case for retrieving a `Serie` domain object based on the provided query.
     *
     * This method interacts with the repository layer to fetch the data for a series using its
     * unique code and maps the persisted entity to the corresponding domain model.
     *
     * @param query The query object containing the unique code of the series to be retrieved.
     * @return The `Serie` domain object if found, or null if no series with the given code exists.
     */
    override fun execute(query: FindSerieByCodeQuery): Serie? {
        return repository.findSerieByCode(query.code)?.toDomain()
    }

}