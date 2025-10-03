package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response

import java.time.LocalDateTime

/**
 * Represents a successful response message with accompanying metadata.
 *
 * This data class is used to encapsulate information about a successful operation,
 * including a descriptive title, a detailed message, and a timestamp indicating when
 * the response was generated.
 *
 * @property title A brief title describing the success.
 * @property message A detailed message providing additional information about the success.
 * @property timestamp The time the response was generated, defaulting to the current time.
 */
data class SuccessResponse(
    val title: String,
    val message: String,
    val timestamp: LocalDateTime? = LocalDateTime.now()
)
