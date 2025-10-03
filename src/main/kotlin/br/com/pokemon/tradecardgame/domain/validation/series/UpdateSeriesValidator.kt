package br.com.pokemon.tradecardgame.domain.validation.series

import br.com.pokemon.tradecardgame.application.validation.CreateValidation
import br.com.pokemon.tradecardgame.application.validation.UpdateValidation
import br.com.pokemon.tradecardgame.domain.exception.InvalidDataException
import br.com.pokemon.tradecardgame.domain.exception.SeriesAlreadyExistsException
import br.com.pokemon.tradecardgame.domain.exception.SeriesNotFoundException
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.validation.SerieValidator
import br.com.pokemon.tradecardgame.domain.validation.ValidatorStrategy
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.SeriesJpaAdapter
import org.springframework.stereotype.Component

/**
 * Validator class for updating a series entity, implementing business rules and validations.
 *
 * This class extends [SerieValidator] and implements the [ValidatorStrategy] interface.
 * It verifies the necessary rules for updating a series, ensuring data integrity and consistency.
 * The operations in this validator include:
 * - Checking if a valid ID is provided for the series.
 * - Verifying the existence of a series with the specified ID in the repository.
 * - Ensuring the uniqueness of fields like code and name in the repository.
 *
 * The class is annotated with `@Component` for Spring's component scanning and managed as a bean.
 * The `@UpdateValidation` annotation is used as a custom qualifier to distinguish it
 * for creation validation use cases.
 *
 * @constructor Accepts a [SeriesJpaAdapter] dependency for repository operations.
 */
@Component
@UpdateValidation
class UpdateSeriesValidator (
    private val repository: SeriesJpaAdapter
): SerieValidator(), ValidatorStrategy<Serie>{
    /**
     * Validates and processes a given series based on specific business rules.
     *
     * This method performs the following validations:
     * 1. Ensures the series has a non-null identifier.
     * 2. Confirms the existence of a series with the specified identifier in the repository.
     * 3. Checks if there is no other series with the same code in the repository.
     * 4. Ensures there is no existing series with the same name in the repository.
     *
     * Throws appropriate exceptions if any of the validations fail.
     *
     * @param item The series to be validated. Must contain a valid identifier and pass all business rules.
     * @throws InvalidDataException If the series identifier is null.
     * @throws SeriesNotFoundException If the series with the specified identifier doesn't exist in the repository.
     * @throws SeriesAlreadyExistsException If a series with the same code or name already exists in the repository.
     */
    override fun execute(item: Serie) {
        if (item.id == null) throw InvalidDataException("Invalid serie id")
        if (!super.existsSerieById(item.id, repository)) throw SeriesNotFoundException(item.id)
        if (super.checkIfExistSerieWithSameCode(item.code, repository)) throw SeriesAlreadyExistsException(item.code)
        if (super.checkIfExistSerieWithSameName(item.name, repository)) throw SeriesAlreadyExistsException(item.name)
    }
}