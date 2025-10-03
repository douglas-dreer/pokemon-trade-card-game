package br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence

import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.repository.SeriesSpringRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SeriesJpaAdapterTest {

    private val springRepository = mockk<SeriesSpringRepository>()
    private lateinit var adapter: SeriesJpaAdapter

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        adapter = SeriesJpaAdapter(springRepository)
    }

    @Test
    fun `should find all series with pagination successfully`() {
        // Given
        val page = 0
        val pageSize = 10
        val pageRequest = PageRequest.of(page, pageSize)

        val serieEntities = listOf(
            SerieEntity(
                id = UUID.randomUUID(),
                code = "SV01",
                name = "Scarlet & Violet Base Set",
                releaseYear = 2023,
                imageUrl = "https://example.com/sv01.jpg",
                expansions = "BASE,PROMO",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            SerieEntity(
                id = UUID.randomUUID(),
                code = "SV02",
                name = "Paldea Evolved",
                releaseYear = 2023,
                imageUrl = "https://example.com/sv02.jpg",
                expansions = "BASE",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        val pagedResult = PageImpl(serieEntities, pageRequest, serieEntities.size.toLong())

        every { springRepository.findAll(pageRequest) } returns pagedResult

        // When
        val result = adapter.findAllSeries(page, pageSize)

        // Then
        assertEquals(2, result.content.size)
        assertEquals("SV01", result.content[0].code)
        assertEquals("SV02", result.content[1].code)
        assertEquals(2, result.totalElements)
        assertEquals(0, result.number)
        assertEquals(10, result.size)

        verify(exactly = 1) { springRepository.findAll(pageRequest) }
    }

    @Test
    fun `should find serie by id successfully`() {
        // Given
        val serieId = UUID.randomUUID()
        val serieEntity = SerieEntity(
            id = serieId,
            code = "SV01",
            name = "Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01.jpg",
            expansions = "BASE,PROMO",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { springRepository.findById(serieId) } returns Optional.of(serieEntity)

        // When
        val result = adapter.findSerieById(serieId)

        // Then
        assertEquals(serieId, result?.id)
        assertEquals("SV01", result?.code)
        assertEquals("Scarlet & Violet Base Set", result?.name)
        assertEquals(2023, result?.releaseYear)

        verify(exactly = 1) { springRepository.findById(serieId) }
    }

    @Test
    fun `should return null when serie not found by id`() {
        // Given
        val serieId = UUID.randomUUID()

        every { springRepository.findById(serieId) } returns Optional.empty()

        // When
        val result = adapter.findSerieById(serieId)

        // Then
        assertNull(result)

        verify(exactly = 1) { springRepository.findById(serieId) }
    }

    @Test
    fun `should find serie by code successfully`() {
        // Given
        val serieCode = "SV01"
        val serieEntity = SerieEntity(
            id = UUID.randomUUID(),
            code = serieCode,
            name = "Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01.jpg",
            expansions = "BASE,PROMO",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { springRepository.findSerieEntityByCode(serieCode) } returns serieEntity

        // When
        val result = adapter.findSerieByCode(serieCode)

        // Then
        assertEquals(serieCode, result?.code)
        assertEquals("Scarlet & Violet Base Set", result?.name)
        assertEquals(2023, result?.releaseYear)

        verify(exactly = 1) { springRepository.findSerieEntityByCode(serieCode) }
    }

    @Test
    fun `should return null when serie not found by code`() {
        // Given
        val serieCode = "NONEXISTENT"

        every { springRepository.findSerieEntityByCode(serieCode) } returns null

        // When
        val result = adapter.findSerieByCode(serieCode)

        // Then
        assertNull(result)

        verify(exactly = 1) { springRepository.findSerieEntityByCode(serieCode) }
    }

    @Test
    fun `should create serie successfully`() {
        // Given
        val serieEntity = SerieEntity(
            id = null,
            code = "SV03",
            name = "Obsidian Flames",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv03.jpg",
            expansions = "BASE",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedEntity = serieEntity.copy(id = UUID.randomUUID())

        every { springRepository.save(serieEntity) } returns savedEntity

        // When
        val result = adapter.createSerie(serieEntity)

        // Then
        assertEquals(savedEntity.id, result.id)
        assertEquals("SV03", result.code)
        assertEquals("Obsidian Flames", result.name)
        assertEquals(2023, result.releaseYear)

        verify(exactly = 1) { springRepository.save(serieEntity) }
    }

    @Test
    fun `should update serie successfully`() {
        // Given
        val serieId = UUID.randomUUID()
        val serieEntity = SerieEntity(
            id = serieId,
            code = "SV01_UPDATED",
            name = "Updated Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01_updated.jpg",
            expansions = "BASE,PROMO,SPECIAL",
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now()
        )

        every { springRepository.save(serieEntity) } returns serieEntity

        // When
        val result = adapter.updateSerie(serieEntity)

        // Then
        assertEquals(serieId, result.id)
        assertEquals("SV01_UPDATED", result.code)
        assertEquals("Updated Scarlet & Violet Base Set", result.name)
        assertEquals("BASE,PROMO,SPECIAL", result.expansions)

        verify(exactly = 1) { springRepository.save(serieEntity) }
    }

    @Test
    fun `should delete serie by id successfully`() {
        // Given
        val serieId = UUID.randomUUID()

        every { springRepository.deleteById(serieId) } just Runs

        // When
        adapter.deleteSerieById(serieId)

        // Then
        verify(exactly = 1) { springRepository.deleteById(serieId) }
    }

    @Test
    fun `should check if serie exists by id`() {
        // Given
        val existingId = UUID.randomUUID()
        val nonExistingId = UUID.randomUUID()

        every { springRepository.existsById(existingId) } returns true
        every { springRepository.existsById(nonExistingId) } returns false

        // When
        val existsResult = adapter.existsSerieById(existingId)
        val notExistsResult = adapter.existsSerieById(nonExistingId)

        // Then
        assertTrue(existsResult)
        assertFalse(notExistsResult)

        verify(exactly = 1) { springRepository.existsById(existingId) }
        verify(exactly = 1) { springRepository.existsById(nonExistingId) }
    }

    @Test
    fun `should check if serie exists by code`() {
        // Given
        val existingCode = "SV01"
        val nonExistingCode = "NONEXISTENT"

        every { springRepository.existsSerieByCode(existingCode) } returns true
        every { springRepository.existsSerieByCode(nonExistingCode) } returns false

        // When
        val existsResult = adapter.existsSerieByCode(existingCode)
        val notExistsResult = adapter.existsSerieByCode(nonExistingCode)

        // Then
        assertTrue(existsResult)
        assertFalse(notExistsResult)

        verify(exactly = 1) { springRepository.existsSerieByCode(existingCode) }
        verify(exactly = 1) { springRepository.existsSerieByCode(nonExistingCode) }
    }

    @Test
    fun `should check if serie exists by name`() {
        // Given
        val existingName = "Scarlet & Violet Base Set"
        val nonExistingName = "Nonexistent Serie"

        every { springRepository.existsSerieByName(existingName) } returns true
        every { springRepository.existsSerieByName(nonExistingName) } returns false

        // When
        val existsResult = adapter.existsSerieByName(existingName)
        val notExistsResult = adapter.existsSerieByName(nonExistingName)

        // Then
        assertTrue(existsResult)
        assertFalse(notExistsResult)

        verify(exactly = 1) { springRepository.existsSerieByName(existingName) }
        verify(exactly = 1) { springRepository.existsSerieByName(nonExistingName) }
    }

    @Test
    fun `should handle empty page result`() {
        // Given
        val page = 0
        val pageSize = 10
        val pageRequest = PageRequest.of(page, pageSize)
        val emptyPage = PageImpl<SerieEntity>(emptyList(), pageRequest, 0)

        every { springRepository.findAll(pageRequest) } returns emptyPage

        // When
        val result = adapter.findAllSeries(page, pageSize)

        // Then
        assertEquals(0, result.content.size)
        assertEquals(0, result.totalElements)
        assertEquals(0, result.number)
        assertEquals(10, result.size)

        verify(exactly = 1) { springRepository.findAll(pageRequest) }
    }

    @Test
    fun `should handle different page sizes correctly`() {
        // Given
        val page = 1
        val pageSize = 5
        val pageRequest = PageRequest.of(page, pageSize)

        val serieEntity = SerieEntity(
            id = UUID.randomUUID(),
            code = "SV03",
            name = "Obsidian Flames",
            releaseYear = 2023,
            imageUrl = null,
            expansions = "",
            createdAt = LocalDateTime.now(),
            updatedAt = null
        )

        val pagedResult = PageImpl(listOf(serieEntity), pageRequest, 6L) // Total of 6 elements

        every { springRepository.findAll(pageRequest) } returns pagedResult

        // When
        val result = adapter.findAllSeries(page, pageSize)

        // Then
        assertEquals(1, result.content.size)
        assertEquals("SV03", result.content[0].code)
        assertEquals(6, result.totalElements)
        assertEquals(1, result.number)
        assertEquals(5, result.size)
        assertEquals(2, result.totalPages) // 6 elements / 5 per page = 2 pages

        verify(exactly = 1) { springRepository.findAll(pageRequest) }
    }

    @Test
    fun `should pass correct parameters to spring repository`() {
        // Given
        val serieId = UUID.randomUUID()
        val serieCode = "TEST01"
        val serieName = "Test Serie"
        val serieEntity = SerieEntity(
            id = serieId,
            code = serieCode,
            name = serieName,
            releaseYear = 2024,
            imageUrl = null,
            expansions = "",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { springRepository.findAll(any<PageRequest>()) } returns PageImpl(emptyList())
        every { springRepository.findById(any()) } returns Optional.empty()
        every { springRepository.findSerieEntityByCode(any()) } returns null
        every { springRepository.save(any()) } returns serieEntity
        every { springRepository.deleteById(any()) } just Runs
        every { springRepository.existsById(any()) } returns false
        every { springRepository.existsSerieByCode(any()) } returns false
        every { springRepository.existsSerieByName(any()) } returns false

        // When
        adapter.findAllSeries(1, 5)
        adapter.findSerieById(serieId)
        adapter.findSerieByCode(serieCode)
        adapter.createSerie(serieEntity)
        adapter.updateSerie(serieEntity)
        adapter.deleteSerieById(serieId)
        adapter.existsSerieById(serieId)
        adapter.existsSerieByCode(serieCode)
        adapter.existsSerieByName(serieName)

        // Then
        verify(exactly = 1) { springRepository.findAll(PageRequest.of(1, 5)) }
        verify(exactly = 1) { springRepository.findById(serieId) }
        verify(exactly = 1) { springRepository.findSerieEntityByCode(serieCode) }
        verify(exactly = 2) { springRepository.save(serieEntity) } // create and update
        verify(exactly = 1) { springRepository.deleteById(serieId) }
        verify(exactly = 1) { springRepository.existsById(serieId) }
        verify(exactly = 1) { springRepository.existsSerieByCode(serieCode) }
        verify(exactly = 1) { springRepository.existsSerieByName(serieName) }
    }
}