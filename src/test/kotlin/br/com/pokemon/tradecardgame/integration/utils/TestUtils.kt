package br.com.pokemon.tradecardgame.integration.utils

import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response.SerieResponse
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import br.com.pokemon.tradecardgame.integration.config.TestSecurityConfig
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.util.*

/**
 * Utility class providing common test operations and assertions for integration tests.
 *
 * This class contains helper methods for:
 * - HTTP request building and execution
 * - JSON serialization/deserialization
 * - Common assertions for Serie objects
 * - Test data validation utilities
 * - Authentication token generation
 */
object TestUtils {

    /**
     * Performs a POST request to create a Serie with authentication.
     *
     * @param mockMvc The MockMvc instance
     * @param objectMapper The ObjectMapper for JSON serialization
     * @param requestBody The request body object
     * @param endpoint The endpoint URL (defaults to "/series")
     * @return ResultActions for further assertions
     */
    fun performAuthenticatedPost(
        mockMvc: MockMvc,
        objectMapper: ObjectMapper,
        requestBody: Any,
        endpoint: String = "/series"
    ): ResultActions {
        return mockMvc.perform(
            post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .header("Authorization", "Bearer ${TestSecurityConfig.generateTestJwtToken()}")
        )
    }

    /**
     * Performs a GET request with authentication.
     *
     * @param mockMvc The MockMvc instance
     * @param endpoint The endpoint URL
     * @param params Optional query parameters
     * @return ResultActions for further assertions
     */
    fun performAuthenticatedGet(
        mockMvc: MockMvc,
        endpoint: String,
        params: Map<String, String> = emptyMap()
    ): ResultActions {
        val requestBuilder = get(endpoint)
            .header("Authorization", "Bearer ${TestSecurityConfig.generateTestJwtToken()}")

        params.forEach { (key, value) ->
            requestBuilder.param(key, value)
        }

        return mockMvc.perform(requestBuilder)
    }

