package br.com.pokemon.tradecardgame.application.usecase.serie

import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.DeleteSerieByIdCommand
import br.com.pokemon.tradecardgame.domain.validation.ValidatorStrategy
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class DeleteSerieByIdUsecaseTest {

    private val repository = mockk<SerieRepositoryPort>()
    private val validator1 = mockk<ValidatorStrategy<UUID>>()
    private val validator2 = mockk<ValidatorStrategy<UUID>>()
    private val validators = listOf(validator1, validator2)

    private lateinit var useCase: DeleteSerieByIdUsecaseImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        useCase = DeleteSerieByIdUsecaseImpl(repository, validators)
    }

    @Test
    fun `should delete serie successfully when all validations pass`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = DeleteSerieByIdCommand(serieId)

        every { validator1.execute(serieId) } just Runs
        every { validator2.execute(serieId) } just Runs
        every { repository.deleteSerieById(serieId) } just Runs

        // When
        useCase.execute(command)

        // Then
        verify(exactly = 1) { validator1.execute(serieId) }
        verify(exactly = 1) { validator2.execute(serieId) }
        verify(exactly = 1) { repository.deleteSerieById(serieId) }
    }

    @Test
    fun `should throw exception when validation fails`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = DeleteSerieByIdCommand(serieId)

        val validationException = RuntimeException("Delete validation failed")

        every { validator1.execute(serieId) } throws validationException

        // When & Then
        assertThrows<RuntimeException> {
            useCase.execute(command)
        }

        verify(exactly = 1) { validator1.execute(serieId) }
        verify(exactly = 0) { validator2.execute(serieId) }
        verify(exactly = 0) { repository.deleteSerieById(serieId) }
    }

    @Test
    fun `should execute all validators in order when all pass`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = DeleteSerieByIdCommand(serieId)

        every { validator1.execute(serieId) } just Runs
        every { validator2.execute(serieId) } just Runs
        every { repository.deleteSerieById(serieId) } just Runs

        // When
        useCase.execute(command)

        // Then
        verify(exactly = 1) { validator1.execute(serieId) }
        verify(exactly = 1) { validator2.execute(serieId) }
        verifyOrder {
            validator1.execute(serieId)
            validator2.execute(serieId)
            repository.deleteSerieById(serieId)
        }
    }

    @Test
    fun `should stop execution when first validator fails`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = DeleteSerieByIdCommand(serieId)

        val firstValidatorException = RuntimeException("First validator failed")

        every { validator1.execute(serieId) } throws firstValidatorException

        // When & Then
        assertThrows<RuntimeException> {
            useCase.execute(command)
        }

        verify(exactly = 1) { validator1.execute(serieId) }
        verify(exactly = 0) { validator2.execute(serieId) }
        verify(exactly = 0) { repository.deleteSerieById(serieId) }
    }

    @Test
    fun `should stop execution when second validator fails`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = DeleteSerieByIdCommand(serieId)

        val secondValidatorException = RuntimeException("Second validator failed")

        every { validator1.execute(serieId) } just Runs
        every { validator2.execute(serieId) } throws secondValidatorException

        // When & Then
        assertThrows<RuntimeException> {
            useCase.execute(command)
        }

        verify(exactly = 1) { validator1.execute(serieId) }
        verify(exactly = 1) { validator2.execute(serieId) }
        verify(exactly = 0) { repository.deleteSerieById(serieId) }
    }

    @Test
    fun `should handle repository exception properly`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = DeleteSerieByIdCommand(serieId)

        val repositoryException = RuntimeException("Database deletion failed")

        every { validator1.execute(serieId) } just Runs
        every { validator2.execute(serieId) } just Runs
        every { repository.deleteSerieById(serieId) } throws repositoryException

        // When & Then
        assertThrows<RuntimeException> {
            useCase.execute(command)
        }

        verify(exactly = 1) { validator1.execute(serieId) }
        verify(exactly = 1) { validator2.execute(serieId) }
        verify(exactly = 1) { repository.deleteSerieById(serieId) }
    }

    @Test
    fun `should call executeValidation with correct serie id`() {
        // Given
        val expectedId = UUID.randomUUID()
        val command = DeleteSerieByIdCommand(expectedId)

        every { validator1.execute(expectedId) } just Runs
        every { validator2.execute(expectedId) } just Runs
        every { repository.deleteSerieById(expectedId) } just Runs

        // When
        useCase.execute(command)

        // Then
        verify(exactly = 1) { validator1.execute(expectedId) }
        verify(exactly = 1) { validator2.execute(expectedId) }
        verify(exactly = 1) { repository.deleteSerieById(expectedId) }
        confirmVerified(repository, validator1, validator2)
    }

    @Test
    fun `should extract id from command correctly`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = DeleteSerieByIdCommand(serieId)

        every { validator1.execute(any()) } just Runs
        every { validator2.execute(any()) } just Runs
        every { repository.deleteSerieById(any()) } just Runs

        // When
        useCase.execute(command)

        // Then
        verify(exactly = 1) { validator1.execute(serieId) }
        verify(exactly = 1) { validator2.execute(serieId) }
        verify(exactly = 1) { repository.deleteSerieById(serieId) }
    }

    @Test
    fun `should work with empty validators list`() {
        // Given
        val serieId = UUID.randomUUID()
        val command = DeleteSerieByIdCommand(serieId)
        val emptyValidators = emptyList<ValidatorStrategy<UUID>>()
        val useCaseWithEmptyValidators = DeleteSerieByIdUsecaseImpl(repository, emptyValidators)

        every { repository.deleteSerieById(serieId) } just Runs

        // When
        useCaseWithEmptyValidators.execute(command)

        // Then
        verify(exactly = 1) { repository.deleteSerieById(serieId) }
        verify(exactly = 0) { validator1.execute(any()) }
        verify(exactly = 0) { validator2.execute(any()) }
    }

    @Test
    fun `should handle different UUID formats correctly`() {
        // Given
        val randomUUID = UUID.randomUUID()
        val specificUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val command1 = DeleteSerieByIdCommand(randomUUID)
        val command2 = DeleteSerieByIdCommand(specificUUID)

        every { validator1.execute(any()) } just Runs
        every { validator2.execute(any()) } just Runs
        every { repository.deleteSerieById(any()) } just Runs

        // When
        useCase.execute(command1)
        useCase.execute(command2)

        // Then
        verify(exactly = 1) { validator1.execute(randomUUID) }
        verify(exactly = 1) { validator2.execute(randomUUID) }
        verify(exactly = 1) { repository.deleteSerieById(randomUUID) }

        verify(exactly = 1) { validator1.execute(specificUUID) }
        verify(exactly = 1) { validator2.execute(specificUUID) }
        verify(exactly = 1) { repository.deleteSerieById(specificUUID) }
    }
}