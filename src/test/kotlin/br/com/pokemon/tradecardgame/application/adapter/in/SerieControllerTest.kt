package br.com.pokemon.tradecardgame.application.adapter.`in`

import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.SerieController
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.CreateSerieRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.PageRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.UpdateSerieRequest
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.*
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.DeleteSerieByIdCommand
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.UpdateSerieCommand
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindSerieByCodeQuery
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindSerieByIdQuery
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest as SpringPageRequest
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SerieControllerTest {

    private val createSerieUsecase = mockk<CreateSerieUsecase>()
    private val findAllSerieUsecase = mockk<FindAllSerieUsecase>()
    private val findSerieByIdUsecase = mockk<FindSerieByIdUsecase>()
    private val findSerieByCodeUsecase = mockk<FindSerieByCodeUsecase>()
    private val updateSerieByIdUsecase = mockk<UpdateSerieByIdUsecase>()
    private val deleteSerieByIdUsecase = mockk<DeleteSerieByIdUsecase>()

    private lateinit var controller: SerieController

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        controller = SerieController(
            createSerieUsecase,
            findAllSerieUsecase,
            findSerieByIdUsecase,
            findSerieByCodeUsecase,
            updateSerieByIdUsecase,
            deleteSerieByIdUsecase
        )
    }

    @Test
    fun `should find all series successfully`() {
        // Given
        val pageRequest = PageRequest(page = 0, pageSize = 10, sort = "ASC", direction = "id")
        val serie1 = Serie(
            id = UUID.randomUUID(),
            code = "SV01",
            name = "Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01.jpg",
            createdAt = LocalDateTime.now()
        )
        val serie2 = Serie(
            id = UUID.randomUUID(),
            code = "SV02",
            name = "Paldea Evolved",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv02.jpg",
            createdAt = LocalDateTime.now()
        )

        val springPageRequest = SpringPageRequest.of(0, 10)
        val pagedResult = PageImpl(listOf(serie1, serie2), springPageRequest, 2)

        every { findAllSerieUsecase.execute(any()) } returns pagedResult

        // When
        val response = controller.findAllSeries(pageRequest)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(2, response.body!!.content.size)
        assertEquals("SV01", response.body!!.content[0].code)
        assertEquals("SV02", response.body!!.content[1].code)

        verify(exactly = 1) { findAllSerieUsecase.execute(any()) }
    }

    @Test
    fun `should find serie by id successfully`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "SV01",
            name = "Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01.jpg",
            createdAt = LocalDateTime.now()
        )

        every { findSerieByIdUsecase.execute(any()) } returns serie

        // When
        val response = controller.findSerieById(serieId)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(serieId, response.body!!.id)
        assertEquals("SV01", response.body!!.code)
        assertEquals("Scarlet & Violet Base Set", response.body!!.name)

        verify(exactly = 1) { findSerieByIdUsecase.execute(FindSerieByIdQuery(serieId)) }
    }

    @Test
    fun `should find serie by code successfully`() {
        // Given
        val serieCode = "SV01"
        val serie = Serie(
            id = UUID.randomUUID(),
            code = serieCode,
            name = "Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01.jpg",
            createdAt = LocalDateTime.now()
        )

        every { findSerieByCodeUsecase.execute(any()) } returns serie

        // When
        val response = controller.findSerieByCode(serieCode)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(serieCode, response.body!!.code)
        assertEquals("Scarlet & Violet Base Set", response.body!!.name)

        verify(exactly = 1) { findSerieByCodeUsecase.execute(FindSerieByCodeQuery(serieCode)) }
    }

    @Test
    fun `should create serie successfully`() {
        // Given
        val request = CreateSerieRequest(
            code = "SV03",
            name = "Obsidian Flames",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv03.jpg"
        )

        val createdSerie = Serie(
            id = UUID.randomUUID(),
            code = "SV03",
            name = "Obsidian Flames",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv03.jpg",
            createdAt = LocalDateTime.now()
        )

        every { createSerieUsecase.execute(any()) } returns createdSerie

        // When
        val response = controller.createSerie(request)

        // Then
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertEquals("SV03", response.body!!.code)
        assertEquals("Obsidian Flames", response.body!!.name)
        assertNotNull(response.headers.location)

        verify(exactly = 1) { createSerieUsecase.execute(any()) }
    }

    @Test
    fun `should update serie successfully`() {
        // Given
        val serieId = UUID.randomUUID()
        val request = UpdateSerieRequest(
            id = serieId,
            code = "SV01_UPDATED",
            name = "Updated Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01_updated.jpg"
        )

        val updatedSerie = Serie(
            id = serieId,
            code = "SV01_UPDATED",
            name = "Updated Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01_updated.jpg",
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now()
        )

        every { updateSerieByIdUsecase.execute(any(), any()) } returns updatedSerie

        // When
        val response = controller.updateSerie(serieId, request)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(serieId, response.body!!.id)
        assertEquals("SV01_UPDATED", response.body!!.code)
        assertEquals("Updated Scarlet & Violet Base Set", response.body!!.name)

        verify(exactly = 1) { updateSerieByIdUsecase.execute(serieId, any()) }
    }

    @Test
    fun `should delete serie successfully`() {
        // Given
        val serieId = UUID.randomUUID()

        every { deleteSerieByIdUsecase.execute(any()) } just Runs

        // When
        val response = controller.deleteSerieById(serieId)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Serie deleted successfully", response.body!!.title)
        assertEquals("Serie with id $serieId deleted successfully", response.body!!.message)

        verify(exactly = 1) { deleteSerieByIdUsecase.execute(DeleteSerieByIdCommand(serieId)) }
    }

    @Test
    fun `should handle empty page result`() {
        // Given
        val pageRequest = PageRequest(page = 0, pageSize = 10, sort = "ASC", direction = "id")
        val springPageRequest = SpringPageRequest.of(0, 10)
        val emptyPage = PageImpl<Serie>(emptyList(), springPageRequest, 0)

        every { findAllSerieUsecase.execute(any()) } returns emptyPage

        // When
        val response = controller.findAllSeries(pageRequest)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(0, response.body!!.content.size)
        assertEquals(0, response.body!!.totalElements)

        verify(exactly = 1) { findAllSerieUsecase.execute(any()) }
    }

    @Test
    fun `should handle null result from find by id`() {
        // Given
        val serieId = UUID.randomUUID()

        every { findSerieByIdUsecase.execute(any()) } returns null

        // When
        val response = controller.findSerieById(serieId)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        // O controller retorna null no body quando não encontra
        // Em um cenário real, isso deveria ser tratado com um 404

        verify(exactly = 1) { findSerieByIdUsecase.execute(FindSerieByIdQuery(serieId)) }
    }

    @Test
    fun `should handle null result from find by code`() {
        // Given
        val serieCode = "NONEXISTENT"

        every { findSerieByCodeUsecase.execute(any()) } returns null

        // When
        val response = controller.findSerieByCode(serieCode)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        // O controller retorna null no body quando não encontra
        // Em um cenário real, isso deveria ser tratado com um 404

        verify(exactly = 1) { findSerieByCodeUsecase.execute(FindSerieByCodeQuery(serieCode)) }
    }

    @Test
    fun `should pass correct parameters to use cases`() {
        // Given
        val pageRequest = PageRequest(page = 1, pageSize = 5, sort = "ASC", direction = "id")
        val serieId = UUID.randomUUID()
        val serieCode = "TEST01"
        val createRequest = CreateSerieRequest("NEW01", "New Serie", 2024)
        val updateRequest = UpdateSerieRequest(serieId, "UPD01", "Updated Serie", 2024)

        val mockSerie = Serie(UUID.randomUUID(), "TEST", "Test", 2024)
        val mockPage = PageImpl(listOf(mockSerie), SpringPageRequest.of(0, 5), 1) // page is adjusted in PageRequest

        every { findAllSerieUsecase.execute(any()) } returns mockPage
        every { findSerieByIdUsecase.execute(any()) } returns mockSerie
        every { findSerieByCodeUsecase.execute(any()) } returns mockSerie
        every { createSerieUsecase.execute(any()) } returns mockSerie
        every { updateSerieByIdUsecase.execute(any(), any()) } returns mockSerie
        every { deleteSerieByIdUsecase.execute(any()) } just Runs

        // When
        controller.findAllSeries(pageRequest)
        controller.findSerieById(serieId)
        controller.findSerieByCode(serieCode)
        controller.createSerie(createRequest)
        controller.updateSerie(serieId, updateRequest)
        controller.deleteSerieById(serieId)

        // Then
        verify(exactly = 1) { findAllSerieUsecase.execute(match { it.page == 0 && it.pageSize == 5 }) } // page is adjusted
        verify(exactly = 1) { findSerieByIdUsecase.execute(FindSerieByIdQuery(serieId)) }
        verify(exactly = 1) { findSerieByCodeUsecase.execute(FindSerieByCodeQuery(serieCode)) }
        verify(exactly = 1) { createSerieUsecase.execute(match { it.code == "NEW01" && it.name == "New Serie" }) }
        verify(exactly = 1) {
            updateSerieByIdUsecase.execute(
                serieId,
                match { it.code == "UPD01" && it.name == "Updated Serie" })
        }
        verify(exactly = 1) { deleteSerieByIdUsecase.execute(DeleteSerieByIdCommand(serieId)) }
    }
}