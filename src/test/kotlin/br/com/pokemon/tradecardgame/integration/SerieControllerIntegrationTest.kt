package br.com.pokemon.tradecardgame.integration

import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.CreateSerieRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.UpdateSerieRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response.SerieResponse
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response.SuccessResponse
import br.com.pokemon.tradecardgame.integration.config.TestSecurityConfig
import br.com.pokemon.tradecardgame.integration.testdata.DatabaseTestDataManager
import br.com.pokemon.tradecardgame.integration.testdata.SerieTestDataBuilder
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.repository.SeriesSpringRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

/**
 * Integration tests for SerieController API endpoints.
 *
 * This test class validates the complete flow from HTTP requests through all application
 * layers to the database, ensuring proper functionality, error handling, and API contracts.
 *
 * Tests cover:
 * - CRUD operations with real HTTP requests and database persistence
 * - JSON serialization/deserialization validation
 * - HTTP status codes and response formats
 * - Authentication and authorization
 * - Error scenarios and exception handling
 */
@AutoConfigureMockMvc
@DisplayName("Serie Controller Integration Tests")
class SerieControllerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var serieRepository: SeriesSpringRepository

    @Autowired
    private lateinit var databaseTestDataManager: DatabaseTestDataManager

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Clean database before each test to ensure isolation
        databaseTestDataManager.cleanupTestData()
    }

    @Nested
    @DisplayName("CREATE Serie Endpoint Tests")
    inner class CreateSerieTests {

        @Test
        @DisplayName("Should create serie with valid data and return 201 with Location header")
        @Transactional
        fun createSerie_WithValidData_ShouldReturn201AndPersistToDatabase() {
            // Arrange
            val createRequest = SerieTestDataBuilder()
                .withCode("VALID01")
                .withName("Valid Test Serie")
                .withReleaseYear(2024)
                .withImageUrl("https://example.com/valid-serie.jpg")
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            val result = mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andExpect(header().exists("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALID01"))
                .andExpect(jsonPath("$.name").value("Valid Test Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2024))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/valid-serie.jpg"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").doesNotExist())
                .andReturn()

            // Verify database persistence
            val responseJson = result.response.contentAsString
            val createdSerie = objectMapper.readValue(responseJson, SerieResponse::class.java)

            val persistedEntity = serieRepository.findById(createdSerie.id!!)
            assert(persistedEntity.isPresent) { "Serie should be persisted in database" }

            val entity = persistedEntity.get()
            assert(entity.code == "VALID01") { "Persisted code should match request" }
            assert(entity.name == "Valid Test Serie") { "Persisted name should match request" }
            assert(entity.releaseYear == 2024) { "Persisted release year should match request" }
            assert(entity.imageUrl == "https://example.com/valid-serie.jpg") { "Persisted image URL should match request" }
        }

        @Test
        @DisplayName("Should create serie with minimal required fields and return 201")
        @Transactional
        fun createSerie_WithMinimalRequiredFields_ShouldReturn201AndPersistToDatabase() {
            // Arrange
            val createRequest = SerieTestDataBuilder.minimal()
                .withCode("MIN01")
                .withName("Minimal Serie")
                .withReleaseYear(2000)
                .withImageUrl(null)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andExpect(header().exists("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("MIN01"))
                .andExpect(jsonPath("$.name").value("Minimal Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2000))
                .andExpect(jsonPath("$.imageUrl").doesNotExist())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())

            // Verify database persistence
            val persistedEntity = serieRepository.findSerieEntityByCode("MIN01")
            assert(persistedEntity != null) { "Serie should be persisted in database" }

            val entity = persistedEntity!!
            assert(entity.code == "MIN01") { "Persisted code should match request" }
            assert(entity.name == "Minimal Serie") { "Persisted name should match request" }
            assert(entity.releaseYear == 2000) { "Persisted release year should match request" }
            assert(entity.imageUrl == null) { "Persisted image URL should be null" }
        }

        @Test
        @DisplayName("Should handle optional fields correctly when creating serie")
        @Transactional
        fun createSerie_WithOptionalFields_ShouldHandleCorrectly() {
            // Arrange
            val createRequest = CreateSerieRequest(
                code = "OPT01",
                name = "Optional Fields Serie",
                releaseYear = 2023,
                imageUrl = null, // Optional field set to null
                expansions = emptyList() // Optional field with empty list
            )

            val requestJson = objectMapper.writeValueAsString(createRequest)
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").value("OPT01"))
                .andExpect(jsonPath("$.name").value("Optional Fields Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2023))
                .andExpect(jsonPath("$.imageUrl").doesNotExist())
                .andExpect(jsonPath("$.expansions").isEmpty())

            // Verify Location header format
            val locationHeader = mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson.replace("OPT01", "OPT02")) // Different code to avoid conflict
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andReturn()
                .response
                .getHeader("Location")

            assert(locationHeader != null) { "Location header should be present" }
            assert(locationHeader!!.matches(Regex("/series/[0-9a-f-]{36}"))) {
                "Location header should match UUID pattern: $locationHeader"
            }
        }

        @Test
        @DisplayName("Should validate HTTP 201 status and response body format")
        @Transactional
        fun createSerie_ShouldValidateResponseFormat() {
            // Arrange
            val createRequest = SerieTestDataBuilder()
                .withCode("FORMAT01")
                .withName("Format Test Serie")
                .withReleaseYear(2024)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().exists("Location"))
                // Validate response structure matches SerieResponse
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").isString())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.releaseYear").isNumber())
                .andExpect(jsonPath("$.expansions").isArray())
                .andExpect(jsonPath("$.createdAt").exists())
                // Validate specific values
                .andExpect(jsonPath("$.code").value("FORMAT01"))
                .andExpect(jsonPath("$.name").value("Format Test Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2024))
        }
    }

    @Nested
    @DisplayName("READ Serie Endpoint Tests")
    inner class ReadSerieTests {

        @Test
        @DisplayName("Should find all series with pagination parameters and return 200")
        @Transactional
        fun findAllSeries_WithPaginationParameters_ShouldReturn200AndCorrectData() {
            // Arrange - Create test data
            val testSeries = SerieTestDataBuilder.createMultiple(5)
            testSeries.forEach { builder ->
                databaseTestDataManager.createTestSerie(builder)
            }

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert - Test first page
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "3")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))

            // Act & Assert - Test second page
            mockMvc.perform(
                get("/series")
                    .param("page", "1")
                    .param("size", "3")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true))
        }

        @Test
        @DisplayName("Should find serie by existing ID and return 200 with correct data")
        @Transactional
        fun findSerieById_WithExistingId_ShouldReturn200AndCorrectSerieData() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("FIND01")
                    .withName("Find By ID Serie")
                    .withReleaseYear(2023)
                    .withImageUrl("https://example.com/find-by-id.jpg")
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testSerie.id.toString()))
                .andExpect(jsonPath("$.code").value("FIND01"))
                .andExpect(jsonPath("$.name").value("Find By ID Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2023))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/find-by-id.jpg"))
                .andExpect(jsonPath("$.createdAt").exists())
        }

        @Test
        @DisplayName("Should find serie by existing code and return 200 with correct data")
        @Transactional
        fun findSerieByCode_WithExistingCode_ShouldReturn200AndCorrectSerieData() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("FINDCODE01")
                    .withName("Find By Code Serie")
                    .withReleaseYear(2022)
                    .withImageUrl("https://example.com/find-by-code.jpg")
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                get("/series")
                    .param("code", "FINDCODE01")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testSerie.id.toString()))
                .andExpect(jsonPath("$.code").value("FINDCODE01"))
                .andExpect(jsonPath("$.name").value("Find By Code Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2022))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/find-by-code.jpg"))
                .andExpect(jsonPath("$.createdAt").exists())
        }

        @Test
        @DisplayName("Should validate HTTP 200 status and response body format for all read operations")
        @Transactional
        fun readOperations_ShouldValidateResponseFormat() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("FORMAT01")
                    .withName("Format Test Serie")
                    .withReleaseYear(2024)
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Test findById response format
            mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").isString())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.releaseYear").isNumber())
                .andExpect(jsonPath("$.expansions").isArray())
                .andExpect(jsonPath("$.createdAt").exists())

            // Test findByCode response format
            mockMvc.perform(
                get("/series")
                    .param("code", "FORMAT01")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").isString())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.releaseYear").isNumber())
                .andExpect(jsonPath("$.expansions").isArray())
                .andExpect(jsonPath("$.createdAt").exists())

            // Test findAll response format
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.size").isNumber())
                .andExpect(jsonPath("$.number").isNumber())
                .andExpect(jsonPath("$.first").isBoolean())
                .andExpect(jsonPath("$.last").isBoolean())
        }

        @Test
        @DisplayName("Should handle empty results correctly for pagination")
        @Transactional
        fun findAllSeries_WithEmptyDatabase_ShouldReturnEmptyPage() {
            // Arrange - Ensure database is empty
            databaseTestDataManager.cleanupTestData()
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
        }

        @Test
        @DisplayName("Should verify correct data retrieval with multiple series")
        @Transactional
        fun findAllSeries_WithMultipleSeries_ShouldReturnCorrectDataOrder() {
            // Arrange - Create series with different data
            databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("ALPHA01")
                    .withName("Alpha Serie")
                    .withReleaseYear(2020)
            )

            databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("BETA01")
                    .withName("Beta Serie")
                    .withReleaseYear(2021)
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            val result = mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andReturn()

            // Verify that both series are present in the response
            val responseContent = result.response.contentAsString
            assert(responseContent.contains("ALPHA01")) { "Response should contain ALPHA01" }
            assert(responseContent.contains("BETA01")) { "Response should contain BETA01" }
            assert(responseContent.contains("Alpha Serie")) { "Response should contain Alpha Serie" }
            assert(responseContent.contains("Beta Serie")) { "Response should contain Beta Serie" }
        }
    }

    @Nested
    @DisplayName("UPDATE Serie Endpoint Tests")
    inner class UpdateSerieTests {

        @Test
        @DisplayName("Should update serie with valid data and return 200 with updated response")
        @Transactional
        fun updateSerie_WithValidData_ShouldReturn200AndUpdateDatabase() {
            // Arrange - Create initial serie
            val initialSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("UPDATE01")
                    .withName("Original Serie Name")
                    .withReleaseYear(2020)
                    .withImageUrl("https://example.com/original.jpg")
            )

            val updateRequest = UpdateSerieRequest(
                id = initialSerie.id,
                code = "UPDATED01",
                name = "Updated Serie Name",
                releaseYear = 2024,
                imageUrl = "https://example.com/updated.jpg",
                expansions = emptyList()
            )

            val requestJson = objectMapper.writeValueAsString(updateRequest)
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                patch("/series/{id}", initialSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(initialSerie.id.toString()))
                .andExpect(jsonPath("$.code").value("UPDATED01"))
                .andExpect(jsonPath("$.name").value("Updated Serie Name"))
                .andExpect(jsonPath("$.releaseYear").value(2024))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/updated.jpg"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())

            // Verify database changes
            val updatedEntity = serieRepository.findById(initialSerie.id!!)
            assert(updatedEntity.isPresent) { "Updated serie should exist in database" }

            val entity = updatedEntity.get()
            assert(entity.code == "UPDATED01") { "Code should be updated in database" }
            assert(entity.name == "Updated Serie Name") { "Name should be updated in database" }
            assert(entity.releaseYear == 2024) { "Release year should be updated in database" }
            assert(entity.imageUrl == "https://example.com/updated.jpg") { "Image URL should be updated in database" }
            assert(entity.updatedAt != null) { "Updated timestamp should be set" }
        }

        @Test
        @DisplayName("Should update serie with partial data and return 200")
        @Transactional
        fun updateSerie_WithPartialData_ShouldReturn200AndUpdateOnlyChangedFields() {
            // Arrange - Create initial serie
            val initialSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("PARTIAL01")
                    .withName("Original Name")
                    .withReleaseYear(2020)
                    .withImageUrl("https://example.com/original.jpg")
            )

            // Update only name and release year, keep other fields
            val updateRequest = UpdateSerieRequest(
                id = initialSerie.id,
                code = initialSerie.code, // Keep original code
                name = "Partially Updated Name", // Update name
                releaseYear = 2023, // Update release year
                imageUrl = initialSerie.imageUrl, // Keep original image URL
                expansions = emptyList()
            )

            val requestJson = objectMapper.writeValueAsString(updateRequest)
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                patch("/series/{id}", initialSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(initialSerie.id.toString()))
                .andExpect(jsonPath("$.code").value("PARTIAL01")) // Unchanged
                .andExpect(jsonPath("$.name").value("Partially Updated Name")) // Changed
                .andExpect(jsonPath("$.releaseYear").value(2023)) // Changed
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/original.jpg")) // Unchanged

            // Verify database reflects partial update
            val updatedEntity = serieRepository.findById(initialSerie.id!!)
            assert(updatedEntity.isPresent) { "Updated serie should exist in database" }

            val entity = updatedEntity.get()
            assert(entity.code == "PARTIAL01") { "Code should remain unchanged" }
            assert(entity.name == "Partially Updated Name") { "Name should be updated" }
            assert(entity.releaseYear == 2023) { "Release year should be updated" }
            assert(entity.imageUrl == "https://example.com/original.jpg") { "Image URL should remain unchanged" }
        }

        @Test
        @DisplayName("Should validate HTTP 200 status and updated response body format")
        @Transactional
        fun updateSerie_ShouldValidateResponseFormat() {
            // Arrange
            val initialSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("FORMAT01")
                    .withName("Format Test Serie")
                    .withReleaseYear(2020)
            )

            val updateRequest = UpdateSerieRequest(
                id = initialSerie.id,
                code = "FORMATUP01",
                name = "Updated Format Test Serie",
                releaseYear = 2024,
                imageUrl = "https://example.com/updated-format.jpg",
                expansions = emptyList()
            )

            val requestJson = objectMapper.writeValueAsString(updateRequest)
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                patch("/series/{id}", initialSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Validate response structure matches SerieResponse
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").isString())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.releaseYear").isNumber())
                .andExpect(jsonPath("$.expansions").isArray())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                // Validate specific updated values
                .andExpect(jsonPath("$.code").value("FORMATUP01"))
                .andExpect(jsonPath("$.name").value("Updated Format Test Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2024))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/updated-format.jpg"))
        }

        @Test
        @DisplayName("Should handle null optional fields in update request")
        @Transactional
        fun updateSerie_WithNullOptionalFields_ShouldHandleCorrectly() {
            // Arrange - Create serie with image URL
            val initialSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("NULL01")
                    .withName("Serie with Image")
                    .withReleaseYear(2020)
                    .withImageUrl("https://example.com/original.jpg")
            )

            // Update to remove image URL (set to null)
            val updateRequest = UpdateSerieRequest(
                id = initialSerie.id,
                code = "NULL01",
                name = "Serie without Image",
                releaseYear = 2020,
                imageUrl = null, // Remove image URL
                expansions = emptyList()
            )

            val requestJson = objectMapper.writeValueAsString(updateRequest)
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                patch("/series/{id}", initialSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(initialSerie.id.toString()))
                .andExpect(jsonPath("$.code").value("NULL01"))
                .andExpect(jsonPath("$.name").value("Serie without Image"))
                .andExpect(jsonPath("$.imageUrl").doesNotExist())

            // Verify database reflects null value
            val updatedEntity = serieRepository.findById(initialSerie.id!!)
            assert(updatedEntity.isPresent) { "Updated serie should exist in database" }
            assert(updatedEntity.get().imageUrl == null) { "Image URL should be null in database" }
        }

        @Test
        @DisplayName("Should preserve creation timestamp and update modification timestamp")
        @Transactional
        fun updateSerie_ShouldPreserveCreatedAtAndUpdateUpdatedAt() {
            // Arrange
            val initialSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("TIMESTAMP01")
                    .withName("Timestamp Test Serie")
                    .withReleaseYear(2020)
            )

            val originalCreatedAt = initialSerie.createdAt
            val originalUpdatedAt = initialSerie.updatedAt

            // Wait a small amount to ensure timestamp difference
            Thread.sleep(10)

            val updateRequest = UpdateSerieRequest(
                id = initialSerie.id,
                code = "TIMESTAMP01",
                name = "Updated Timestamp Test Serie",
                releaseYear = 2024,
                imageUrl = null,
                expansions = emptyList()
            )

            val requestJson = objectMapper.writeValueAsString(updateRequest)
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act
            val result = mockMvc.perform(
                patch("/series/{id}", initialSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andReturn()

            // Assert
            val responseJson = result.response.contentAsString
            val updatedSerie = objectMapper.readValue(responseJson, SerieResponse::class.java)

            // Verify timestamps
            assert(updatedSerie.createdAt == originalCreatedAt) {
                "Created timestamp should be preserved: original=$originalCreatedAt, updated=${updatedSerie.createdAt}"
            }
            assert(updatedSerie.updatedAt != null) { "Updated timestamp should be set" }
            assert(updatedSerie.updatedAt != originalUpdatedAt) {
                "Updated timestamp should be different from original: original=$originalUpdatedAt, updated=${updatedSerie.updatedAt}"
            }
        }
    }

    @Nested
    @DisplayName("DELETE Serie Endpoint Tests")
    inner class DeleteSerieTests {

        @Test
        @DisplayName("Should delete serie with existing ID and return 200 with success message")
        @Transactional
        fun deleteSerie_WithExistingId_ShouldReturn200AndRemoveFromDatabase() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("DELETE01")
                    .withName("Serie to Delete")
                    .withReleaseYear(2020)
                    .withImageUrl("https://example.com/delete-me.jpg")
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Verify serie exists before deletion
            assert(serieRepository.findById(testSerie.id!!).isPresent) {
                "Serie should exist before deletion"
            }

            // Act & Assert
            mockMvc.perform(
                delete("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Serie deleted successfully"))
                .andExpect(jsonPath("$.message").value("Serie with id ${testSerie.id} deleted successfully"))

            // Verify serie is removed from database
            assert(serieRepository.findById(testSerie.id!!).isEmpty) {
                "Serie should be removed from database after deletion"
            }
        }

        @Test
        @DisplayName("Should validate HTTP 200 status and success response message format")
        @Transactional
        fun deleteSerie_ShouldValidateSuccessResponseFormat() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("FORMAT01")
                    .withName("Format Test Serie")
                    .withReleaseYear(2020)
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            val result = mockMvc.perform(
                delete("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Validate response structure matches SuccessResponse
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.title").isString())
                .andExpect(jsonPath("$.message").isString())
                // Validate specific success message content
                .andExpect(jsonPath("$.title").value("Serie deleted successfully"))
                .andExpect(jsonPath("$.message").value("Serie with id ${testSerie.id} deleted successfully"))
                .andReturn()

            // Verify response can be deserialized to SuccessResponse
            val responseJson = result.response.contentAsString
            val successResponse = objectMapper.readValue(responseJson, SuccessResponse::class.java)

            assert(successResponse.title == "Serie deleted successfully") {
                "Success response title should match expected value"
            }
            assert(successResponse.message.contains(testSerie.id.toString())) {
                "Success response message should contain the serie ID"
            }
        }

        @Test
        @DisplayName("Should completely remove serie data from database")
        @Transactional
        fun deleteSerie_ShouldCompletelyRemoveSerieData() {
            // Arrange - Create multiple series to ensure only target is deleted
            val serieToDelete = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("REMOVE01")
                    .withName("Serie to Remove")
                    .withReleaseYear(2020)
            )

            val serieToKeep = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("KEEP01")
                    .withName("Serie to Keep")
                    .withReleaseYear(2021)
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Verify both series exist before deletion
            assert(serieRepository.findById(serieToDelete.id!!).isPresent) {
                "Serie to delete should exist before deletion"
            }
            assert(serieRepository.findById(serieToKeep.id!!).isPresent) {
                "Serie to keep should exist before deletion"
            }

            // Act
            mockMvc.perform(
                delete("/series/{id}", serieToDelete.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)

            // Assert - Verify selective deletion
            assert(serieRepository.findById(serieToDelete.id!!).isEmpty) {
                "Deleted serie should not exist in database"
            }
            assert(serieRepository.findById(serieToKeep.id!!).isPresent) {
                "Other series should remain in database"
            }

            // Verify serie cannot be found by code either
            val deletedByCode = serieRepository.findSerieEntityByCode("REMOVE01")
            assert(deletedByCode == null) {
                "Deleted serie should not be findable by code"
            }

            val keptByCode = serieRepository.findSerieEntityByCode("KEEP01")
            assert(keptByCode != null) {
                "Kept serie should still be findable by code"
            }
        }

        @Test
        @DisplayName("Should handle deletion of serie with all field types")
        @Transactional
        fun deleteSerie_WithAllFieldTypes_ShouldRemoveCompletely() {
            // Arrange - Create serie with all possible field values
            val complexSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("COMPLEX01")
                    .withName("Complex Serie with All Fields")
                    .withReleaseYear(2023)
                    .withImageUrl("https://example.com/complex-serie.jpg")
                    .withExpansions(emptyList()) // Would have expansions in real scenario
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Verify complex serie exists with all fields
            val beforeDeletion = serieRepository.findById(complexSerie.id!!)
            assert(beforeDeletion.isPresent) { "Complex serie should exist before deletion" }

            val entity = beforeDeletion.get()
            assert(entity.code == "COMPLEX01") { "Code should be set" }
            assert(entity.name == "Complex Serie with All Fields") { "Name should be set" }
            assert(entity.releaseYear == 2023) { "Release year should be set" }
            assert(entity.imageUrl == "https://example.com/complex-serie.jpg") { "Image URL should be set" }
            assert(entity.createdAt != null) { "Created timestamp should be set" }

            // Act
            mockMvc.perform(
                delete("/series/{id}", complexSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value("Serie deleted successfully"))
                .andExpect(jsonPath("$.message").value("Serie with id ${complexSerie.id} deleted successfully"))

            // Assert - Verify complete removal
            assert(serieRepository.findById(complexSerie.id!!).isEmpty) {
                "Complex serie should be completely removed from database"
            }
        }

        @Test
        @DisplayName("Should maintain database integrity after deletion")
        @Transactional
        fun deleteSerie_ShouldMaintainDatabaseIntegrity() {
            // Arrange - Create multiple series
            val series = SerieTestDataBuilder.createMultiple(3)
            val createdSeries = series.map { builder ->
                databaseTestDataManager.createTestSerie(builder)
            }

            val serieToDelete = createdSeries[1] // Delete middle one
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Verify initial state
            assert(serieRepository.count() == 3L) { "Should have 3 series initially" }

            // Act
            mockMvc.perform(
                delete("/series/{id}", serieToDelete.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)

            // Assert - Verify database integrity
            assert(serieRepository.count() == 2L) { "Should have 2 series after deletion" }

            // Verify remaining series are intact
            val remainingSeries = serieRepository.findAll()
            assert(remainingSeries.size == 2) { "Should find exactly 2 remaining series" }

            val remainingIds = remainingSeries.map { it.id }.toSet()
            assert(serieToDelete.id !in remainingIds) { "Deleted serie ID should not be in remaining series" }
            assert(createdSeries[0].id in remainingIds) { "First serie should remain" }
            assert(createdSeries[2].id in remainingIds) { "Third serie should remain" }
        }
    }
}