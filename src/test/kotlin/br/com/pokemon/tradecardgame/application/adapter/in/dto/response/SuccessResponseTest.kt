package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SuccessResponseTest {

    @Test
    fun `should create success response with all fields`() {
        // Given
        val title = "Operation Successful"
        val message = "The operation was completed successfully"
        val timestamp = LocalDateTime.of(2023, 10, 15, 14, 30, 0)

        // When
        val response = SuccessResponse(
            title = title,
            message = message,
            timestamp = timestamp
        )

        // Then
        assertEquals(title, response.title)
        assertEquals(message, response.message)
        assertEquals(timestamp, response.timestamp)
    }

    @Test
    fun `should create success response with default timestamp`() {
        // Given
        val title = "Serie Created"
        val message = "Serie was created successfully"
        val beforeCreation = LocalDateTime.now()

        // When
        val response = SuccessResponse(
            title = title,
            message = message
        )
        val afterCreation = LocalDateTime.now()

        // Then
        assertEquals(title, response.title)
        assertEquals(message, response.message)
        assertNotNull(response.timestamp)

        // Timestamp should be between before and after creation
        assertTrue(response.timestamp!!.isAfter(beforeCreation.minusSeconds(1)))
        assertTrue(response.timestamp!!.isBefore(afterCreation.plusSeconds(1)))
    }

    @Test
    fun `should create success response with null timestamp`() {
        // Given
        val title = "Update Complete"
        val message = "Serie was updated successfully"

        // When
        val response = SuccessResponse(
            title = title,
            message = message,
            timestamp = null
        )

        // Then
        assertEquals(title, response.title)
        assertEquals(message, response.message)
        assertNull(response.timestamp)
    }

    @Test
    fun `should handle empty title and message`() {
        // When
        val response = SuccessResponse(
            title = "",
            message = ""
        )

        // Then
        assertEquals("", response.title)
        assertEquals("", response.message)
        assertNotNull(response.timestamp)
    }

    @Test
    fun `should handle long title and message`() {
        // Given
        val longTitle = "A".repeat(1000)
        val longMessage = "B".repeat(2000)

        // When
        val response = SuccessResponse(
            title = longTitle,
            message = longMessage
        )

        // Then
        assertEquals(longTitle, response.title)
        assertEquals(longMessage, response.message)
        assertEquals(1000, response.title.length)
        assertEquals(2000, response.message.length)
    }

    @Test
    fun `should handle special characters in title and message`() {
        // Given
        val title = "Série Criada com Sucesso! 🎉"
        val message = "A série 'Pokémon TCG' foi criada com êxito às 14:30h"

        // When
        val response = SuccessResponse(
            title = title,
            message = message
        )

        // Then
        assertEquals(title, response.title)
        assertEquals(message, response.message)
        assertTrue(response.title.contains("🎉"))
        assertTrue(response.message.contains("Pokémon"))
    }

    @Test
    fun `should handle multiline messages`() {
        // Given
        val title = "Batch Operation Complete"
        val message = """
            Operation completed successfully:
            - 5 series created
            - 3 series updated
            - 1 series deleted
        """.trimIndent()

        // When
        val response = SuccessResponse(
            title = title,
            message = message
        )

        // Then
        assertEquals(title, response.title)
        assertEquals(message, response.message)
        assertTrue(response.message.contains("\n"))
        assertTrue(response.message.contains("5 series created"))
    }

    @Test
    fun `should create response for different operation types`() {
        // Given & When
        val createResponse = SuccessResponse(
            title = "Serie Created",
            message = "Serie with code SV01 was created successfully"
        )

        val updateResponse = SuccessResponse(
            title = "Serie Updated",
            message = "Serie with ID 123 was updated successfully"
        )

        val deleteResponse = SuccessResponse(
            title = "Serie Deleted",
            message = "Serie with ID 456 was deleted successfully"
        )

        // Then
        assertEquals("Serie Created", createResponse.title)
        assertEquals("Serie Updated", updateResponse.title)
        assertEquals("Serie Deleted", deleteResponse.title)

        assertTrue(createResponse.message.contains("created"))
        assertTrue(updateResponse.message.contains("updated"))
        assertTrue(deleteResponse.message.contains("deleted"))
    }

    @Test
    fun `should handle whitespace in title and message`() {
        // Given
        val title = "  Success  "
        val message = "  Operation completed  "

        // When
        val response = SuccessResponse(
            title = title,
            message = message
        )

        // Then
        assertEquals("  Success  ", response.title) // preserves whitespace
        assertEquals("  Operation completed  ", response.message) // preserves whitespace
    }

    @Test
    fun `should create response with specific timestamp`() {
        // Given
        val specificTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0)
        val title = "New Year Success"
        val message = "Operation completed at midnight"

        // When
        val response = SuccessResponse(
            title = title,
            message = message,
            timestamp = specificTime
        )

        // Then
        assertEquals(title, response.title)
        assertEquals(message, response.message)
        assertEquals(specificTime, response.timestamp)
        assertEquals(2024, response.timestamp!!.year)
        assertEquals(1, response.timestamp!!.monthValue)
        assertEquals(1, response.timestamp!!.dayOfMonth)
    }

    @Test
    fun `should handle JSON-like messages`() {
        // Given
        val title = "API Response"
        val message = """{"status": "success", "data": {"id": 123, "name": "Test Serie"}}"""

        // When
        val response = SuccessResponse(
            title = title,
            message = message
        )

        // Then
        assertEquals(title, response.title)
        assertEquals(message, response.message)
        assertTrue(response.message.contains("\"status\": \"success\""))
        assertTrue(response.message.contains("\"id\": 123"))
    }

    @Test
    fun `should create multiple responses with different timestamps`() {
        // Given
        val timestamp1 = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val timestamp2 = LocalDateTime.of(2023, 1, 1, 11, 0, 0)

        // When
        val response1 = SuccessResponse("First", "First operation", timestamp1)
        val response2 = SuccessResponse("Second", "Second operation", timestamp2)

        // Then
        assertEquals(timestamp1, response1.timestamp)
        assertEquals(timestamp2, response2.timestamp)
        assertTrue(response2.timestamp!!.isAfter(response1.timestamp))
    }

    @Test
    fun `should handle numeric values in messages`() {
        // Given
        val title = "Batch Processing Complete"
        val message = "Processed 1,234 records in 45.67 seconds with 99.9% success rate"

        // When
        val response = SuccessResponse(
            title = title,
            message = message
        )

        // Then
        assertEquals(title, response.title)
        assertEquals(message, response.message)
        assertTrue(response.message.contains("1,234"))
        assertTrue(response.message.contains("45.67"))
        assertTrue(response.message.contains("99.9%"))
    }
}