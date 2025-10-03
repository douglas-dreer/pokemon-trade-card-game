package br.com.pokemon.tradecardgame.domain.validation.series

import br.com.pokemon.tradecardgame.application.validation.CreateValidation
import br.com.pokemon.tradecardgame.domain.exception.SeriesAlreadyExistsException
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.validation.SerieValidator
import br.com.pokemon.tradecardgame.domain.validation.ValidatorStrategy
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.SeriesJpaAdapter
import org.springframework.stereotype.Component

@Component
@CreateValidation
class CreateSeriesValidator(
    private val repository: SeriesJpaAdapter
) : SerieValidator(), ValidatorStrategy<Serie> {
    /**
     * Validates the given series and ensures that there are no duplications with existing series
     * based on the code or name. If a series with the same code or name already exists, an exception
     * is thrown.
     *
     * @param item The series to be validated.
     * @throws SeriesAlreadyExistsException if a series with the same code or name already exists.
     */
    override fun execute(item: Serie) {
        if (super.checkIfExistSerieWithSameCode(item.code, repository)) {
            throw SeriesAlreadyExistsException(item.code)
        }

        if (super.checkIfExistSerieWithSameName(item.name, repository)) {
            throw SeriesAlreadyExistsException(item.name)
        }
    }
}