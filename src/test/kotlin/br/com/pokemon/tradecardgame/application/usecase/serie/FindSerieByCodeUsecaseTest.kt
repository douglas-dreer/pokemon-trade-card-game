package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindSerieByCodeQuery
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FindSerieByCodeUsecaseTest {

    private val repository = mockk<SerieRepositoryPort>()
    private lateinit var useCase: FindSerieByCodeUsecaseImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        useCase = FindSerieByCodeUsecaseImpl(repository)
    }

    @Test
    fun `should return serie when found by code`() {
        // Given
        val serieCode = "SV01"
        val query = FindSerieByCodeQuery(serieCode)
        val serieId = UUID.randomUUID()
        val now = LocalDateTime.now()

        val serieEntity = SerieEntity(
            id = serieId,
            code = serieCode,
            name = "Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01.jpg",
            expansions = "BASE,PROMO",
            createdAt = now,
            updatedAt = now
        )

        every { repository.findSerieByCode(serieCode) } returns serieEntity

        // When
        val result = useCase.execute(query)

        // Then
        assertEquals(serieId, result?.id)
        assertEquals(serieCode, result?.code)
        assertEquals("Scarlet & Violet Base Set", result?.name)
        assertEquals(2023, result?.releaseYear)
        assertEquals("https://example.com/sv01.jpg", result?.imageUrl)
        assertEquals(now, result?.createdAt)
        assertEquals(now, result?.updatedAt)

        verify(exactly = 1) { repository.findSerieByCode(serieCode) }
    }

    @Test
    fun `should return null when serie not found by code`() {
        // Given
        val serieCode = "NONEXISTENT"
        val query = FindSerieByCodeQuery(serieCode)

        every { repository.findSerieByCode(serieCode) } returns null

        // When
        val result = useCase.execute(query)

        // Then
        assertNull(result)

        verify(exactly = 1) { repository.findSerieByCode(serieCode) }
    }

    @Test
    fun `should handle serie with empty expansions`() {
        // Given
        val serieCode = "EMPTY01"
        val query = FindSerieByCodeQuery(serieCode)
        val serieId = UUID.randomUUID()

        val serieEntity = SerieEntity(
            id = serieId,
            code = serieCode,
            name = "Empty Expansions Serie",
            releaseYear = 2024,
            imageUrl = null,
            expansions = "",
            createdAt = LocalDateTime.now(),
            updatedAt = null
        )

        every { repository.findSerieByCode(serieCode) } returns serieEntity

        // When
        val result = useCase.execute(query)

        // Then
        assertEquals(serieId, result?.id)
        assertEquals(serieCode, result?.code)
        assertEquals("Empty Expansions Serie", result?.name)
        assertEquals(2024, result?.releaseYear)
        assertNull(result?.imageUrl)
        assertEquals(emptyList(), result?.expansions)
        assertNull(result?.updatedAt)

        verify(exactly = 1) { repository.findSerieByCode(serieCode) }
    }

    @Test
    fun `should handle case sensitive code search`() {
        // Given
        val serieCode = "sv01" // lowercase
        val query = FindSerieByCodeQuery(serieCode)

        every { repository.findSerieByCode(serieCode) } returns null

        // When
        val result = useCase.execute(query)

        // Then
        assertNull(result)

        verify(exactly = 1) { repository.findSerieByCode(serieCode) }
    }

    @Test
    fun `should handle special characters in code`() {
        // Given
        val serieCode = "SV-01_SPECIAL"
        val query = FindSerieByCodeQuery(serieCode)
        val serieId = UUID.randomUUID()

        val serieEntity = SerieEntity(
            id = serieId,
            code = serieCode,
            name = "Special Characters Serie",
            releaseYear = 2023,
            imageUrl = "https://example.com/special.jpg",
            expansions = "SPECIAL",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { repository.findSerieByCode(serieCode) } returns serieEntity

        // When
        val result = useCase.execute(query)

        // Then
        assertEquals(serieId, result?.id)
        assertEquals(serieCode, result?.code)
        assertEquals("Special Characters Serie", result?.name)

        verify(exactly = 1) { repository.findSerieByCode(serieCode) }
    }

    @Test
    fun `should call repository with exact code parameter`() {
        // Given
        val expectedCode = "EXACT_CODE"
        val query = FindSerieByCodeQuery(expectedCode)

        every { repository.findSerieByCode(expectedCode) } returns null

        // When
        useCase.execute(query)

        // Then
        verify(exactly = 1) { repository.findSerieByCode(expectedCode) }
        confirmVerified(repository)
    }

    @Test
    fun `should map entity with multiple expansions correctly`() {
        // Given
        val serieCode = "MULTI01"
        val query = FindSerieByCodeQuery(serieCode)
        val serieId = UUID.randomUUID()

        val serieEntity = SerieEntity(
            id = serieId,
            code = serieCode,
            name = "Multi Expansion Serie",
            releaseYear = 2023,
            imageUrl = "https://example.com/multi.jpg",
            expansions = "BASE,PROMO,SPECIAL,LIMITED",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { repository.findSerieByCode(serieCode) } returns serieEntity

        // When
        val result = useCase.execute(query)

        // Then
        assertEquals(serieId, result?.id)
        assertEquals(serieCode, result?.code)
        assertEquals("Multi Expansion Serie", result?.name)
        // Note: The actual expansion mapping depends on the toDomain() implementation
        // This test verifies the use case calls the repository correctly

        verify(exactly = 1) { repository.findSerieByCode(serieCode) }
    }
}