package br.com.pokemon.tradecardgame.domain.validation.series

import br.com.pokemon.tradecardgame.domain.exception.SeriesAlreadyExistsException
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.SeriesJpaAdapter
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class CreateSeriesValidatorTest {

    private val repository = mockk<SeriesJpaAdapter>()
    private lateinit var validator: CreateSeriesValidator

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        validator = CreateSeriesValidator(repository)
    }

    @Test
    fun `should validate successfully when serie code and name do not exist`() {
        // Given
        val serie = Serie(
            id = null,
            code = "NEW01",
            name = "New Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode("NEW01") } returns false
        every { repository.existsSerieByName("New Serie") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        // Then
        verify(exactly = 1) { repository.existsSerieByCode("NEW01") }
        verify(exactly = 1) { repository.existsSerieByName("New Serie") }
    }

    @Test
    fun `should throw exception when serie code already exists`() {
        // Given
        val serie = Serie(
            id = null,
            code = "EXISTING_CODE",
            name = "New Serie Name",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode("EXISTING_CODE") } returns true

        // When & Then
        val exception = assertThrows<SeriesAlreadyExistsException> {
            validator.execute(serie)
        }

        assertEquals("The series 'EXISTING_CODE' already exists in the system.", exception.message)
        verify(exactly = 1) { repository.existsSerieByCode("EXISTING_CODE") }
        verify(exactly = 0) { repository.existsSerieByName(any()) } // Should not check name if code exists
    }

    @Test
    fun `should throw exception when serie name already exists`() {
        // Given
        val serie = Serie(
            id = null,
            code = "NEW_CODE",
            name = "Existing Serie Name",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode("NEW_CODE") } returns false
        every { repository.existsSerieByName("Existing Serie Name") } returns true

        // When & Then
        val exception = assertThrows<SeriesAlreadyExistsException> {
            validator.execute(serie)
        }

        assertEquals("The series 'Existing Serie Name' already exists in the system.", exception.message)
        verify(exactly = 1) { repository.existsSerieByCode("NEW_CODE") }
        verify(exactly = 1) { repository.existsSerieByName("Existing Serie Name") }
    }

    @Test
    fun `should throw exception when both code and name exist`() {
        // Given
        val serie = Serie(
            id = null,
            code = "EXISTING_CODE",
            name = "Existing Serie Name",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode("EXISTING_CODE") } returns true

        // When & Then
        val exception = assertThrows<SeriesAlreadyExistsException> {
            validator.execute(serie)
        }

        assertEquals(
            "The series 'EXISTING_CODE' already exists in the system.",
            exception.message
        ) // Should fail on code first
        verify(exactly = 1) { repository.existsSerieByCode("EXISTING_CODE") }
        verify(exactly = 0) { repository.existsSerieByName(any()) } // Should not check name if code exists
    }

    @Test
    fun `should validate serie with null id`() {
        // Given
        val serie = Serie(
            id = null,
            code = "NULL_ID",
            name = "Null ID Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode("NULL_ID") } returns false
        every { repository.existsSerieByName("Null ID Serie") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        verify(exactly = 1) { repository.existsSerieByCode("NULL_ID") }
        verify(exactly = 1) { repository.existsSerieByName("Null ID Serie") }
    }

    @Test
    fun `should validate serie with existing id`() {
        // Given
        val serieId = UUID.randomUUID()
        val serie = Serie(
            id = serieId,
            code = "WITH_ID",
            name = "Serie With ID",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode("WITH_ID") } returns false
        every { repository.existsSerieByName("Serie With ID") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        verify(exactly = 1) { repository.existsSerieByCode("WITH_ID") }
        verify(exactly = 1) { repository.existsSerieByName("Serie With ID") }
    }

    @Test
    fun `should handle case sensitive code validation`() {
        // Given
        val serie = Serie(
            id = null,
            code = "sv01",
            name = "Lowercase Code Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode("sv01") } returns false
        every { repository.existsSerieByName("Lowercase Code Serie") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        verify(exactly = 1) { repository.existsSerieByCode("sv01") }
        verify(exactly = 1) { repository.existsSerieByName("Lowercase Code Serie") }
    }

    @Test
    fun `should handle case sensitive name validation`() {
        // Given
        val serie = Serie(
            id = null,
            code = "CASE01",
            name = "scarlet & violet base set",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode("CASE01") } returns false
        every { repository.existsSerieByName("scarlet & violet base set") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        verify(exactly = 1) { repository.existsSerieByCode("CASE01") }
        verify(exactly = 1) { repository.existsSerieByName("scarlet & violet base set") }
    }

    @Test
    fun `should handle special characters in code and name`() {
        // Given
        val serie = Serie(
            id = null,
            code = "SV-01_SPECIAL",
            name = "Scarlet & Violet: Special Edition (2023)",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode("SV-01_SPECIAL") } returns false
        every { repository.existsSerieByName("Scarlet & Violet: Special Edition (2023)") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        verify(exactly = 1) { repository.existsSerieByCode("SV-01_SPECIAL") }
        verify(exactly = 1) { repository.existsSerieByName("Scarlet & Violet: Special Edition (2023)") }
    }

    @Test
    fun `should validate serie with all optional fields`() {
        // Given
        val serie = Serie(
            id = UUID.randomUUID(),
            code = "FULL01",
            name = "Full Featured Serie",
            releaseYear = 2023,
            imageUrl = "https://example.com/full.jpg",
            expansions = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode("FULL01") } returns false
        every { repository.existsSerieByName("Full Featured Serie") } returns false

        // When & Then - Should not throw any exception
        validator.execute(serie)

        verify(exactly = 1) { repository.existsSerieByCode("FULL01") }
        verify(exactly = 1) { repository.existsSerieByName("Full Featured Serie") }
    }

    @Test
    fun `should handle repository exceptions gracefully`() {
        // Given
        val serie = Serie(
            id = null,
            code = "ERROR01",
            name = "Error Test Serie",
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode("ERROR01") } throws RuntimeException("Database connection failed")

        // When & Then
        assertThrows<RuntimeException> {
            validator.execute(serie)
        }

        verify(exactly = 1) { repository.existsSerieByCode("ERROR01") }
        verify(exactly = 0) { repository.existsSerieByName(any()) }
    }

    @Test
    fun `should validate multiple series with different codes and names`() {
        // Given
        val serie1 = Serie(null, "CODE1", "Name 1", 2023, createdAt = LocalDateTime.now())
        val serie2 = Serie(null, "CODE2", "Name 2", 2023, createdAt = LocalDateTime.now())
        val serie3 = Serie(null, "CODE3", "Name 3", 2023, createdAt = LocalDateTime.now())

        every { repository.existsSerieByCode(any()) } returns false
        every { repository.existsSerieByName(any()) } returns false

        // When & Then - All should validate successfully
        validator.execute(serie1)
        validator.execute(serie2)
        validator.execute(serie3)

        verify(exactly = 1) { repository.existsSerieByCode("CODE1") }
        verify(exactly = 1) { repository.existsSerieByCode("CODE2") }
        verify(exactly = 1) { repository.existsSerieByCode("CODE3") }
        verify(exactly = 1) { repository.existsSerieByName("Name 1") }
        verify(exactly = 1) { repository.existsSerieByName("Name 2") }
        verify(exactly = 1) { repository.existsSerieByName("Name 3") }
    }

    @Test
    fun `should call repository methods with exact parameters`() {
        // Given
        val expectedCode = "EXACT_CODE"
        val expectedName = "Exact Name"
        val serie = Serie(
            id = null,
            code = expectedCode,
            name = expectedName,
            releaseYear = 2023,
            createdAt = LocalDateTime.now()
        )

        every { repository.existsSerieByCode(expectedCode) } returns false
        every { repository.existsSerieByName(expectedName) } returns false

        // When
        validator.execute(serie)

        // Then
        verify(exactly = 1) { repository.existsSerieByCode(expectedCode) }
        verify(exactly = 1) { repository.existsSerieByName(expectedName) }
        confirmVerified(repository)
    }
}