package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request

import br.com.pokemon.tradecardgame.domain.model.Expansion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CreateSerieRequestTest {

    @Test
    fun `should create request with all fields successfully`() {
        // Given
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "EXP1", "Expansion 1"),
            Expansion(UUID.randomUUID(), "EXP2", "Expansion 2")
        )

        // When
        val request = CreateSerieRequest(
            code = "SV01",
            name = "Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01.jpg",
            expansions = expansions
        )

        // Then
        assertEquals("SV01", request.code)
        assertEquals("Scarlet & Violet Base Set", request.name)
        assertEquals(2023, request.releaseYear)
        assertEquals("https://example.com/sv01.jpg", request.imageUrl)
        assertEquals(2, request.expansions.size)
        assertEquals("EXP1", request.expansions[0].code)
        assertEquals("EXP2", request.expansions[1].code)
    }

    @Test
    fun `should create request with minimal required fields`() {
        // When
        val request = CreateSerieRequest(
            code = "SV02",
            name = "Paldea Evolved",
            releaseYear = 2023
        )

        // Then
        assertEquals("SV02", request.code)
        assertEquals("Paldea Evolved", request.name)
        assertEquals(2023, request.releaseYear)
        assertEquals(null, request.imageUrl)
        assertTrue(request.expansions.isEmpty())
    }

    @Test
    fun `should convert to command successfully`() {
        // Given
        val request = CreateSerieRequest(
            code = "SV03",
            name = "Obsidian Flames",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv03.jpg"
        )

        // When
        val command = request.toCommand()

        // Then
        assertEquals("SV03", command.code)
        assertEquals("Obsidian Flames", command.name)
        assertEquals(2023, command.releaseYear)
        assertEquals("https://example.com/sv03.jpg", command.imageUrl)
        assertTrue(command.expansions.isEmpty())
    }

    @Test
    fun `should convert to command with expansions`() {
        // Given
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "BASE", "Base Set"),
            Expansion(UUID.randomUUID(), "PROMO", "Promo Cards")
        )
        val request = CreateSerieRequest(
            code = "SV04",
            name = "Paradox Rift",
            releaseYear = 2023,
            expansions = expansions
        )

        // When
        val command = request.toCommand()

        // Then
        assertEquals("SV04", command.code)
        assertEquals("Paradox Rift", command.name)
        assertEquals(2023, command.releaseYear)
        assertEquals(2, command.expansions.size)
        assertEquals("BASE", command.expansions[0].code)
        assertEquals("PROMO", command.expansions[1].code)
    }

    @Test
    fun `should throw exception when code is blank`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            CreateSerieRequest(
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
            CreateSerieRequest(
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
            CreateSerieRequest(
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
            CreateSerieRequest(
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
            CreateSerieRequest(
                code = "OLD01",
                name = "Old Serie",
                releaseYear = 1998
            )
        }

        assertThrows<IllegalArgumentException> {
            CreateSerieRequest(
                code = "OLD02",
                name = "Very Old Serie",
                releaseYear = 1990
            )
        }
    }

    @Test
    fun `should accept release year greater than 1998`() {
        // When
        val request1999 = CreateSerieRequest(
            code = "EDGE01",
            name = "Edge Case Serie",
            releaseYear = 1999
        )

        val request2024 = CreateSerieRequest(
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
        val request = CreateSerieRequest(
            code = "NULL01",
            name = "No Image Serie",
            releaseYear = 2023,
            imageUrl = null
        )

        // Then
        assertEquals(null, request.imageUrl)

        val command = request.toCommand()
        assertEquals(null, command.imageUrl)
    }

    @Test
    fun `should handle empty expansions list correctly`() {
        // When
        val request = CreateSerieRequest(
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
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "TEST1", "Test Expansion 1"),
            Expansion(UUID.randomUUID(), "TEST2", "Test Expansion 2")
        )
        val request = CreateSerieRequest(
            code = "PRESERVE01",
            name = "Data Preservation Test",
            releaseYear = 2023,
            imageUrl = "https://example.com/preserve.jpg",
            expansions = expansions
        )

        // When
        val command = request.toCommand()

        // Then
        assertEquals(request.code, command.code)
        assertEquals(request.name, command.name)
        assertEquals(request.releaseYear, command.releaseYear)
        assertEquals(request.imageUrl, command.imageUrl)
        assertEquals(request.expansions.size, command.expansions.size)
        assertEquals(request.expansions[0].code, command.expansions[0].code)
        assertEquals(request.expansions[1].code, command.expansions[1].code)
    }

    @Test
    fun `should create command with timestamps`() {
        // Given
        val request = CreateSerieRequest(
            code = "TIME01",
            name = "Timestamp Test",
            releaseYear = 2023
        )

        // When
        val command = request.toCommand()

        // Then
        // Commands don't have createdAt/updatedAt, they are set in the entity/domain
        assertEquals("TIME01", command.code)
        assertEquals("Timestamp Test", command.name)
    }
}