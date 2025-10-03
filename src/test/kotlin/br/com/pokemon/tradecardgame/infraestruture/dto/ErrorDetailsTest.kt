package br.com.pokemon.tradecardgame.infraestruture.dto

import com.google.gson.JsonSyntaxException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ErrorDetailsTest {

    @Test
    fun `should create error details with all fields`() {
        // Given
        val status = 400
        val error = "Bad Request"
        val message = "Invalid input data provided"

        // When
        val errorDetails = ErrorDetails(
            status = status,
            error = error,
            message = message
        )

        // Then
        assertEquals(status, errorDetails.status)
        assertEquals(error, errorDetails.error)
        assertEquals(message, errorDetails.message)
    }

    @Test
    fun `should create error details with minimal fields`() {
        // Given
        val status = 404
        val error = "Not Found"

        // When
        val errorDetails = ErrorDetails(
            status = status,
            error = error
        )

        // Then
        assertEquals(status, errorDetails.status)
        assertEquals(error, errorDetails.error)
        assertEquals(null, errorDetails.message)
    }

    @Test
    fun `should create error details with null message`() {
        // Given
        val status = 500
        val error = "Internal Server Error"
        val message = null

        // When
        val errorDetails = ErrorDetails(
            status = status,
            error = error,
            message = message
        )

        // Then
        assertEquals(status, errorDetails.status)
        assertEquals(error, errorDetails.error)
        assertEquals(null, errorDetails.message)
    }

    @Test
    fun `should serialize to JSON successfully`() {
        // Given
        val errorDetails = ErrorDetails(
            status = 400,
            error = "Bad Request",
            message = "Invalid data"
        )

        // When
        val json = errorDetails.toJSON()

        // Then
        assertNotNull(json)
        assertTrue(json.contains("\"status\":400"))
        assertTrue(json.contains("\"error\":\"Bad Request\""))
        assertTrue(json.contains("\"message\":\"Invalid data\""))
    }

    @Test
    fun `should serialize to JSON with null message`() {
        // Given
        val errorDetails = ErrorDetails(
            status = 404,
            error = "Not Found",
            message = null
        )

        // When
        val json = errorDetails.toJSON()

        // Then
        assertNotNull(json)
        assertTrue(json.contains("\"status\":404"))
        assertTrue(json.contains("\"error\":\"Not Found\""))
        assertTrue(json.contains("\"message\":null") || json.contains("\"message\": null"))
    }

    @Test
    fun `should serialize to JSON with empty message`() {
        // Given
        val errorDetails = ErrorDetails(
            status = 400,
            error = "Bad Request",
            message = ""
        )

        // When
        val json = errorDetails.toJSON()

        // Then
        assertNotNull(json)
        assertTrue(json.contains("\"status\":400"))
        assertTrue(json.contains("\"error\":\"Bad Request\""))
        assertTrue(json.contains("\"message\":\"\""))
    }

    @Test
    fun `should handle different HTTP status codes`() {
        // Given & When
        val badRequest = ErrorDetails(400, "Bad Request", "Invalid input")
        val notFound = ErrorDetails(404, "Not Found", "Resource not found")
        val conflict = ErrorDetails(409, "Conflict", "Resource already exists")
        val serverError = ErrorDetails(500, "Internal Server Error", "Server error")

        // Then
        assertEquals(400, badRequest.status)
        assertEquals(404, notFound.status)
        assertEquals(409, conflict.status)
        assertEquals(500, serverError.status)

        assertEquals("Bad Request", badRequest.error)
        assertEquals("Not Found", notFound.error)
        assertEquals("Conflict", conflict.error)
        assertEquals("Internal Server Error", serverError.error)
    }

    @Test
    fun `should handle special characters in error and message`() {
        // Given
        val errorDetails = ErrorDetails(
            status = 400,
            error = "Erro de Validação (çãõ)",
            message = "Campo 'código' inválido: @#$%"
        )

        // When
        val json = errorDetails.toJSON()

        // Then
        assertTrue(json.contains("çãõ"))
        assertTrue(json.contains("@#$%"))
        assertTrue(json.contains("código"))
    }

    @Test
    fun `should handle long error messages`() {
        // Given
        val longError = "A".repeat(1000)
        val longMessage = "B".repeat(2000)
        val errorDetails = ErrorDetails(
            status = 400,
            error = longError,
            message = longMessage
        )

        // When
        val json = errorDetails.toJSON()

        // Then
        assertNotNull(json)
        assertTrue(json.length > 3000) // Should contain both long strings
        assertTrue(json.contains("\"status\":400"))
    }

    @Test
    fun `should handle multiline messages`() {
        // Given
        val multilineMessage = """
            Error occurred while processing:
            - Line 1: Invalid code format
            - Line 2: Missing required field
            - Line 3: Duplicate entry found
        """.trimIndent()

        val errorDetails = ErrorDetails(
            status = 400,
            error = "Validation Error",
            message = multilineMessage
        )

        // When
        val json = errorDetails.toJSON()

        // Then
        assertNotNull(json)
        assertTrue(json.contains("Line 1"))
        assertTrue(json.contains("Line 2"))
        assertTrue(json.contains("Line 3"))
    }

    @Test
    fun `should handle JSON special characters in message`() {
        // Given
        val messageWithJson = """{"field": "value", "nested": {"key": "data"}}"""
        val errorDetails = ErrorDetails(
            status = 400,
            error = "JSON Error",
            message = messageWithJson
        )

        // When
        val json = errorDetails.toJSON()

        // Then
        assertNotNull(json)
        // The JSON should be properly escaped
        assertTrue(json.contains("\\\"field\\\"") || json.contains("\"field\""))
    }

    @Test
    fun `should create different error types`() {
        // Given & When
        val validationError = ErrorDetails(400, "Validation Error", "Field validation failed")
        val authError = ErrorDetails(401, "Unauthorized", "Invalid credentials")
        val forbiddenError = ErrorDetails(403, "Forbidden", "Access denied")
        val notFoundError = ErrorDetails(404, "Not Found", "Resource not found")
        val conflictError = ErrorDetails(409, "Conflict", "Resource already exists")

        // Then
        assertEquals("Validation Error", validationError.error)
        assertEquals("Unauthorized", authError.error)
        assertEquals("Forbidden", forbiddenError.error)
        assertEquals("Not Found", notFoundError.error)
        assertEquals("Conflict", conflictError.error)
    }

    @Test
    fun `should handle whitespace in error and message`() {
        // Given
        val errorDetails = ErrorDetails(
            status = 400,
            error = "  Bad Request  ",
            message = "  Invalid input data  "
        )

        // When
        val json = errorDetails.toJSON()

        // Then
        assertEquals("  Bad Request  ", errorDetails.error)
        assertEquals("  Invalid input data  ", errorDetails.message)
        assertTrue(json.contains("\"  Bad Request  \""))
        assertTrue(json.contains("\"  Invalid input data  \""))
    }

    @Test
    fun `should handle numeric values in messages`() {
        // Given
        val errorDetails = ErrorDetails(
            status = 400,
            error = "Validation Error",
            message = "Expected value between 1 and 100, got 150. Rate: 99.9%"
        )

        // When
        val json = errorDetails.toJSON()

        // Then
        assertTrue(json.contains("150"))
        assertTrue(json.contains("99.9%"))
        assertTrue(json.contains("between 1 and 100"))
    }

    @Test
    fun `should maintain data consistency in JSON serialization`() {
        // Given
        val errorDetails = ErrorDetails(
            status = 422,
            error = "Unprocessable Entity",
            message = "Data validation failed"
        )

        // When
        val json = errorDetails.toJSON()

        // Then
        assertTrue(json.contains("422"))
        assertTrue(json.contains("Unprocessable Entity"))
        assertTrue(json.contains("Data validation failed"))

        // Verify it's valid JSON structure
        assertTrue(json.startsWith("{"))
        assertTrue(json.endsWith("}"))
        assertTrue(json.contains("\"status\""))
        assertTrue(json.contains("\"error\""))
        assertTrue(json.contains("\"message\""))
    }
}