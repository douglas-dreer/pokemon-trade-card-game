package br.com.pokemon.tradecardgame.domain.validation.series

import br.com.pokemon.tradecardgame.domain.exception.InvalidDataException
import br.com.pokemon.tradecardgame.domain.exception.SeriesAlreadyExistsException
import br.com.pokemon.tradecardgame.domain.exception.SeriesNotFoundException
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.SeriesJpaAdapter
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class UpdateSeriesValidatorTest {

    private val repository = mockk<SeriesJpaAdapter>()
    private lateinit var validator: UpdateSeriesValidator

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        validator = UpdateSeriesValidator(repository)
    }

    @Test
    fun `should validate successfully when all conditions are met`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "UPDATE01",
            name = "Updated Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(serieId) } returns true
        every { repository.existsSerieByCode("UPDATE01") } returns false
        every { repository.existsSerieByName("Updated Serie") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        // Then
        verify(exactly = 1) { repository.existsSerieById(serieId) }
        verify(exactly = 1) { repository.existsSerieByCode("UPDATE01") }
        verify(exactly = 1) { repository.existsSerieByName("Updated Serie") }
    }

    @Test
    fun `should throw InvalidDataException when serie id is null`() {
        // Given
        val serie = Serie(
            id = null,
            code = "NULL_ID",
            name = "Null ID Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        // When & Then
        val exception = assertThrows<InvalidDataException> {
            validator.execute(serie)
        }

        assertEquals("Invalid serie id", exception.message)
        verify(exactly = 0) { repository.existsSerieById(any()) }
        verify(exactly = 0) { repository.existsSerieByCode(any()) }
        verify(exactly = 0) { repository.existsSerieByName(any()) }
    }

    @Test
    fun `should throw SeriesNotFoundException when serie does not exist`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "NOT_FOUND",
            name = "Not Found Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(serieId) } returns false

        // When & Then
        val exception = assertThrows<SeriesNotFoundException> {
            validator.execute(serie)
        }

        assertEquals("The series '$serieId' was not found in the system.", exception.message)
        verify(exactly = 1) { repository.existsSerieById(serieId) }
        verify(exactly = 0) { repository.existsSerieByCode(any()) }
        verify(exactly = 0) { repository.existsSerieByName(any()) }
    }

    @Test
    fun `should throw SeriesAlreadyExistsException when code already exists`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "EXISTING_CODE",
            name = "New Name",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(serieId) } returns true
        every { repository.existsSerieByCode("EXISTING_CODE") } returns true

        // When & Then
        val exception = assertThrows<SeriesAlreadyExistsException> {
            validator.execute(serie)
        }

        assertEquals("The series 'EXISTING_CODE' already exists in the system.", exception.message)
        verify(exactly = 1) { repository.existsSerieById(serieId) }
        verify(exactly = 1) { repository.existsSerieByCode("EXISTING_CODE") }
        verify(exactly = 0) { repository.existsSerieByName(any()) } // Should not check name if code exists
    }

    @Test
    fun `should throw SeriesAlreadyExistsException when name already exists`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "NEW_CODE",
            name = "Existing Name",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(serieId) } returns true
        every { repository.existsSerieByCode("NEW_CODE") } returns false
        every { repository.existsSerieByName("Existing Name") } returns true

        // When & Then
        val exception = assertThrows<SeriesAlreadyExistsException> {
            validator.execute(serie)
        }

        assertEquals("The series 'Existing Name' already exists in the system.", exception.message)
        verify(exactly = 1) { repository.existsSerieById(serieId) }
        verify(exactly = 1) { repository.existsSerieByCode("NEW_CODE") }
        verify(exactly = 1) { repository.existsSerieByName("Existing Name") }
    }

    @Test
    fun `should validate in correct order - id, code, then name`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "ORDER_TEST",
            name = "Order Test Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(serieId) } returns true
        every { repository.existsSerieByCode("ORDER_TEST") } returns false
        every { repository.existsSerieByName("Order Test Serie") } returns false

        // When
        validator.execute(serie)

        // Then - Verify the order of calls
        verifyOrder {
            repository.existsSerieById(serieId)
            repository.existsSerieByCode("ORDER_TEST")
            repository.existsSerieByName("Order Test Serie")
        }
    }

    @Test
    fun `should stop validation at first failure - id check`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "STOP_AT_ID",
            name = "Stop At ID Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(serieId) } returns false

        // When & Then
        assertThrows<SeriesNotFoundException> {
            validator.execute(serie)
        }

        verify(exactly = 1) { repository.existsSerieById(serieId) }
        verify(exactly = 0) { repository.existsSerieByCode(any()) }
        verify(exactly = 0) { repository.existsSerieByName(any()) }
    }

    @Test
    fun `should stop validation at first failure - code check`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "STOP_AT_CODE",
            name = "Stop At Code Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(serieId) } returns true
        every { repository.existsSerieByCode("STOP_AT_CODE") } returns true

        // When & Then
        assertThrows<SeriesAlreadyExistsException> {
            validator.execute(serie)
        }

        verify(exactly = 1) { repository.existsSerieById(serieId) }
        verify(exactly = 1) { repository.existsSerieByCode("STOP_AT_CODE") }
        verify(exactly = 0) { repository.existsSerieByName(any()) }
    }

    @Test
    fun `should handle case sensitive validations`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "case_sensitive",
            name = "Case Sensitive Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(serieId) } returns true
        every { repository.existsSerieByCode("case_sensitive") } returns false
        every { repository.existsSerieByName("Case Sensitive Serie") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        verify(exactly = 1) { repository.existsSerieByCode("case_sensitive") }
        verify(exactly = 1) { repository.existsSerieByName("Case Sensitive Serie") }
    }

    @Test
    fun `should handle special characters in code and name`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "SV-02_UPDATE",
            name = "Scarlet & Violet: Updated Edition (2023)",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(serieId) } returns true
        every { repository.existsSerieByCode("SV-02_UPDATE") } returns false
        every { repository.existsSerieByName("Scarlet & Violet: Updated Edition (2023)") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        verify(exactly = 1) { repository.existsSerieByCode("SV-02_UPDATE") }
        verify(exactly = 1) { repository.existsSerieByName("Scarlet & Violet: Updated Edition (2023)") }
    }

    @Test
    fun `should validate serie with all optional fields`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "FULL_UPDATE",
            name = "Full Update Serie",
            releaseYear = 2023,
            imageUrl = "https://example.com/updated.jpg",
            expansions = emptyList(),
            createdAt = LocalDateTime.now().minusDays(5),
            updatedAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(serieId) } returns true
        every { repository.existsSerieByCode("FULL_UPDATE") } returns false
        every { repository.existsSerieByName("Full Update Serie") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        verify(exactly = 1) { repository.existsSerieById(serieId) }
        verify(exactly = 1) { repository.existsSerieByCode("FULL_UPDATE") }
        verify(exactly = 1) { repository.existsSerieByName("Full Update Serie") }
    }

    @Test
    fun `should handle repository exceptions gracefully`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "ERROR_TEST",
            name = "Error Test Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(serieId) } throws RuntimeException("Database connection failed")

        // When & Then
        assertThrows<RuntimeException> {
            validator.execute(serie)
        }

        verify(exactly = 1) { repository.existsSerieById(serieId) }
        verify(exactly = 0) { repository.existsSerieByCode(any()) }
        verify(exactly = 0) { repository.existsSerieByName(any()) }
    }

    @Test
    fun `should handle different UUID formats`() {
        // Given
        val specificUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val serie = Serie(
            id = specificUUID,
            code = "UUID_TEST",
            name = "UUID Test Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(specificUUID) } returns true
        every { repository.existsSerieByCode("UUID_TEST") } returns false
        every { repository.existsSerieByName("UUID Test Serie") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        verify(exactly = 1) { repository.existsSerieById(specificUUID) }
        verify(exactly = 1) { repository.existsSerieByCode("UUID_TEST") }
        verify(exactly = 1) { repository.existsSerieByName("UUID Test Serie") }
    }

    @Test
    fun `should call repository methods with exact parameters`() {
        // Given
        val expectedId = UUID.randomUUID()
        val expectedCode = "EXACT_CODE"
        val expectedName = "Exact Name"
        val serie = Serie(
            id = expectedId,
            code = expectedCode,
            name = expectedName,
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieById(expectedId) } returns true
        every { repository.existsSerieByCode(expectedCode) } returns false
        every { repository.existsSerieByName(expectedName) } returns false

        // When
        validator.execute(serie)

        // Then
        verify(exactly = 1) { repository.existsSerieById(expectedId) }
        verify(exactly = 1) { repository.existsSerieByCode(expectedCode) }
        verify(exactly = 1) { repository.existsSerieByName(expectedName) }
        confirmVerified(repository)
    }

    @Test
    fun `should validate multiple updates with different series`() {
        // Given
        val serieId1 = UUID.randomUUID()
        val serieId2 = UUID.randomUUID()
        val serie1 = Serie(serieId1, "CODE1", "Name 1", 2023, createdAt = LocalDateTime.now())
        val serie2 = Serie(serieId2, "CODE2", "Name 2", 2023, createdAt = LocalDateTime.now())

        every { repository.existsSerieById(any()) } returns true
        every { repository.existsSerieByCode(any()) } returns false
        every { repository.existsSerieByName(any()) } returns false

        // When & Then - Both should validate successfully
        validator.execute(serie1)
        validator.execute(serie2)

        verify(exactly = 1) { repository.existsSerieById(serieId1) }
        verify(exactly = 1) { repository.existsSerieById(serieId2) }
        verify(exactly = 1) { repository.existsSerieByCode("CODE1") }
        verify(exactly = 1) { repository.existsSerieByCode("CODE2") }
        verify(exactly = 1) { repository.existsSerieByName("Name 1") }
        verify(exactly = 1) { repository.existsSerieByName("Name 2") }
    }
}