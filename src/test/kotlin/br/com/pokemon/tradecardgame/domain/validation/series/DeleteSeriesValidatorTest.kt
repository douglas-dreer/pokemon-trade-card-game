package br.com.pokemon.tradecardgame.domain.validation.series

import br.com.pokemon.tradecardgame.domain.exception.SeriesNotFoundException
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.SeriesJpaAdapter
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteSeriesValidatorTest {

    private val repository = mockk<SeriesJpaAdapter>()
    private lateinit var validator: DeleteSeriesValidator

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        validator = DeleteSeriesValidator(repository)
    }

    @Test
    fun `should validate successfully when serie exists`() {
        // Given
        val serieId = UUID.randomUUID()

        every { repository.existsSerieById(serieId) } returns true

        // When & Then - Should not throw any exception
        validator.execute(serieId)

        // Then
        verify(exactly = 1) { repository.existsSerieById(serieId) }
    }

    @Test
    fun `should throw SeriesNotFoundException when serie does not exist`() {
        // Given
        val serieId = UUID.randomUUID()

        every { repository.existsSerieById(serieId) } returns false

        // When & Then
        val exception = assertThrows<SeriesNotFoundException> {
            validator.execute(serieId)
        }

        assertEquals("The series '$serieId' was not found in the system.", exception.message)
        verify(exactly = 1) { repository.existsSerieById(serieId) }
    }

    @Test
    fun `should validate different UUID formats`() {
        // Given
        val randomUUID = UUID.randomUUID()
        val specificUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

        every { repository.existsSerieById(randomUUID) } returns true
        every { repository.existsSerieById(specificUUID) } returns true

        // When & Then - Both should validate successfully
        validator.execute(randomUUID)
        validator.execute(specificUUID)

        verify(exactly = 1) { repository.existsSerieById(randomUUID) }
        verify(exactly = 1) { repository.existsSerieById(specificUUID) }
    }

    @Test
    fun `should handle multiple validations with different results`() {
        // Given
        val existingId = UUID.randomUUID()
        val nonExistingId = UUID.randomUUID()

        every { repository.existsSerieById(existingId) } returns true
        every { repository.existsSerieById(nonExistingId) } returns false

        // When & Then
        // First validation should pass
        validator.execute(existingId)

        // Second validation should fail
        assertThrows<SeriesNotFoundException> {
            validator.execute(nonExistingId)
        }

        verify(exactly = 1) { repository.existsSerieById(existingId) }
        verify(exactly = 1) { repository.existsSerieById(nonExistingId) }
    }

    @Test
    fun `should handle repository exceptions gracefully`() {
        // Given
        val serieId = UUID.randomUUID()

        every { repository.existsSerieById(serieId) } throws RuntimeException("Database connection failed")

        // When & Then
        assertThrows<RuntimeException> {
            validator.execute(serieId)
        }

        verify(exactly = 1) { repository.existsSerieById(serieId) }
    }

    @Test
    fun `should validate multiple existing series`() {
        // Given
        val serieId1 = UUID.randomUUID()
        val serieId2 = UUID.randomUUID()
        val serieId3 = UUID.randomUUID()

        every { repository.existsSerieById(serieId1) } returns true
        every { repository.existsSerieById(serieId2) } returns true
        every { repository.existsSerieById(serieId3) } returns true

        // When & Then - All should validate successfully
        validator.execute(serieId1)
        validator.execute(serieId2)
        validator.execute(serieId3)

        verify(exactly = 1) { repository.existsSerieById(serieId1) }
        verify(exactly = 1) { repository.existsSerieById(serieId2) }
        verify(exactly = 1) { repository.existsSerieById(serieId3) }
    }

    @Test
    fun `should validate multiple non-existing series`() {
        // Given
        val serieId1 = UUID.randomUUID()
        val serieId2 = UUID.randomUUID()
        val serieId3 = UUID.randomUUID()

        every { repository.existsSerieById(serieId1) } returns false
        every { repository.existsSerieById(serieId2) } returns false
        every { repository.existsSerieById(serieId3) } returns false

        // When & Then - All should throw exceptions
        assertThrows<SeriesNotFoundException> {
            validator.execute(serieId1)
        }

        assertThrows<SeriesNotFoundException> {
            validator.execute(serieId2)
        }

        assertThrows<SeriesNotFoundException> {
            validator.execute(serieId3)
        }

        verify(exactly = 1) { repository.existsSerieById(serieId1) }
        verify(exactly = 1) { repository.existsSerieById(serieId2) }
        verify(exactly = 1) { repository.existsSerieById(serieId3) }
    }

    @Test
    fun `should call repository with exact UUID parameter`() {
        // Given
        val expectedId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        every { repository.existsSerieById(expectedId) } returns true

        // When
        validator.execute(expectedId)

        // Then
        verify(exactly = 1) { repository.existsSerieById(expectedId) }
        confirmVerified(repository)
    }

    @Test
    fun `should handle edge case with nil UUID`() {
        // Given
        val nilUUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

        every { repository.existsSerieById(nilUUID) } returns false

        // When & Then
        assertThrows<SeriesNotFoundException> {
            validator.execute(nilUUID)
        }

        verify(exactly = 1) { repository.existsSerieById(nilUUID) }
    }

    @Test
    fun `should handle concurrent validations`() {
        // Given
        val serieId1 = UUID.randomUUID()
        val serieId2 = UUID.randomUUID()

        every { repository.existsSerieById(serieId1) } returns true
        every { repository.existsSerieById(serieId2) } returns false

        // When & Then - Simulate concurrent access
        validator.execute(serieId1) // Should pass

        assertThrows<SeriesNotFoundException> {
            validator.execute(serieId2) // Should fail
        }

        verify(exactly = 1) { repository.existsSerieById(serieId1) }
        verify(exactly = 1) { repository.existsSerieById(serieId2) }
    }

    @Test
    fun `should validate same UUID multiple times`() {
        // Given
        val serieId = UUID.randomUUID()

        every { repository.existsSerieById(serieId) } returns true

        // When & Then - Multiple validations of same ID should all pass
        validator.execute(serieId)
        validator.execute(serieId)
        validator.execute(serieId)

        verify(exactly = 3) { repository.existsSerieById(serieId) }
    }

    @Test
    fun `should handle repository returning different results for same UUID`() {
        // Given
        val serieId = UUID.randomUUID()

        // First call returns true, second returns false (simulating concurrent deletion)
        every { repository.existsSerieById(serieId) } returnsMany listOf(true, false)

        // When & Then
        validator.execute(serieId) // Should pass

        assertThrows<SeriesNotFoundException> {
            validator.execute(serieId) // Should fail
        }

        verify(exactly = 2) { repository.existsSerieById(serieId) }
    }

    @Test
    fun `should preserve exception details`() {
        // Given
        val serieId = UUID.randomUUID()

        every { repository.existsSerieById(serieId) } returns false

        // When & Then
        val exception = assertThrows<SeriesNotFoundException> {
            validator.execute(serieId)
        }

        assertEquals("The series '$serieId' was not found in the system.", exception.message)
        assertTrue(exception.message.contains(serieId.toString()))
        verify(exactly = 1) { repository.existsSerieById(serieId) }
    }

    @Test
    fun `should handle timeout or slow repository responses`() {
        // Given
        val serieId = UUID.randomUUID()

        // Simulate slow response
        every { repository.existsSerieById(serieId) } answers {
            Thread.sleep(10) // Small delay to simulate slow response
            true
        }

        // When & Then - Should still work despite delay
        validator.execute(serieId)

        verify(exactly = 1) { repository.existsSerieById(serieId) }
    }

    @Test
    fun `should validate with different exception types from repository`() {
        // Given
        val serieId1 = UUID.randomUUID()
        val serieId2 = UUID.randomUUID()
        val serieId3 = UUID.randomUUID()

        every { repository.existsSerieById(serieId1) } throws RuntimeException("Connection timeout")
        every { repository.existsSerieById(serieId2) } throws IllegalStateException("Invalid state")
        every { repository.existsSerieById(serieId3) } throws Exception("Generic exception")

        // When & Then - All should propagate their respective exceptions
        assertThrows<RuntimeException> {
            validator.execute(serieId1)
        }

        assertThrows<IllegalStateException> {
            validator.execute(serieId2)
        }

        assertThrows<Exception> {
            validator.execute(serieId3)
        }

        verify(exactly = 1) { repository.existsSerieById(serieId1) }
        verify(exactly = 1) { repository.existsSerieById(serieId2) }
        verify(exactly = 1) { repository.existsSerieById(serieId3) }
    }
}