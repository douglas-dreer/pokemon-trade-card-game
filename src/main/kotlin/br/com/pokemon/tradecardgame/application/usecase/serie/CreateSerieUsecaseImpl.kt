package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.application.validation.CreateValidation
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.CreateSerieUsecase
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.CreateSerieCommand
import br.com.pokemon.tradecardgame.domain.validation.ValidatorStrategy
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.SeriesJpaAdapter
import org.springframework.stereotype.Service

/**
 * Implementation of the `CreateSerieUsecase` interface responsible for handling
 * the creation of a new `Serie` in the system. This class defines the business logic for
 * creating a `Serie`, applying validation strategies, and delegating the actual persistence
 * operations to the repository layer.
 *
 * @constructor Creates an instance of `CreateSerieUsecaseImpl` with necessary dependencies.
 * @param repository The repository adapter used to persist the `Serie` entity.
 * @param createValidators A list of validation strategies annotated with `@CreateValidation`, applied to validate the `Serie` before creation.
 */
@Service
class CreateSerieUsecaseImpl(
    private val repository: SeriesJpaAdapter,
    @CreateValidation
    private val createValidators: List<ValidatorStrategy<Serie>>
) : CreateSerieUsecase {
    /**
     * Executes the process of creating a new `Serie` in the system. This includes performing
     * necessary validations on the provided command, converting it to the appropriate domain
     * model and entity, and delegating the entity persistence to the repository layer.
     *
     * @param command The command object containing the data necessary to create the new `Serie`.
     * @return The created `Serie` domain object after successful persistence.
     */
    override fun execute(command: CreateSerieCommand): Serie {
        executeValidation(command.toDomain())

        return repository
            .createSerie(command.toEntity())
            .toDomain()

    }

    /**
     * Executes the validation process for the given `Serie` instance by iterating
     * through a list of predefined validation strategies and applying them.
     *
     * @param serie The `Serie` instance to be validated.
     */
    override fun executeValidation(serie: Serie) {
        createValidators.forEach { it.execute(serie) }
    }
}