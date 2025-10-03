package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.application.validation.UpdateValidation
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.UpdateSerieByIdUsecase
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.UpdateSerieCommand
import br.com.pokemon.tradecardgame.domain.validation.ValidatorStrategy
import org.springframework.stereotype.Service
import java.util.*

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
class UpdateSerieByIdUsecaseImpl(
    private val repository: SerieRepositoryPort,
    @UpdateValidation
    private val validator: List<ValidatorStrategy<Serie>>
) : UpdateSerieByIdUsecase {
    override fun execute(id: UUID, command: UpdateSerieCommand): Serie {
        executeValitation(id, command.toDomain())
        return repository.updateSerie(command.toEntity()).toDomain()
    }

    /**
     * Executes the validation process for the given `Serie` and its associated identifier.
     *
     * This method applies one or more validation strategies to the provided `Serie`
     * instance and ensures it adheres to the required business rules or constraints before
     * any further operations are performed.
     *
     * @param id The unique identifier associated with the `Serie` being validated.
     * @param serie The `Serie` instance that needs to be validated.
     */
    override fun executeValitation(id: UUID, serie: Serie) {
        validator.forEach { it.execute(serie) }
    }
}