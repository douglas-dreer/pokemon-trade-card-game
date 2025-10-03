package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.CreateSerieCommand
import br.com.pokemon.tradecardgame.domain.validation.ValidatorStrategy
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.SeriesJpaAdapter
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class CreateSerieUsecaseTest {

    private val repository = mockk<SeriesJpaAdapter>()
    private val validator1 = mockk<ValidatorStrategy<Serie>>()
    private val validator2 = mockk<ValidatorStrategy<Serie>>()
    private val validators = listOf(validator1, validator2)

    private lateinit var useCase: CreateSerieUsecaseImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        useCase = CreateSerieUsecaseImpl(repository, validators)
    }

    @Test
    fun `should create serie successfully when all validations pass`() {
        // Given
        val command = CreateSerieCommand(
            code = "SV01",
            name = "Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01.jpg"
        )

        val expectedId = UUID.randomUUID()
        val now = LocalDateTime.now()

        val entityToSave = SerieEntity(
            id = null,
            code = "SV01",
            name = "Scarlet & Violet Base Set",
            releaseYear = 2023,
            imageUrl = "https://example.com/sv01.jpg",
            expansions = "",
            createdAt = now,
            updatedAt = now
        )

        val savedEntity = entityToSave.copy(id = expectedId)

        every { validator1.execute(any()) } just Runs
        every { validator2.execute(any()) } just Runs
        every { repository.createSerie(any()) } returns savedEntity

        // When
        val result = useCase.execute(command)

        // Then
        assertEquals(expectedId, result.id)
        assertEquals("SV01", result.code)
        assertEquals("Scarlet & Violet Base Set", result.name)
        assertEquals(2023, result.releaseYear)
        assertEquals("https://example.com/sv01.jpg", result.imageUrl)

        verify(exactly = 1) { validator1.execute(any()) }
        verify(exactly = 1) { validator2.execute(any()) }
        verify(exactly = 1) { repository.createSerie(any()) }
    }

    @Test
    fun `should throw exception when validation fails`() {
        // Given
        val command = CreateSerieCommand(
            code = "INVALID",
            name = "Invalid Serie",
            releaseYear = 2023
        )

        val validationException = RuntimeException("Validation failed")

        every { validator1.execute(any()) } throws validationException

        // When & Then
        assertThrows<RuntimeException> {
            useCase.execute(command)
        }

        verify(exactly = 1) { validator1.execute(any()) }
        verify(exactly = 0) { validator2.execute(any()) }
        verify(exactly = 0) { repository.createSerie(any()) }
    }

    @Test
    fun `should execute all validators when all pass`() {
        // Given
        val command = CreateSerieCommand(
            code = "SV02",
            name = "Paldea Evolved",
            releaseYear = 2023
        )

        val savedEntity = SerieEntity(
            id = UUID.randomUUID(),
            code = "SV02",
            name = "Paldea Evolved",
            releaseYear = 2023,
            imageUrl = null,
            expansions = "",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { validator1.execute(any()) } just Runs
        every { validator2.execute(any()) } just Runs
        every { repository.createSerie(any()) } returns savedEntity

        // When
        useCase.execute(command)

        // Then
        verify(exactly = 1) { validator1.execute(any()) }
        verify(exactly = 1) { validator2.execute(any()) }
        verifyOrder {
            validator1.execute(any())
            validator2.execute(any())
            repository.createSerie(any())
        }
    }

    @Test
    fun `should create serie with minimal required fields`() {
        // Given
        val command = CreateSerieCommand(
            code = "SV03",
            name = "Obsidian Flames",
            releaseYear = 2023
        )

        val expectedId = UUID.randomUUID()
        val savedEntity = SerieEntity(
            id = expectedId,
            code = "SV03",
            name = "Obsidian Flames",
            releaseYear = 2023,
            imageUrl = null,
            expansions = "",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { validator1.execute(any()) } just Runs
        every { validator2.execute(any()) } just Runs
        every { repository.createSerie(any()) } returns savedEntity

        // When
        val result = useCase.execute(command)

        // Then
        assertEquals(expectedId, result.id)
        assertEquals("SV03", result.code)
        assertEquals("Obsidian Flames", result.name)
        assertEquals(2023, result.releaseYear)
        assertEquals(null, result.imageUrl)
        assertEquals(emptyList(), result.expansions)
    }
}