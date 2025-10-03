package br.com.pokemon.tradecardgame.domain.exception

/**
 * Represents an exception indicating invalid data in the context of domain-specific rules.
 *
 * This exception is typically used to signal that the provided data violates certain
 * domain constraints or business logic rules. It extends [DomainException], leveraging
 * its functionality to provide detailed error messages and integration with the application's
 * exception hierarchy.
 *
 * @constructor Creates a new instance of [InvalidDataException] with a specific error message.
 * @param message A descriptive message explaining the reason for the invalid data.
 */
class InvalidDataException(message: String) : DomainException(message)