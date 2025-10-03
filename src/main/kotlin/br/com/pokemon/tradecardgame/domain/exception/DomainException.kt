package br.com.pokemon.tradecardgame.domain.exception

/**
 * Base class for domain-specific exceptions in the application.
 *
 * This exception serves as the foundation for more specific exceptions that represent
 * business rule violations or domain constraints in the system. Subclasses of
 * [DomainException] should provide additional context or specify particular issues
 * related to the domain logic.
 *
 * It extends [RuntimeException], allowing it to be thrown and handled as part of
 * standard exception handling mechanisms.
 *
 * @property message A descriptive message providing detailed information about the exception.
 */
open class DomainException(
    override val message: String
) : RuntimeException(message)