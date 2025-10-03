package br.com.pokemon.tradecardgame.domain.exception

import java.util.UUID

/**
 * Indicates that a specific series was not found in the system.
 *
 * This exception is typically thrown when an operation attempts to access a series
 * identified by a unique identifier (UUID) that does not exist in the system. It serves
 * to notify clients of the application that the requested series could not be located,
 * ensuring clear communication about the issue.
 *
 * @constructor Creates a new [SeriesNotFoundException].
 * @param id The unique identifier of the series that was not found.
 */
class SeriesNotFoundException(
    id: UUID
) : DomainException("The series '$id' was not found in the system.")