    /**
     * Performs a PATCH request with authentication.
     *
     * @param mockMvc The MockMvc instance
     * @param objectMapper The ObjectMapper for JSON serialization
     * @param endpoint The endpoint URL
     * @param requestBody The request body object
     * @return ResultActions for further assertions
     */
    fun performAuthenticatedPatch(
        mockMvc: MockMvc,
        objectMapper: ObjectMapper,
        endpoint: String,
        requestBody: Any
    ): ResultActions {
        return mockMvc.perform(
            patch(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .header("Authorization", "Bearer ${TestSecurityConfig.generateTestJwtToken()}")
        )
    }

    /**
     * Performs a DELETE request with authentication.
     *
     * @param mockMvc The MockMvc instance
     * @param endpoint The endpoint URL
     * @return ResultActions for further assertions
     */
    fun performAuthenticatedDelete(
        mockMvc: MockMvc,
        endpoint: String
    ): ResultActions {
        return mockMvc.perform(
            delete(endpoint)
                .header("Authorization", "Bearer ${TestSecurityConfig.generateTestJwtToken()}")
        )
    }

    /**
     * Performs an unauthenticated request (no Authorization header).
     *
     * @param mockMvc The MockMvc instance
     * @param endpoint The endpoint URL
     * @param method The HTTP method (GET, POST, etc.)
     * @return ResultActions for further assertions
     */
    fun performUnauthenticatedRequest(
        mockMvc: MockMvc,
        endpoint: String,
        method: String = "GET"
    ): ResultActions {
        val requestBuilder = when (method.uppercase()) {
            "GET" -> get(endpoint)
            "POST" -> post(endpoint).contentType(MediaType.APPLICATION_JSON)
            "PATCH" -> patch(endpoint).contentType(MediaType.APPLICATION_JSON)
            "DELETE" -> delete(endpoint)
            else -> get(endpoint)
        }

        return mockMvc.perform(requestBuilder)
    }

    /**
     * Deserializes JSON response to SerieResponse object.
     *
     * @param resultActions The ResultActions from a MockMvc request
     * @param objectMapper The ObjectMapper for deserialization
     * @return The deserialized SerieResponse
     */
    fun extractSerieResponse(resultActions: ResultActions, objectMapper: ObjectMapper): SerieResponse {
        val jsonResponse = resultActions.andReturn().response.contentAsString
        return objectMapper.readValue(jsonResponse, SerieResponse::class.java)
    }

    /**
     * Asserts that two Serie objects have the same core properties.
     * Ignores timestamps and ID for comparison.
     *
     * @param expected The expected Serie
     * @param actual The actual Serie
     */
    fun assertSerieEquals(expected: Serie, actual: Serie) {
        assert(expected.code == actual.code) { "Code mismatch: expected ${expected.code}, got ${actual.code}" }
        assert(expected.name == actual.name) { "Name mismatch: expected ${expected.name}, got ${actual.name}" }
        assert(expected.releaseYear == actual.releaseYear) { "Release year mismatch: expected ${expected.releaseYear}, got ${actual.releaseYear}" }
        assert(expected.imageUrl == actual.imageUrl) { "Image URL mismatch: expected ${expected.imageUrl}, got ${actual.imageUrl}" }
    }

    /**
     * Asserts that a SerieEntity matches expected values.
     *
     * @param expected The expected SerieEntity
     * @param actual The actual SerieEntity
     */
    fun assertSerieEntityEquals(expected: SerieEntity, actual: SerieEntity) {
        assert(expected.code == actual.code) { "Code mismatch: expected ${expected.code}, got ${actual.code}" }
        assert(expected.name == actual.name) { "Name mismatch: expected ${expected.name}, got ${actual.name}" }
        assert(expected.releaseYear == actual.releaseYear) { "Release year mismatch: expected ${expected.releaseYear}, got ${actual.releaseYear}" }
        assert(expected.imageUrl == actual.imageUrl) { "Image URL mismatch: expected ${expected.imageUrl}, got ${actual.imageUrl}" }
    }

    /**
     * Asserts that a SerieResponse contains expected values.
     *
     * @param response The SerieResponse to validate
     * @param expectedCode The expected code
     * @param expectedName The expected name
     * @param expectedYear The expected release year
     * @param expectedImageUrl The expected image URL
     */
    fun assertSerieResponse(
        response: SerieResponse,
        expectedCode: String,
        expectedName: String,
        expectedYear: Int,
        expectedImageUrl: String? = null
    ) {
        assert(response.code == expectedCode) { "Code mismatch: expected $expectedCode, got ${response.code}" }
        assert(response.name == expectedName) { "Name mismatch: expected $expectedName, got ${response.name}" }
        assert(response.releaseYear == expectedYear) { "Release year mismatch: expected $expectedYear, got ${response.releaseYear}" }
        assert(response.imageUrl == expectedImageUrl) { "Image URL mismatch: expected $expectedImageUrl, got ${response.imageUrl}" }
        assert(response.id != null) { "ID should not be null in response" }
        assert(response.createdAt != null) { "CreatedAt should not be null in response" }
    }

    /**
     * Validates that a timestamp is recent (within the last minute).
     * Useful for testing auto-generated timestamps.
     *
     * @param timestamp The timestamp to validate
     * @param fieldName The name of the field for error messages
     */
    fun assertRecentTimestamp(timestamp: LocalDateTime?, fieldName: String = "timestamp") {
        assert(timestamp != null) { "$fieldName should not be null" }
        val now = LocalDateTime.now()
        val oneMinuteAgo = now.minusMinutes(1)
        assert(timestamp!!.isAfter(oneMinuteAgo) && timestamp.isBefore(now.plusMinutes(1))) {
            "$fieldName should be recent: $timestamp"
        }
    }

    /**
     * Generates a unique code for testing purposes.
     *
     * @param prefix The prefix for the code
     * @return A unique code string
     */
    fun generateUniqueCode(prefix: String = "TEST"): String {
        return "$prefix${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
    }

    /**
     * Generates a unique name for testing purposes.
     *
     * @param prefix The prefix for the name
     * @return A unique name string
     */
    fun generateUniqueName(prefix: String = "Test Serie"): String {
        return "$prefix ${UUID.randomUUID().toString().substring(0, 8)}"
    }

    /**
     * Creates a map of pagination parameters for testing.
     *
     * @param page The page number
     * @param size The page size
     * @return Map of pagination parameters
     */
    fun paginationParams(page: Int = 0, size: Int = 10): Map<String, String> {
        return mapOf(
            "page" to page.toString(),
            "size" to size.toString()
        )
    }

    /**
     * Validates JSON structure for error responses.
     *
     * @param resultActions The ResultActions from a MockMvc request
     * @param expectedStatus The expected HTTP status code
     * @param expectedMessage Optional expected error message
     */
    fun assertErrorResponse(
        resultActions: ResultActions,
        expectedStatus: Int,
        expectedMessage: String? = null
    ) {
        resultActions
            .andExpect(status().`is`(expectedStatus))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(expectedStatus))
            .andExpect(jsonPath("$.error").exists())

        expectedMessage?.let {
            resultActions.andExpect(jsonPath("$.message").value(it))
        }
    }

    /**
     * Validates successful response structure.
     *
     * @param resultActions The ResultActions from a MockMvc request
     * @param expectedStatus The expected HTTP status code
     */
    fun assertSuccessResponse(resultActions: ResultActions, expectedStatus: Int = 200) {
        resultActions
            .andExpect(status().`is`(expectedStatus))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }
}