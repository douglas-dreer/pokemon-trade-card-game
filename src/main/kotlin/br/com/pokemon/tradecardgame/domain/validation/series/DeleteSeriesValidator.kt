package br.com.pokemon.tradecardgame.domain.validation.series

import br.com.pokemon.tradecardgame.application.validation.DeleteValidation
import br.com.pokemon.tradecardgame.domain.exception.SeriesNotFoundException
import br.com.pokemon.tradecardgame.domain.validation.SerieValidator
import br.com.pokemon.tradecardgame.domain.validation.ValidatorStrategy
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.SeriesJpaAdapter
import org.springframework.stereotype.Component
import java.util.*

/**
 * Validator class responsible for validating series entities during the deletion operation.
 *
 * This class extends [SerieValidator] and implements the [ValidatorStrategy] interface
 * to ensure that series entities meet specific rules and constraints before being deleted.
 * The validation includes checking whether the series exists in the repository and whether
 * it has a valid identifier.
 *
 * @constructor Creates a new instance of [DeleteSeriesValidator] with a dependency on
 * [SeriesJpaAdapter], which provides access to the data layer for validation purposes.
 *
 * @property repository An instance of [SeriesJpaAdapter] used to query the database for
 * information about series entities during the validation process.
 */
@Component
@DeleteValidation
class DeleteSeriesValidator(
    private val repository: SeriesJpaAdapter
) : SerieValidator(), ValidatorStrategy<UUID> {
    /**
     * Validates the provided series identifier for deletion by ensuring the series exists in the repository.
     * If the series is not found, a [SeriesNotFoundException] is thrown.
     *
     * @param item The unique identifier of the series to be validated for deletion.
     * @throws SeriesNotFoundException If no series with the specified identifier exists.
     */
    override fun execute(item: UUID) {
        if (!repository.existsSerieById(item)) throw SeriesNotFoundException(item)
    }
}