package br.com.pokemon.tradecardgame.domain.exception

/**
 * Indicates that a series with the specified name already exists in the system.
 *
 * This exception is thrown to enforce uniqueness constraints in the domain, preventing
 * the creation of multiple series with the same name. It helps maintain data integrity
 * by signaling an attempt to duplicate existing series records.
 *
 * @constructor Creates a new [SeriesAlreadyExistsException].
 * @param name The name of the series that already exists in the system.
 */
class SeriesAlreadyExistsException(
    name: String
) : DomainException("The series '$name' already exists in the system.")