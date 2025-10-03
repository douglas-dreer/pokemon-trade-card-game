package br.com.pokemon.tradecardgame.infraestruture.dto

import java.time.LocalDateTime

/**
 * Represents a standardized error response structure.
 *
 * The `ErrorResponse` class is used to encapsulate error details in a consistent way,
 * typically for RESTful APIs. It includes information such as the type of error,
 * a descriptive message, the HTTP status code, and a timestamp indicating when
 * the error occurred. Optional detailed messages can also be provided.
 *
 * @property error A brief description or classification of the error.
 * @property message A detailed message providing additional context about the error.
 * @property status The HTTP status code associated with the error.
 * @property timestamp The date and time when the error occurred.
 * @property details A list of optional additional details about the error.
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val status: Int,
    val timestamp: LocalDateTime,
    val details: List<String> = emptyList()
)
