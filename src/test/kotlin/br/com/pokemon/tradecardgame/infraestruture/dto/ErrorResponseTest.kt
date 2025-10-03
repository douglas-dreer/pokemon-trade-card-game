package br.com.pokemon.tradecardgame.infraestruture.dto

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorResponseTest {

    @Test
    fun `should create error response with all fields`() {
        // Given
        val error = "Bad Request"
        val message = "Invalid input data"
        val status = 400
        val timestamp = LocalDateTime.of(2023, 10, 15, 14, 30, 0)
        val details = listOf("Field 'name' is required", "Field 'code' must be unique")

        // When
        val errorResponse = ErrorResponse(
            error = error,
            message = message,
            status = status,
            timestamp = timestamp,
            details = details
        )

        // Then
        assertEquals(error, errorResponse.error)
        assertEquals(message, errorResponse.message)
        assertEquals(status, errorResponse.status)
        assertEquals(timestamp, errorResponse.timestamp)
        assertEquals(2, errorResponse.details.size)
        assertEquals("Field 'name' is required", errorResponse.details[0])
        assertEquals("Field 'code' must be unique", errorResponse.details[1])
    }

    @Test
    fun `should create error response with minimal fields`() {
        // Given
        val error = "Not Found"
        val message = "Resource not found"
        val status = 404
        val timestamp = LocalDateTime.now()

        // When
        val errorResponse = ErrorResponse(
            error = error,
            message = message,
            status = status,
            timestamp = timestamp
        )

        // Then
        assertEquals(error, errorResponse.error)
        assertEquals(message, errorResponse.message)
        assertEquals(status, errorResponse.status)
        assertEquals(timestamp, errorResponse.timestamp)
        assertTrue(errorResponse.details.isEmpty())
    }

    @Test
    fun `should create error response with empty details list`() {
        // Given
        val error = "Internal Server Error"
        val message = "An unexpected error occurred"
        val status = 500
        val timestamp = LocalDateTime.now()
        val details = emptyList<String>()

        // When
        val errorResponse = ErrorResponse(
            error = error,
            message = message,
            status = status,
            timestamp = timestamp,
            details = details
        )

        // Then
        assertEquals(error, errorResponse.error)
        assertEquals(message, errorResponse.message)
        assertEquals(status, errorResponse.status)
        assertEquals(timestamp, errorResponse.timestamp)
        assertTrue(errorResponse.details.isEmpty())
    }

    @Test
    fun `should handle different HTTP status codes`() {
        // Given
        val timestamp = LocalDateTime.now()

        val badRequest = ErrorResponse("Bad Request", "Invalid data", 400, timestamp)
        val notFound = ErrorResponse("Not Found", "Resource not found", 404, timestamp)
        val conflict = ErrorResponse("Conflict", "Resource already exists", 409, timestamp)
        val serverError = ErrorResponse("Internal Server Error", "Server error", 500, timestamp)

        // Then
        assertEquals(400, badRequest.status)
        assertEquals(404, notFound.status)
        assertEquals(409, conflict.status)
        assertEquals(500, serverError.status)
    }

    @Test
    fun `should handle long error messages`() {
        // Given
        val longError = "A".repeat(1000)
        val longMessage = "B".repeat(2000)
        val status = 400
        val timestamp = LocalDateTime.now()

        // When
        val errorResponse = ErrorResponse(
            error = longError,
            message = longMessage,
            status = status,
            timestamp = timestamp
        )

        // Then
        assertEquals(longError, errorResponse.error)
        assertEquals(longMessage, errorResponse.message)
        assertEquals(1000, errorResponse.error.length)
        assertEquals(2000, errorResponse.message.length)
    }

    @Test
    fun `should handle special characters in error messages`() {
        // Given
        val error = "Erro de Validação: Caracteres Especiais (çãõ)"
        val message = "O campo 'código' contém caracteres inválidos: @#$%"
        val status = 400
        val timestamp = LocalDateTime.now()

        // When
        val errorResponse = ErrorResponse(
            error = error,
            message = message,
            status = status,
            timestamp = timestamp
        )

        // Then
        assertEquals(error, errorResponse.error)
        assertEquals(message, errorResponse.message)
        assertTrue(errorResponse.error.contains("ç"))
        assertTrue(errorResponse.error.contains("ã"))
        assertTrue(errorResponse.message.contains("@#$%"))
    }

    @Test
    fun `should handle multiple details with different lengths`() {
        // Given
        val details = listOf(
            "Short error",
            "This is a much longer error message that provides more detailed information about what went wrong",
            "",
            "Error with special chars: çãõ@#$%"
        )
        val timestamp = LocalDateTime.now()

        // When
        val errorResponse = ErrorResponse(
            error = "Validation Error",
            message = "Multiple validation errors occurred",
            status = 400,
            timestamp = timestamp,
            details = details
        )

        // Then
        assertEquals(4, errorResponse.details.size)
        assertEquals("Short error", errorResponse.details[0])
        assertTrue(errorResponse.details[1].length > 50)
        assertEquals("", errorResponse.details[2])
        assertTrue(errorResponse.details[3].contains("çãõ"))
    }

    @Test
    fun `should preserve timestamp precision`() {
        // Given
        val timestamp = LocalDateTime.of(2023, 12, 25, 23, 59, 59, 999999999)

        // When
        val errorResponse = ErrorResponse(
            error = "Test Error",
            message = "Test message",
            status = 400,
            timestamp = timestamp
        )

        // Then
        assertEquals(timestamp, errorResponse.timestamp)
        assertEquals(2023, errorResponse.timestamp.year)
        assertEquals(12, errorResponse.timestamp.monthValue)
        assertEquals(25, errorResponse.timestamp.dayOfMonth)
        assertEquals(23, errorResponse.timestamp.hour)
        assertEquals(59, errorResponse.timestamp.minute)
        assertEquals(59, errorResponse.timestamp.second)
        assertEquals(999999999, errorResponse.timestamp.nano)
    }

    @Test
    fun `should handle empty strings in error and message`() {
        // Given
        val error = ""
        val message = ""
        val status = 400
        val timestamp = LocalDateTime.now()

        // When
        val errorResponse = ErrorResponse(
            error = error,
            message = message,
            status = status,
            timestamp = timestamp
        )

        // Then
        assertEquals("", errorResponse.error)
        assertEquals("", errorResponse.message)
        assertEquals(status, errorResponse.status)
        assertEquals(timestamp, errorResponse.timestamp)
    }

    @Test
    fun `should maintain data integrity across multiple instances`() {
        // Given
        val timestamp1 = LocalDateTime.now()
        val timestamp2 = timestamp1.plusMinutes(1)

        val error1 = ErrorResponse("Error 1", "Message 1", 400, timestamp1, listOf("Detail 1"))
        val error2 = ErrorResponse("Error 2", "Message 2", 404, timestamp2, listOf("Detail 2"))

        // Then
        assertEquals("Error 1", error1.error)
        assertEquals("Error 2", error2.error)
        assertEquals("Message 1", error1.message)
        assertEquals("Message 2", error2.message)
        assertEquals(400, error1.status)
        assertEquals(404, error2.status)
        assertEquals(timestamp1, error1.timestamp)
        assertEquals(timestamp2, error2.timestamp)
        assertEquals("Detail 1", error1.details[0])
        assertEquals("Detail 2", error2.details[0])
    }
}