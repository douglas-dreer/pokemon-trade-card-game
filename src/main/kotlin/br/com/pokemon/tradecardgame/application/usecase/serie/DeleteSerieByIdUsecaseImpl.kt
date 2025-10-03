package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.application.validation.DeleteValidation
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.DeleteSerieByIdUsecase
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.DeleteSerieByIdCommand
import br.com.pokemon.tradecardgame.domain.validation.ValidatorStrategy
import org.springframework.stereotype.Service
import java.util.UUID

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
class DeleteSerieByIdUsecaseImpl(
    private val repository: SerieRepositoryPort,
    @DeleteValidation
    private val validator: List<ValidatorStrategy<UUID>>
): DeleteSerieByIdUsecase {

    /**
     * Executes the process of deleting a `Serie` by its unique identifier. This method performs
     * validation on the given command's ID and delegates the deletion of the series to the repository.
     *
     * @param command The command object containing the unique identifier of the `Serie` to be deleted.
     */
    override fun execute(command: DeleteSerieByIdCommand) {
        executeValidation(command.id)
        repository.deleteSerieById(command.id)
    }

    /**
     * Executes the validation process for the given `UUID` representing a series identifier.
     * This method iterates through a list of predefined validation strategies and applies them
     * on the provided `serieId`.
     *
     * @param serieId The unique identifier of the series to be validated.
     */
    override fun executeValidation(serieId: UUID) {
        validator.forEach { it.execute(serieId) }
    }

}