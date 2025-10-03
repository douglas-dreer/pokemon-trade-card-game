package br.com.pokemon.tradecardgame.domain.validation

import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.SeriesJpaAdapter
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.repository.SeriesSpringRepository
import java.util.UUID

/**
 * Abstract class responsible for defining validation methods for a series entity.
 *
 * This class serves as a base for validating specific attributes or relationships
 * related to a series. It includes methods for checking the existence of a series
 * by various unique identifiers such as ID, code, or name.
 */
abstract class SerieValidator {

    /**
     * Checks whether a series exists with the given unique identifier.
     *
     * @param serieId The unique identifier of the series to check for existence.
     */
    fun existsSerieById(serieId: UUID, repository: SeriesJpaAdapter): Boolean {
        return repository.existsSerieById(serieId)
    }

    /**
     * Checks if there is an existing series with the same code.
     *
     * @param code The unique code of the series to validate.
     * @return True if a series with the same code exists, otherwise false.
     */
    fun checkIfExistSerieWithSameCode(code: String, repository: SeriesJpaAdapter): Boolean {
        return repository.existsSerieByCode(code)
    }

    /**
     * Checks if a series with the specified name already exists.
     *
     * @param name The name of the series to validate.
     * @return True if a series with the same name exists, otherwise false.
     */
    fun checkIfExistSerieWithSameName(name: String, repository: SeriesJpaAdapter): Boolean {
        return repository.existsSerieByName(name)
    }



}