package br.com.pokemon.tradecardgame.integration.testdata

import br.com.pokemon.tradecardgame.domain.model.Expansion
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SerieTestDataBuilderTest {

    @Test
    fun `should create builder with default values`() {
        // When
        val builder = SerieTestDataBuilder()
        val domain = builder.buildDomain()

        // Then
        assertNotNull(domain.id)
        assertTrue(domain.code.startsWith("TEST"))
        assertTrue(domain.name.startsWith("Test Serie"))
        assertEquals(2024, domain.releaseYear)
        assertEquals("https://example.com/test-serie-image.jpg", domain.imageUrl)
        assertTrue(domain.expansions.isEmpty())
        assertNotNull(domain.createdAt)
        assertNull(domain.updatedAt)
    }

    @Test
    fun `should build domain object with custom values`() {
        // Given
        val customId = UUID.randomUUID()
        val customCreatedAt = LocalDateTime.of(2023, 1, 1, 12, 0)
        val customUpdatedAt = LocalDateTime.of(2023, 6, 1, 15, 30)
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "EXP1", "Expansion 1"),
            Expansion(UUID.randomUUID(), "EXP2", "Expansion 2")
        )

        // When
        val domain = SerieTestDataBuilder()
            .withId(customId)
            .withCode("CUSTOM01")
            .withName("Custom Serie Name")
            .withReleaseYear(2023)
            .withImageUrl("https://custom.com/image.jpg")
            .withExpansions(expansions)
            .withCreatedAt(customCreatedAt)
            .withUpdatedAt(customUpdatedAt)
            .buildDomain()

        // Then
        assertEquals(customId, domain.id)
        assertEquals("CUSTOM01", domain.code)
        assertEquals("Custom Serie Name", domain.name)
        assertEquals(2023, domain.releaseYear)
        assertEquals("https://custom.com/image.jpg", domain.imageUrl)
        assertEquals(2, domain.expansions.size)
        assertEquals("EXP1", domain.expansions[0].code)
        assertEquals("EXP2", domain.expansions[1].code)
        assertEquals(customCreatedAt, domain.createdAt)
        assertEquals(customUpdatedAt, domain.updatedAt)
    }

    @Test
    fun `should build entity with custom values`() {
        // Given
        val customId = UUID.randomUUID()

        // When
        val entity = SerieTestDataBuilder()
            .withId(customId)
            .withCode("ENTITY01")
            .withName("Entity Serie")
            .withReleaseYear(2022)
            .withImageUrl(null)
            .buildEntity()

        // Then
        assertEquals(customId, entity.id)
        assertEquals("ENTITY01", entity.code)
        assertEquals("Entity Serie", entity.name)
        assertEquals(2022, entity.releaseYear)
        assertNull(entity.imageUrl)
        assertNull(entity.expansions) // SerieEntity uses String for expansions
        assertNotNull(entity.createdAt)
        assertNull(entity.updatedAt)
    }

    @Test
    fun `should build create request with custom values`() {
        // Given
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "REQ1", "Request Expansion 1")
        )

        // When
        val request = SerieTestDataBuilder()
            .withCode("REQUEST01")
            .withName("Request Serie")
            .withReleaseYear(2021)
            .withImageUrl("https://request.com/image.jpg")
            .withExpansions(expansions)
            .buildCreateRequest()

        // Then
        assertEquals("REQUEST01", request.code)
        assertEquals("Request Serie", request.name)
        assertEquals(2021, request.releaseYear)
        assertEquals("https://request.com/image.jpg", request.imageUrl)
        assertEquals(1, request.expansions.size)
        assertEquals("REQ1", request.expansions[0].code)
    }

    @Test
    fun `should build update request with custom values`() {
        // Given
        val customId = UUID.randomUUID()
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "UPD1", "Update Expansion 1")
        )

        // When
        val request = SerieTestDataBuilder()
            .withId(customId)
            .withCode("UPDATE01")
            .withName("Update Serie")
            .withReleaseYear(2020)
            .withImageUrl("https://update.com/image.jpg")
            .withExpansions(expansions)
            .buildUpdateRequest()

        // Then
        assertEquals(customId, request.id)
        assertEquals("UPDATE01", request.code)
        assertEquals("Update Serie", request.name)
        assertEquals(2020, request.releaseYear)
        assertEquals("https://update.com/image.jpg", request.imageUrl)
        assertEquals(1, request.expansions.size)
        assertEquals("UPD1", request.expansions[0].code)
    }

    @Test
    fun `should support method chaining`() {
        // When
        val domain = SerieTestDataBuilder()
            .withCode("CHAIN01")
            .withName("Chained Serie")
            .withReleaseYear(2019)
            .withImageUrl("https://chain.com/image.jpg")
            .buildDomain()

        // Then
        assertEquals("CHAIN01", domain.code)
        assertEquals("Chained Serie", domain.name)
        assertEquals(2019, domain.releaseYear)
        assertEquals("https://chain.com/image.jpg", domain.imageUrl)
    }

    @Test
    fun `should handle null values correctly`() {
        // When
        val domain = SerieTestDataBuilder()
            .withId(null)
            .withImageUrl(null)
            .withCreatedAt(null)
            .withUpdatedAt(null)
            .buildDomain()

        // Then
        assertNull(domain.id)
        assertNull(domain.imageUrl)
        assertNull(domain.createdAt)
        assertNull(domain.updatedAt)
    }

    @Test
    fun `minimal companion method should create builder with minimal data`() {
        // When
        val domain = SerieTestDataBuilder.minimal().buildDomain()

        // Then
        assertEquals("MIN01", domain.code)
        assertEquals("Minimal Serie", domain.name)
        assertEquals(1999, domain.releaseYear)
        assertNull(domain.imageUrl)
        assertTrue(domain.expansions.isEmpty())
    }

    @Test
    fun `complete companion method should create builder with complete data`() {
        // When
        val domain = SerieTestDataBuilder.complete().buildDomain()

        // Then
        assertEquals("COMP01", domain.code)
        assertEquals("Complete Test Serie", domain.name)
        assertEquals(2024, domain.releaseYear)
        assertEquals("https://example.com/complete-serie.jpg", domain.imageUrl)
        assertTrue(domain.expansions.isEmpty())
    }

    @Test
    fun `invalid companion method should create builder with invalid data`() {
        // When
        val builder = SerieTestDataBuilder.invalid()

        // Then - These should be invalid values that would fail validation
        val domain = builder.buildDomain()
        assertEquals("", domain.code) // Invalid: empty code
        assertEquals("", domain.name) // Invalid: empty name
        assertEquals(1990, domain.releaseYear) // Invalid: year too early
    }

    @Test
    fun `createMultiple companion method should create unique builders`() {
        // When
        val builders = SerieTestDataBuilder.createMultiple(3)

        // Then
        assertEquals(3, builders.size)

        val domains = builders.map { it.buildDomain() }
        assertEquals("BATCH001", domains[0].code)
        assertEquals("Batch Serie 1", domains[0].name)
        assertEquals(2021, domains[0].releaseYear) // 2020 + (1 % 5) = 2021

        assertEquals("BATCH002", domains[1].code)
        assertEquals("Batch Serie 2", domains[1].name)
        assertEquals(2022, domains[1].releaseYear) // 2020 + (2 % 5) = 2022

        assertEquals("BATCH003", domains[2].code)
        assertEquals("Batch Serie 3", domains[2].name)
        assertEquals(2023, domains[2].releaseYear) // 2020 + (3 % 5) = 2023
    }

    @Test
    fun `should generate unique codes for different instances`() {
        // When
        val builder1 = SerieTestDataBuilder()
        val builder2 = SerieTestDataBuilder()

        val domain1 = builder1.buildDomain()
        val domain2 = builder2.buildDomain()

        // Then
        assertTrue(domain1.code != domain2.code, "Codes should be unique")
        assertTrue(domain1.name != domain2.name, "Names should be unique")
    }

    @Test
    fun `should preserve custom values across multiple builds`() {
        // Given
        val builder = SerieTestDataBuilder()
            .withCode("PRESERVE01")
            .withName("Preserved Serie")

        // When
        val domain1 = builder.buildDomain()
        val domain2 = builder.buildDomain()
        val entity = builder.buildEntity()
        val request = builder.buildCreateRequest()

        // Then
        assertEquals("PRESERVE01", domain1.code)
        assertEquals("PRESERVE01", domain2.code)
        assertEquals("PRESERVE01", entity.code)
        assertEquals("PRESERVE01", request.code)

        assertEquals("Preserved Serie", domain1.name)
        assertEquals("Preserved Serie", domain2.name)
        assertEquals("Preserved Serie", entity.name)
        assertEquals("Preserved Serie", request.name)
    }

    @Test
    fun `should handle empty expansions list`() {
        // When
        val domain = SerieTestDataBuilder()
            .withExpansions(emptyList())
            .buildDomain()

        // Then
        assertTrue(domain.expansions.isEmpty())
    }

    @Test
    fun `should handle multiple expansions`() {
        // Given
        val expansions = listOf(
            Expansion(UUID.randomUUID(), "MULTI1", "Multi Expansion 1"),
            Expansion(UUID.randomUUID(), "MULTI2", "Multi Expansion 2"),
            Expansion(UUID.randomUUID(), "MULTI3", "Multi Expansion 3")
        )

        // When
        val domain = SerieTestDataBuilder()
            .withExpansions(expansions)
            .buildDomain()

        // Then
        assertEquals(3, domain.expansions.size)
        assertEquals("MULTI1", domain.expansions[0].code)
        assertEquals("MULTI2", domain.expansions[1].code)
        assertEquals("MULTI3", domain.expansions[2].code)
    }

    @Test
    fun `should build different object types with same data`() {
        // Given
        val builder = SerieTestDataBuilder()
            .withCode("SAME01")
            .withName("Same Data Serie")
            .withReleaseYear(2023)

        // When
        val domain = builder.buildDomain()
        val entity = builder.buildEntity()
        val createRequest = builder.buildCreateRequest()
        val updateRequest = builder.buildUpdateRequest()

        // Then
        assertEquals("SAME01", domain.code)
        assertEquals("SAME01", entity.code)
        assertEquals("SAME01", createRequest.code)
        assertEquals("SAME01", updateRequest.code)

        assertEquals("Same Data Serie", domain.name)
        assertEquals("Same Data Serie", entity.name)
        assertEquals("Same Data Serie", createRequest.name)
        assertEquals("Same Data Serie", updateRequest.name)

        assertEquals(2023, domain.releaseYear)
        assertEquals(2023, entity.releaseYear)
        assertEquals(2023, createRequest.releaseYear)
        assertEquals(2023, updateRequest.releaseYear)
    }
}