package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response

import br.com.pokemon.tradecardgame.domain.model.Expansion
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SerieResponseTest {

    @Test
    fun `should create serie response with all fields`() {
        // Given
        val id = UUID.randomUUID()
        val code = "SV01"
        val name = "Scarlet & Violet Base Set"
        val releaseYear = 2023
        val imageUrl = "https://example.com/sv01.jpg"
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "BASE", "Base Set"),
            Expansion(UUID.randomUUID(), "PROMO", "Promo Cards")
        )
        val createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val updatedAt = LocalDateTime.of(2023, 6, 1, 15, 30, 0)

        // When
        val response = SerieResponse(
            id = id,
            code = code,
            name = name,
            releaseYear = releaseYear,
            imageUrl = imageUrl,
            expansions = expansions,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // Then
        assertEquals(id, response.id)
        assertEquals(code, response.code)
        assertEquals(name, response.name)
        assertEquals(releaseYear, response.releaseYear)
        assertEquals(imageUrl, response.imageUrl)
        assertEquals(2, response.expansions.size)
        assertEquals("BASE", response.expansions[0].code)
        assertEquals("PROMO", response.expansions[1].code)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
    }

    @Test
    fun `should create serie response with minimal fields`() {
        // Given
        val id = UUID.randomUUID()
        val code = "SV02"
        val name = "Paldea Evolved"
        val releaseYear = 2023

        // When
        val response = SerieResponse(
            id = id,
            code = code,
            name = name,
            releaseYear = releaseYear
        )

        // Then
        assertEquals(id, response.id)
        assertEquals(code, response.code)
        assertEquals(name, response.name)
        assertEquals(releaseYear, response.releaseYear)
        assertNull(response.imageUrl)
        assertTrue(response.expansions.isEmpty())
        assertNotNull(response.createdAt) // default value
        assertNull(response.updatedAt)
    }

    @Test
    fun `should create serie response with null id`() {
        // When
        val response = SerieResponse(
            id = null,
            code = "NEW01",
            name = "New Serie",
            releaseYear = 2024
        )

        // Then
        assertNull(response.id)
        assertEquals("NEW01", response.code)
        assertEquals("New Serie", response.name)
        assertEquals(2024, response.releaseYear)
    }

    @Test
    fun `should create serie response with null imageUrl`() {
        // When
        val response = SerieResponse(
            id = UUID.randomUUID(),
            code = "NO_IMG",
            name = "No Image Serie",
            releaseYear = 2023,
            imageUrl = null
        )

        // Then
        assertNull(response.imageUrl)
        assertEquals("NO_IMG", response.code)
        assertEquals("No Image Serie", response.name)
    }

    @Test
    fun `should create serie response with empty expansions`() {
        // When
        val response = SerieResponse(
            id = UUID.randomUUID(),
            code = "EMPTY",
            name = "Empty Expansions",
            releaseYear = 2023,
            expansions = emptyList()
        )

        // Then
        assertTrue(response.expansions.isEmpty())
        assertEquals(0, response.expansions.size)
    }

    @Test
    fun `should create serie response with multiple expansions`() {
        // Given
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "BASE", "Base Set"),
            Expansion(UUID.randomUUID(), "PROMO", "Promo Cards"),
            Expansion(UUID.randomUUID(), "SPECIAL", "Special Edition"),
            Expansion(UUID.randomUUID(), "LIMITED", "Limited Edition")
        )

        // When
        val response = SerieResponse(
            id = UUID.randomUUID(),
            code = "MULTI",
            name = "Multiple Expansions Serie",
            releaseYear = 2023,
            expansions = expansions
        )

        // Then
        assertEquals(4, response.expansions.size)
        assertEquals("BASE", response.expansions[0].code)
        assertEquals("PROMO", response.expansions[1].code)
        assertEquals("SPECIAL", response.expansions[2].code)
        assertEquals("LIMITED", response.expansions[3].code)
    }

    @Test
    fun `should handle default createdAt timestamp`() {
        // Given
        val beforeCreation = LocalDateTime.now()

        // When
        val response = SerieResponse(
            id = UUID.randomUUID(),
            code = "TIME_TEST",
            name = "Timestamp Test",
            releaseYear = 2023
        )
        val afterCreation = LocalDateTime.now()

        // Then
        assertNotNull(response.createdAt)
        assertTrue(response.createdAt!!.isAfter(beforeCreation.minusSeconds(1)))
        assertTrue(response.createdAt!!.isBefore(afterCreation.plusSeconds(1)))
    }

    @Test
    fun `should handle null timestamps`() {
        // When
        val response = SerieResponse(
            id = UUID.randomUUID(),
            code = "NULL_TIME",
            name = "Null Timestamps",
            releaseYear = 2023,
            createdAt = null,
            updatedAt = null
        )

        // Then
        assertNull(response.createdAt)
        assertNull(response.updatedAt)
    }

    @Test
    fun `should handle specific timestamps`() {
        // Given
        val createdAt = LocalDateTime.of(2023, 1, 15, 9, 30, 0)
        val updatedAt = LocalDateTime.of(2023, 6, 20, 14, 45, 30)

        // When
        val response = SerieResponse(
            id = UUID.randomUUID(),
            code = "SPECIFIC",
            name = "Specific Timestamps",
            releaseYear = 2023,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // Then
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
        assertTrue(response.updatedAt!!.isAfter(response.createdAt))
    }

    @Test
    fun `should handle different release years`() {
        // Given & When
        val oldSerie = SerieResponse(
            id = UUID.randomUUID(),
            code = "OLD99",
            name = "Old Serie",
            releaseYear = 1999
        )

        val newSerie = SerieResponse(
            id = UUID.randomUUID(),
            code = "NEW24",
            name = "New Serie",
            releaseYear = 2024
        )

        // Then
        assertEquals(1999, oldSerie.releaseYear)
        assertEquals(2024, newSerie.releaseYear)
    }

    @Test
    fun `should handle special characters in name and code`() {
        // When
        val response = SerieResponse(
            id = UUID.randomUUID(),
            code = "SV-01_SPECIAL",
            name = "Scarlet & Violet: Special Edition (Pokémon TCG)",
            releaseYear = 2023
        )

        // Then
        assertEquals("SV-01_SPECIAL", response.code)
        assertEquals("Scarlet & Violet: Special Edition (Pokémon TCG)", response.name)
        assertTrue(response.code.contains("-"))
        assertTrue(response.code.contains("_"))
        assertTrue(response.name.contains("&"))
        assertTrue(response.name.contains(":"))
        assertTrue(response.name.contains("("))
        assertTrue(response.name.contains(")"))
    }

    @Test
    fun `should handle long URLs`() {
        // Given
        val longUrl =
            "https://example.com/very/long/path/to/image/with/many/subdirectories/and/parameters?param1=value1&param2=value2&param3=value3.jpg"

        // When
        val response = SerieResponse(
            id = UUID.randomUUID(),
            code = "LONG_URL",
            name = "Long URL Serie",
            releaseYear = 2023,
            imageUrl = longUrl
        )

        // Then
        assertEquals(longUrl, response.imageUrl)
        assertTrue(response.imageUrl!!.length > 100)
        assertTrue(response.imageUrl!!.startsWith("https://"))
        assertTrue(response.imageUrl!!.endsWith(".jpg"))
    }

    @Test
    fun `should handle different UUID formats`() {
        // Given
        val randomUUID = UUID.randomUUID()
        val specificUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

        // When
        val response1 = SerieResponse(
            id = randomUUID,
            code = "UUID1",
            name = "Random UUID",
            releaseYear = 2023
        )

        val response2 = SerieResponse(
            id = specificUUID,
            code = "UUID2",
            name = "Specific UUID",
            releaseYear = 2023
        )

        // Then
        assertEquals(randomUUID, response1.id)
        assertEquals(specificUUID, response2.id)
        assertEquals("123e4567-e89b-12d3-a456-426614174000", response2.id.toString())
    }

    @Test
    fun `should handle expansions with different properties`() {
        // Given
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "SHORT", "S"),
            Expansion(UUID.randomUUID(), "VERY_LONG_CODE_NAME", "Very Long Expansion Name With Many Words"),
            Expansion(UUID.randomUUID(), "NUM123", "Numeric 123 Expansion")
        )

        // When
        val response = SerieResponse(
            id = UUID.randomUUID(),
            code = "EXP_TEST",
            name = "Expansion Test Serie",
            releaseYear = 2023,
            expansions = expansions
        )

        // Then
        assertEquals(3, response.expansions.size)
        assertEquals("SHORT", response.expansions[0].code)
        assertEquals("S", response.expansions[0].name)
        assertEquals("VERY_LONG_CODE_NAME", response.expansions[1].code)
        assertEquals("Very Long Expansion Name With Many Words", response.expansions[1].name)
        assertEquals("NUM123", response.expansions[2].code)
        assertEquals("NUM123", response.expansions[2].code)
    }

    @Test
    fun `should preserve all data integrity`() {
        // Given
        val id = UUID.randomUUID()
        val code = "INTEGRITY"
        val name = "Data Integrity Test"
        val releaseYear = 2023
        val imageUrl = "https://test.com/image.jpg"
        val expansions = listOf(Expansion(UUID.randomUUID(), "TEST", "Test Expansion"))
        val createdAt = LocalDateTime.now().minusDays(1)
        val updatedAt = LocalDateTime.now()

        // When
        val response = SerieResponse(
            id = id,
            code = code,
            name = name,
            releaseYear = releaseYear,
            imageUrl = imageUrl,
            expansions = expansions,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // Then - All data should be preserved exactly
        assertEquals(id, response.id)
        assertEquals(code, response.code)
        assertEquals(name, response.name)
        assertEquals(releaseYear, response.releaseYear)
        assertEquals(imageUrl, response.imageUrl)
        assertEquals(expansions, response.expansions)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
    }
}