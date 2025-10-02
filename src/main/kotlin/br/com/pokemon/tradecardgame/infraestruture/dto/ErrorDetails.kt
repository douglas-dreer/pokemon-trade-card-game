package br.com.pokemon.tradecardgame.infraestruture.dto

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException


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
