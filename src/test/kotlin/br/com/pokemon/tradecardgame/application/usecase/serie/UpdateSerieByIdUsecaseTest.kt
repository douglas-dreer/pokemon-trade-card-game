package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.UpdateSerieCommand
import br.com.pokemon.tradecardgame.domain.validation.ValidatorStrategy
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class UpdateSerieByIdUsecaseTest {

    private val repository = mockk<SerieRepositoryPort>()
    private val validator1 = mockk<ValidatorStrategy<Serie>>()
    private val validator2 = mockk<ValidatorStrategy<Serie>>()
    private val validators = listOf(validator1, validator2)

    private lateinit var useCase: UpdateSerieByIdUsecaseImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        useCase = UpdateSerieByIdUsecaseImpl(repository, validators)
    }

    @Test
    fun `should update serie successfully when all validations pass`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = UpdateSerieCommand(
            id = serieId,
            code = "SV01_UPDATED",
            name = "Updated Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01_updated.jpg"
        )

        val now = LocalDateTime.now()
        val updatedEntity = SerieEntity(
            id = serieId,
            code = "SV01_UPDATED",
            name = "Updated Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01_updated.jpg",
            expansions = "",
            createdAt = now.minusDays(1),
            updatedAt = now
        )

        every { validator1.execute(any()) } just Runs
        every { validator2.execute(any()) } just Runs
        every { repository.updateSerie(any()) } returns updatedEntity

        // When
        val result = useCase.execute(serieId, command)

        // Then
        assertEquals(serieId, result.id)
        assertEquals("SV01_UPDATED", result.code)
        assertEquals("Updated Scarlet & Violet Base Set", result.name)
        assertEquals(2023, result.releaseYear)
        assertEquals("https://example.com/sv01_updated.jpg", result.imageUrl)

        verify(exactly = 1) { validator1.execute(any()) }
        verify(exactly = 1) { validator2.execute(any()) }
        verify(exactly = 1) { repository.updateSerie(any()) }
    }

    @Test
    fun `should throw exception when validation fails`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = UpdateSerieCommand(
            id = serieId,
            code = "INVALID",
            name = "Invalid Update",
            releaseYear = 2023
        )

        val validationException = RuntimeException("Update validation failed")

        every { validator1.execute(any()) } throws validationException

        // When & Then
        assertThrows<RuntimeException> {
            useCase.execute(serieId, command)
        }

        verify(exactly = 1) { validator1.execute(any()) }
        verify(exactly = 0) { validator2.execute(any()) }
        verify(exactly = 0) { repository.updateSerie(any()) }
    }

    @Test
    fun `should execute all validators in order when all pass`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = UpdateSerieCommand(
            id = serieId,
            code = "SV02",
            name = "Paldea Evolved Updated",
            releaseYear = 2023
        )

        val updatedEntity = SerieEntity(
            id = serieId,
            code = "SV02",
            name = "Paldea Evolved Updated",
            releaseYear = 2023,
            imageUrl = null,
            expansions = "",
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now()
        )

        every { validator1.execute(any()) } just Runs
        every { validator2.execute(any()) } just Runs
        every { repository.updateSerie(any()) } returns updatedEntity

        // When
        useCase.execute(serieId, command)

        // Then
        verify(exactly = 1) { validator1.execute(any()) }
        verify(exactly = 1) { validator2.execute(any()) }
        verifyOrder {
            validator1.execute(any())
            validator2.execute(any())
            repository.updateSerie(any())
        }
    }

    @Test
    fun `should update serie with partial fields`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = UpdateSerieCommand(
            id = serieId,
            code = "PARTIAL01",
            name = "Partially Updated Serie",
            releaseYear = 2024,
            imageUrl = null
        )

        val updatedEntity = SerieEntity(
            id = serieId,
            code = "PARTIAL01",
            name = "Partially Updated Serie",
            releaseYear = 2024,
            imageUrl = null,
            expansions = "",
            createdAt = LocalDateTime.now().minusDays(5),
            updatedAt = LocalDateTime.now()
        )

        every { validator1.execute(any()) } just Runs
        every { validator2.execute(any()) } just Runs
        every { repository.updateSerie(any()) } returns updatedEntity

        // When
        val result = useCase.execute(serieId, command)

        // Then
        assertEquals(serieId, result.id)
        assertEquals("PARTIAL01", result.code)
        assertEquals("Partially Updated Serie", result.name)
        assertEquals(2024, result.releaseYear)
        assertEquals(null, result.imageUrl)

        verify(exactly = 1) { repository.updateSerie(any()) }
    }

    @Test
    fun `should pass correct domain object to validators`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = UpdateSerieCommand(
            id = serieId,
            code = "VALIDATE01",
            name = "Validation Test Serie",
            releaseYear = 2023,
            imageUrl = "https://example.com/validate.jpg"
        )

        val updatedEntity = SerieEntity(
            id = serieId,
            code = "VALIDATE01",
            name = "Validation Test Serie",
            releaseYear = 2023,
            imageUrl = "https://example.com/validate.jpg",
            expansions = "",
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now()
        )

        val capturedSerie = slot<Serie>()

        every { validator1.execute(capture(capturedSerie)) } just Runs
        every { validator2.execute(any()) } just Runs
        every { repository.updateSerie(any()) } returns updatedEntity

        // When
        useCase.execute(serieId, command)

        // Then
        assertEquals(serieId, capturedSerie.captured.id)
        assertEquals("VALIDATE01", capturedSerie.captured.code)
        assertEquals("Validation Test Serie", capturedSerie.captured.name)
        assertEquals(2023, capturedSerie.captured.releaseYear)
        assertEquals("https://example.com/validate.jpg", capturedSerie.captured.imageUrl)
    }

    @Test
    fun `should handle repository exception properly`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = UpdateSerieCommand(
            id = serieId,
            code = "REPO_ERROR",
            name = "Repository Error Test",
            releaseYear = 2023
        )

        val repositoryException = RuntimeException("Database connection failed")

        every { validator1.execute(any()) } just Runs
        every { validator2.execute(any()) } just Runs
        every { repository.updateSerie(any()) } throws repositoryException

        // When & Then
        assertThrows<RuntimeException> {
            useCase.execute(serieId, command)
        }

        verify(exactly = 1) { validator1.execute(any()) }
        verify(exactly = 1) { validator2.execute(any()) }
        verify(exactly = 1) { repository.updateSerie(any()) }
    }

    @Test
    fun `should call executeValidation with correct parameters`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = UpdateSerieCommand(
            id = serieId,
            code = "EXEC_VAL",
            name = "Execute Validation Test",
            releaseYear = 2023
        )

        val updatedEntity = SerieEntity(
            id = serieId,
            code = "EXEC_VAL",
            name = "Execute Validation Test",
            releaseYear = 2023,
            imageUrl = null,
            expansions = "",
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now()
        )

        every { validator1.execute(any()) } just Runs
        every { validator2.execute(any()) } just Runs
        every { repository.updateSerie(any()) } returns updatedEntity

        // When
        useCase.execute(serieId, command)

        // Then
        // Verify that executeValidation was called with the correct ID and domain object
        verify(exactly = 1) { validator1.execute(match { it.id == serieId }) }
        verify(exactly = 1) { validator2.execute(match { it.id == serieId }) }
    }
}