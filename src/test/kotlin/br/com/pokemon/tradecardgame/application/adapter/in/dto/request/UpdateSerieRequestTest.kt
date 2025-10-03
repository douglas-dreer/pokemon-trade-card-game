package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request

import br.com.pokemon.tradecardgame.domain.model.Expansion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdateSerieRequestTest {

    @Test
    fun `should create request with all fields successfully`() {
        // Given
        val serieId = UUID.randomUUID()
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "EXP1", "Updated Expansion 1"),
            Expansion(UUID.randomUUID(), "EXP2", "Updated Expansion 2")
        )

        // When
        val request = UpdateSerieRequest(
            id = serieId,
            code = "SV01_UPD",
            name = "Updated Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01_updated.jpg",
            expansions = expansions
        )

        // Then
        assertEquals(serieId, request.id)
        assertEquals("SV01_UPD", request.code)
        assertEquals("Updated Scarlet & Violet Base Set", request.name)
        assertEquals(2023, request.releaseYear)
        assertEquals("https://example.com/sv01_updated.jpg", request.imageUrl)
        assertEquals(2, request.expansions.size)
        assertEquals("EXP1", request.expansions[0].code)
        assertEquals("EXP2", request.expansions[1].code)
    }

    @Test
    fun `should create request with minimal required fields`() {
        // Given
        val serieId = UUID.randomUUID()

        // When
        val request = UpdateSerieRequest(
            id = serieId,
            code = "SV02_UPD",
            name = "Updated Paldea Evolved",
            releaseYear = 2023
        )

        // Then
        assertEquals(serieId, request.id)
        assertEquals("SV02_UPD", request.code)
        assertEquals("Updated Paldea Evolved", request.name)
        assertEquals(2023, request.releaseYear)
        assertNull(request.imageUrl)
        assertTrue(request.expansions.isEmpty())
    }

    @Test
    fun `should create request with null id`() {
        // When
        val request = UpdateSerieRequest(
            id = null,
            code = "NULL_ID",
            name = "Null ID Serie",
            releaseYear = 2023
        )

        // Then
        assertNull(request.id)
        assertEquals("NULL_ID", request.code)
        assertEquals("Null ID Serie", request.name)
    }

    @Test
    fun `should convert to command successfully`() {
        // Given
        val serieId = UUID.randomUUID()
        val request = UpdateSerieRequest(
            id = serieId,
            code = "SV03_UPD",
            name = "Updated Obsidian Flames",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv03_updated.jpg"
        )

        // When
        val command = request.toCommand()

        // Then
        assertEquals(serieId, command.id)
        assertEquals("SV03_UPD", command.code)
        assertEquals("Updated Obsidian Flames", command.name)
        assertEquals(2023, command.releaseYear)
        assertEquals("https://example.com/sv03_updated.jpg", command.imageUrl)
        assertTrue(command.expansions.isEmpty())
    }

    @Test
    fun `should convert to command with expansions`() {
        // Given
        val serieId = UUID.randomUUID()
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "BASE_UPD", "Updated Base Set"),
            Expansion(UUID.randomUUID(), "PROMO_UPD", "Updated Promo Cards")
        )
        val request = UpdateSerieRequest(
            id = serieId,
            code = "SV04_UPD",
            name = "Updated Paradox Rift",
            releaseYear = 2023,
            expansions = expansions
        )

        // When
        val command = request.toCommand()

        // Then
        assertEquals(serieId, command.id)
        assertEquals("SV04_UPD", command.code)
        assertEquals("Updated Paradox Rift", command.name)
        assertEquals(2023, command.releaseYear)
        assertEquals(2, command.expansions.size)
        assertEquals("BASE_UPD", command.expansions[0].code)
        assertEquals("PROMO_UPD", command.expansions[1].code)
    }

    @Test
    fun `should throw exception when code is blank`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            UpdateSerieRequest(
                id = UUID.randomUUID(),
                code = "",
                name = "Valid Name",
                releaseYear = 2023
            )
        }
    }

    @Test
    fun `should throw exception when code is only whitespace`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            UpdateSerieRequest(
                id = UUID.randomUUID(),
                code = "   ",
                name = "Valid Name",
                releaseYear = 2023
            )
        }
    }

    @Test
    fun `should throw exception when name is blank`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            UpdateSerieRequest(
                id = UUID.randomUUID(),
                code = "VALID01",
                name = "",
                releaseYear = 2023
            )
        }
    }

    @Test
    fun `should throw exception when name is only whitespace`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            UpdateSerieRequest(
                id = UUID.randomUUID(),
                code = "VALID01",
                name = "   ",
                releaseYear = 2023
            )
        }
    }

    @Test
    fun `should throw exception when release year is 1998 or less`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            UpdateSerieRequest(
                id = UUID.randomUUID(),
                code = "OLD01",
                name = "Old Serie",
                releaseYear = 1998
            )
        }

        assertThrows<IllegalArgumentException> {
            UpdateSerieRequest(
                id = UUID.randomUUID(),
                code = "OLD02",
                name = "Very Old Serie",
                releaseYear = 1990
            )
        }
    }

    @Test
    fun `should accept release year greater than 1998`() {
        // When
        val request1999 = UpdateSerieRequest(
            id = UUID.randomUUID(),
            code = "EDGE01",
            name = "Edge Case Serie",
            releaseYear = 1999
        )

        val request2024 = UpdateSerieRequest(
            id = UUID.randomUUID(),
            code = "NEW01",
            name = "New Serie",
            releaseYear = 2024
        )

        // Then
        assertEquals(1999, request1999.releaseYear)
        assertEquals(2024, request2024.releaseYear)
    }

    @Test
    fun `should handle null imageUrl correctly`() {
        // When
        val request = UpdateSerieRequest(
            id = UUID.randomUUID(),
            code = "NULL01",
            name = "No Image Serie",
            releaseYear = 2023,
            imageUrl = null
        )

        // Then
        assertNull(request.imageUrl)

        val command = request.toCommand()
        assertNull(command.imageUrl)
    }

    @Test
    fun `should handle empty expansions list correctly`() {
        // When
        val request = UpdateSerieRequest(
            id = UUID.randomUUID(),
            code = "EMPTY01",
            name = "Empty Expansions Serie",
            releaseYear = 2023,
            expansions = emptyList()
        )

        // Then
        assertTrue(request.expansions.isEmpty())

        val command = request.toCommand()
        assertTrue(command.expansions.isEmpty())
    }

    @Test
    fun `should preserve all data in command conversion`() {
        // Given
        val serieId = UUID.randomUUID()
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "TEST1", "Test Expansion 1"),
            Expansion(UUID.randomUUID(), "TEST2", "Test Expansion 2")
        )
        val request = UpdateSerieRequest(
            id = serieId,
            code = "PRESERVE01",
            name = "Data Preservation Test",
            releaseYear = 2023,
            imageUrl = "https://example.com/preserve.jpg",
            expansions = expansions
        )

        // When
        val command = request.toCommand()

        // Then
        assertEquals(request.id, command.id)
        assertEquals(request.code, command.code)
        assertEquals(request.name, command.name)
        assertEquals(request.releaseYear, command.releaseYear)
        assertEquals(request.imageUrl, command.imageUrl)
        assertEquals(request.expansions.size, command.expansions.size)
        assertEquals(request.expansions[0].code, command.expansions[0].code)
        assertEquals(request.expansions[1].code, command.expansions[1].code)
    }

    @Test
    fun `should create command with updated timestamp`() {
        // Given
        val request = UpdateSerieRequest(
            id = UUID.randomUUID(),
            code = "TIME01",
            name = "Timestamp Test",
            releaseYear = 2023
        )

        // When
        val command = request.toCommand()

        // Then
        // Commands don't have updatedAt, it's set in the entity/domain
        assertEquals("TIME01", command.code)
        assertEquals("Timestamp Test", command.name)
    }

    @Test
    fun `should handle null id in command conversion`() {
        // Given
        val request = UpdateSerieRequest(
            id = null,
            code = "NULL_CMD",
            name = "Null Command Test",
            releaseYear = 2023
        )

        // When
        val command = request.toCommand()

        // Then
        assertNull(command.id)
        assertEquals("NULL_CMD", command.code)
        assertEquals("Null Command Test", command.name)
    }

    @Test
    fun `should validate with different UUID formats`() {
        // Given
        val randomUUID = UUID.randomUUID()
        val specificUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

        // When
        val request1 = UpdateSerieRequest(
            id = randomUUID,
            code = "UUID1",
            name = "Random UUID Test",
            releaseYear = 2023
        )

        val request2 = UpdateSerieRequest(
            id = specificUUID,
            code = "UUID2",
            name = "Specific UUID Test",
            releaseYear = 2023
        )

        // Then
        assertEquals(randomUUID, request1.id)
        assertEquals(specificUUID, request2.id)

        val command1 = request1.toCommand()
        val command2 = request2.toCommand()

        assertEquals(randomUUID, command1.id)
        assertEquals(specificUUID, command2.id)
    }
}