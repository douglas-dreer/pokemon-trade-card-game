package br.com.pokemon.tradecardgame.domain.port.`in`.serie

import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.CreateSerieCommand

/**
 * Defines the use case for creating a new `Serie` within the application.
 *
 * This interface specifies the contract for handling the creation process of a `Serie`,
 * including executing the creation operation and performing validations on the domain
 * model. The implementation of this use case is responsible for integrating with
 * the necessary business logic, validation strategies, and persistence mechanisms to
 * ensure the integrity of the `Serie`.
 */
interface CreateSerieUsecase {
    /**
     * Executes the process of creating a new `Serie` within the application.
     *
     * This method is responsible for handling the entire creation flow of a `Serie`
     * using the provided `CreateSerieCommand`. It integrates domain validation,
     * business logic, and persistence mechanisms to create a fully defined
     * `Serie` object and return it as the result.
     *
     * @param command The `CreateSerieCommand` instance containing the necessary
     *                data to define and create a new `Serie`, including its
     *                code, name, rarity, release year, and optional additional details.
     * @return The created `Serie` domain object populated with all relevant
     *         information and metadata, including generated values like timestamps.
     */
    fun execute(command: CreateSerieCommand): Serie

    /**
     * Executes the validation process for a given `Serie` object.
     *
     * This method is responsible for applying domain-specific validation rules on the provided
     * `Serie` instance to ensure its integrity and compliance with the business rules.
     *
     * @param serie The `Serie` instance to be validated, containing information such as
     *              code, name, rarity, release year, and other related details.
     */
    fun executeValidation(serie: Serie)
}