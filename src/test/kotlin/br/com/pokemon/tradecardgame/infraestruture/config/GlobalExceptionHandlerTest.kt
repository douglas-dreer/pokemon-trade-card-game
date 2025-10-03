package br.com.pokemon.tradecardgame.infraestruture.config

import br.com.pokemon.tradecardgame.domain.exception.InvalidDataException
import br.com.pokemon.tradecardgame.domain.exception.SeriesAlreadyExistsException
import br.com.pokemon.tradecardgame.domain.exception.SeriesNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.WebRequest
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GlobalExceptionHandlerTest {

    private lateinit var handler: GlobalExceptionHandler
    private val webRequest = mockk<WebRequest>()

    @BeforeEach
    fun setUp() {
        handler = GlobalExceptionHandler()
        every { webRequest.getDescription(false) } returns "uri=/test"
    }

    @Test
    fun `should handle SeriesNotFoundException with 404 status`() {
        // Given
        val serieId = UUID.randomUUID()
        val exception = SeriesNotFoundException(serieId)

        // When
        val response = handler.handleNotFoundException(exception, webRequest)

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(response.body)
        assertEquals(404, response.body!!.status)
        assertEquals("Not Found", response.body!!.error)
        assertEquals("The series '$serieId' was not found in the system.", response.body!!.message)
        assertNotNull(response.body!!.timestamp)
    }

    @Test
    fun `should handle SeriesAlreadyExistsException with 409 status`() {
        // Given
        val serieName = "Existing Serie"
        val exception = SeriesAlreadyExistsException(serieName)

        // When
        val response = handler.handleConflictException(exception, webRequest)

        // Then
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertNotNull(response.body)
        assertEquals(409, response.body!!.status)
        assertEquals("Conflict", response.body!!.error)
        assertEquals("The series '$serieName' already exists in the system.", response.body!!.message)
        assertNotNull(response.body!!.timestamp)
    }

    @Test
    fun `should handle InvalidDataException with 400 status`() {
        // Given
        val errorMessage = "Invalid data provided"
        val exception = InvalidDataException(errorMessage)

        // When
        val response = handler.handleBadRequestDomainException(exception, webRequest)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body!!.status)
        assertEquals("Bad Request", response.body!!.error)
        assertEquals(errorMessage, response.body!!.message)
        assertNotNull(response.body!!.timestamp)
    }

    @Test
    fun `should handle exception with null message`() {
        // Given
        val exception = InvalidDataException("")

        // When
        val response = handler.handleBadRequestDomainException(exception, webRequest)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Erro interno desconhecido.", response.body!!.message)
    }

    @Test
    fun `should create error response with correct timestamp`() {
        // Given
        val exception = InvalidDataException("Test error")
        val beforeRequest = System.currentTimeMillis()

        // When
        val response = handler.handleBadRequestDomainException(exception, webRequest)
        val afterRequest = System.currentTimeMillis()

        // Then
        assertNotNull(response.body!!.timestamp)
        val responseTime = response.body!!.timestamp.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        assert(responseTime >= beforeRequest - 1000) // Allow 1 second tolerance
        assert(responseTime <= afterRequest + 1000)
    }

    @Test
    fun `should handle different series not found scenarios`() {
        // Given
        val serieId1 = UUID.randomUUID()
        val serieId2 = UUID.randomUUID()
        val exception1 = SeriesNotFoundException(serieId1)
        val exception2 = SeriesNotFoundException(serieId2)

        // When
        val response1 = handler.handleNotFoundException(exception1, webRequest)
        val response2 = handler.handleNotFoundException(exception2, webRequest)

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response1.statusCode)
        assertEquals(HttpStatus.NOT_FOUND, response2.statusCode)
        assertEquals("The series '$serieId1' was not found in the system.", response1.body!!.message)
        assertEquals("The series '$serieId2' was not found in the system.", response2.body!!.message)
    }

    @Test
    fun `should handle different series already exists scenarios`() {
        // Given
        val serieName1 = "Serie 1"
        val serieName2 = "Serie 2"
        val exception1 = SeriesAlreadyExistsException(serieName1)
        val exception2 = SeriesAlreadyExistsException(serieName2)

        // When
        val response1 = handler.handleConflictException(exception1, webRequest)
        val response2 = handler.handleConflictException(exception2, webRequest)

        // Then
        assertEquals(HttpStatus.CONFLICT, response1.statusCode)
        assertEquals(HttpStatus.CONFLICT, response2.statusCode)
        assertEquals("The series '$serieName1' already exists in the system.", response1.body!!.message)
        assertEquals("The series '$serieName2' already exists in the system.", response2.body!!.message)
    }

    @Test
    fun `should maintain consistent error response structure`() {
        // Given
        val exception = InvalidDataException("Test error")

        // When
        val response = handler.handleBadRequestDomainException(exception, webRequest)

        // Then
        assertNotNull(response.body)
        val errorResponse = response.body!!

        // Verify all required fields are present
        assertNotNull(errorResponse.timestamp)
        assertNotNull(errorResponse.status)
        assertNotNull(errorResponse.error)
        assertNotNull(errorResponse.message)

        // Verify field types and values
        assertEquals(400, errorResponse.status)
        assertEquals("Bad Request", errorResponse.error)
        assertEquals("Test error", errorResponse.message)
    }
}