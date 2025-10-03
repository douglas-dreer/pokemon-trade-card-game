package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindSerieByIdQuery
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FindSerieByIdUsecaseTest {

    private val repository = mockk<SerieRepositoryPort>()
    private lateinit var useCase: FindSerieByIdUsecaseImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        useCase = FindSerieByIdUsecaseImpl(repository)
    }

    @Test
    fun `should return serie when found by id`() {
        // Given
        val serieId = UUID.randomUUID()
        val query = FindSerieByIdQuery(serieId)
        val now = LocalDateTime.now()

        val serieEntity = SerieEntity(
            id = serieId,
            code = "SV01",
            name = "Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01.jpg",
            expansions = "EXP1,EXP2",
            createdAt = now,
            updatedAt = now
        )

        every { repository.findSerieById(serieId) } returns serieEntity

        // When
        val result = useCase.execute(query)

        // Then
        assertEquals(serieId, result?.id)
        assertEquals("SV01", result?.code)
        assertEquals("Scarlet & Violet Base Set", result?.name)
        assertEquals(2023, result?.releaseYear)
        assertEquals("https://example.com/sv01.jpg", result?.imageUrl)
        assertEquals(now, result?.createdAt)
        assertEquals(now, result?.updatedAt)

        verify(exactly = 1) { repository.findSerieById(serieId) }
    }

    @Test
    fun `should return null when serie not found by id`() {
        // Given
        val serieId = UUID.randomUUID()
        val query = FindSerieByIdQuery(serieId)

        every { repository.findSerieById(serieId) } returns null

        // When
        val result = useCase.execute(query)

        // Then
        assertNull(result)

        verify(exactly = 1) { repository.findSerieById(serieId) }
    }

    @Test
    fun `should handle serie with minimal fields`() {
        // Given
        val serieId = UUID.randomUUID()
        val query = FindSerieByIdQuery(serieId)

        val serieEntity = SerieEntity(
            id = serieId,
            code = "MIN01",
            name = "Minimal Serie",
            releaseYear = 2024,
            imageUrl = null,
            expansions = "",
            createdAt = LocalDateTime.now(),
            updatedAt = null
        )

        every { repository.findSerieById(serieId) } returns serieEntity

        // When
        val result = useCase.execute(query)

        // Then
        assertEquals(serieId, result?.id)
        assertEquals("MIN01", result?.code)
        assertEquals("Minimal Serie", result?.name)
        assertEquals(2024, result?.releaseYear)
        assertNull(result?.imageUrl)
        assertEquals(emptyList(), result?.expansions)
        assertNull(result?.updatedAt)

        verify(exactly = 1) { repository.findSerieById(serieId) }
    }

    @Test
    fun `should map all entity properties to domain correctly`() {
        // Given
        val serieId = UUID.randomUUID()
        val query = FindSerieByIdQuery(serieId)
        val createdAt = LocalDateTime.of(2023, 1, 1, 10, 0)
        val updatedAt = LocalDateTime.of(2023, 6, 1, 15, 30)

        val serieEntity = SerieEntity(
            id = serieId,
            code = "FULL01",
            name = "Full Featured Serie",
            releaseYear = 2023,
            imageUrl = "https://example.com/full01.jpg",
            expansions = "BASE,PROMO,SPECIAL",
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        every { repository.findSerieById(serieId) } returns serieEntity

        // When
        val result = useCase.execute(query)

        // Then
        assertEquals(serieId, result?.id)
        assertEquals("FULL01", result?.code)
        assertEquals("Full Featured Serie", result?.name)
        assertEquals(2023, result?.releaseYear)
        assertEquals("https://example.com/full01.jpg", result?.imageUrl)
        assertEquals(createdAt, result?.createdAt)
        assertEquals(updatedAt, result?.updatedAt)

        verify(exactly = 1) { repository.findSerieById(serieId) }
    }

    @Test
    fun `should call repository with correct id parameter`() {
        // Given
        val expectedId = UUID.randomUUID()
        val query = FindSerieByIdQuery(expectedId)

        every { repository.findSerieById(expectedId) } returns null

        // When
        useCase.execute(query)

        // Then
        verify(exactly = 1) { repository.findSerieById(expectedId) }
        confirmVerified(repository)
    }
}