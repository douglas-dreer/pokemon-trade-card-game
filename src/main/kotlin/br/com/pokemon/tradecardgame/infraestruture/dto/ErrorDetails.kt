package br.com.pokemon.tradecardgame.infraestruture.dto

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * Represents the details of an error response.
 *
 * This class encapsulates information about an error, including its status code,
 * associated error message, and an optional description. It is often used
 * to provide structured error responses in web applications.
 *
 * @property status The HTTP status code associated with the error.
 * @property error A brief description or classification of the error.
 * @property message An optional detailed message providing additional context about the error.
 */
data class ErrorDetails(
    val status: Int,
    val error: String,
    val message: String? = null
) {
    companion object {
        private val gson = Gson()
    }

    /**
     * Serializes the [ErrorDetails] instance to a JSON string.
     *
     * @return A JSON string representation of the object.
     * @throws JsonSyntaxException If serialization fails due to invalid data.
     */
    fun toJSON(): String {
        return try {
            gson.toJson(this)
        } catch (e: JsonSyntaxException) {
            throw JsonSyntaxException("Failed to serialize ErrorDetails to JSON: ${e.message}")
        }
    }
}
