package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindAllSerieQuery
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class FindAllSerieUsecaseTest {

    private val repository = mockk<SerieRepositoryPort>()
    private lateinit var useCase: FindAllSerieUsecaseImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        useCase = FindAllSerieUsecaseImpl(repository)
    }

    @Test
    fun `should return paginated series successfully`() {
        // Given
        val query = FindAllSerieQuery(page = 0, pageSize = 10)

        val serieEntities = listOf(
            SerieEntity(
                id = UUID.randomUUID(),
                code = "SV01",
                name = "Scarlet & Violet Base Set",
                releaseYear = 2023,
                imageUrl = "https://example.com/sv01.jpg",
                expansions = "",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            SerieEntity(
                id = UUID.randomUUID(),
                code = "SV02",
                name = "Paldea Evolved",
                releaseYear = 2023,
                imageUrl = "https://example.com/sv02.jpg",
                expansions = "",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        val pageRequest = PageRequest.of(0, 10)
        val pagedResult = PageImpl(serieEntities, pageRequest, serieEntities.size.toLong())

        every { repository.findAllSeries(0, 10) } returns pagedResult

        // When
        val result = useCase.execute(query)

        // Then
        assertEquals(2, result.content.size)
        assertEquals("SV01", result.content[0].code)
        assertEquals("Scarlet & Violet Base Set", result.content[0].name)
        assertEquals("SV02", result.content[1].code)
        assertEquals("Paldea Evolved", result.content[1].name)
        assertEquals(2, result.totalElements)
        assertEquals(0, result.number)
        assertEquals(10, result.size)

        verify(exactly = 1) { repository.findAllSeries(0, 10) }
    }

    @Test
    fun `should return empty page when no series exist`() {
        // Given
        val query = FindAllSerieQuery(page = 0, pageSize = 10)
        val pageRequest = PageRequest.of(0, 10)
        val emptyPage = PageImpl<SerieEntity>(emptyList(), pageRequest, 0)

        every { repository.findAllSeries(0, 10) } returns emptyPage

        // When
        val result = useCase.execute(query)

        // Then
        assertEquals(0, result.content.size)
        assertEquals(0, result.totalElements)
        assertEquals(0, result.number)
        assertEquals(10, result.size)

        verify(exactly = 1) { repository.findAllSeries(0, 10) }
    }

    @Test
    fun `should handle different page sizes correctly`() {
        // Given
        val query = FindAllSerieQuery(page = 1, pageSize = 5)

        val serieEntities = listOf(
            SerieEntity(
                id = UUID.randomUUID(),
                code = "SV03",
                name = "Obsidian Flames",
                releaseYear = 2023,
                imageUrl = null,
                expansions = "",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        val pageRequest = PageRequest.of(1, 5)
        val pagedResult = PageImpl(serieEntities, pageRequest, 6L) // Total of 6 elements

        every { repository.findAllSeries(1, 5) } returns pagedResult

        // When
        val result = useCase.execute(query)

        // Then
        assertEquals(1, result.content.size)
        assertEquals("SV03", result.content[0].code)
        assertEquals(6, result.totalElements)
        assertEquals(1, result.number)
        assertEquals(5, result.size)
        assertEquals(2, result.totalPages) // 6 elements / 5 per page = 2 pages

        verify(exactly = 1) { repository.findAllSeries(1, 5) }
    }

    @Test
    fun `should map entity properties to domain correctly`() {
        // Given
        val query = FindAllSerieQuery(page = 0, pageSize = 1)
        val now = LocalDateTime.now()

        val serieEntity = SerieEntity(
            id = UUID.randomUUID(),
            code = "TEST01",
            name = "Test Serie",
            releaseYear = 2024,
            imageUrl = "https://test.com/image.jpg",
            expansions = "EXP1,EXP2",
            createdAt = now,
            updatedAt = now
        )

        val pageRequest = PageRequest.of(0, 1)
        val pagedResult = PageImpl(listOf(serieEntity), pageRequest, 1)

        every { repository.findAllSeries(0, 1) } returns pagedResult

        // When
        val result = useCase.execute(query)

        // Then
        val domainSerie = result.content[0]
        assertEquals(serieEntity.id, domainSerie.id)
        assertEquals(serieEntity.code, domainSerie.code)
        assertEquals(serieEntity.name, domainSerie.name)
        assertEquals(serieEntity.releaseYear, domainSerie.releaseYear)
        assertEquals(serieEntity.imageUrl, domainSerie.imageUrl)
        assertEquals(serieEntity.createdAt, domainSerie.createdAt)
        assertEquals(serieEntity.updatedAt, domainSerie.updatedAt)
    }
}