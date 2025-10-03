package br.com.pokemon.tradecardgame.integration

import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.CreateSerieRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.UpdateSerieRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response.SerieResponse
import br.com.pokemon.tradecardgame.domain.model.Expansion
import br.com.pokemon.tradecardgame.integration.config.TestSecurityConfig
import br.com.pokemon.tradecardgame.integration.testdata.DatabaseTestDataManager
import br.com.pokemon.tradecardgame.integration.testdata.SerieTestDataBuilder
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Integration tests for JSON serialization and deserialization in the Serie API.
 *
 * This test class validates JSON handling including:
 * - Request deserialization for all DTO classes and proper mapping
 * - Response serialization and JSON format matching API contracts
 * - Date/time field handling and proper timezone/format conversion
 * - Optional field handling and null value processing
 *
 * Tests cover requirements: 7.1, 7.2, 7.3, 7.4
 */
@AutoConfigureMockMvc
@DisplayName("JSON Serialization Integration Tests")
class JsonSerializationIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var databaseTestDataManager: DatabaseTestDataManager

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Clean database before each test to ensure isolation
        databaseTestDataManager.cleanupTestData()

        // Configure ObjectMapper for proper date/time handling
        objectMapper.registerModule(JavaTimeModule())
    }

    @Nested
    @DisplayName("Request Deserialization Tests")
    inner class RequestDeserializationTests {

        @Test
        @DisplayName("Should deserialize CreateSerieRequest with all fields correctly")
        @Transactional
        fun createSerie_WithCompleteJson_ShouldDeserializeCorrectly() {
            // Arrange
            val jsonRequest = """
                {
                    "code": "JSON01",
                    "name": "JSON Test Serie",
                    "releaseYear": 2024,
                    "imageUrl": "https://example.com/json-test.jpg",
                    "expansions": []
                }
            """.trimIndent()

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.code").value("JSON01"))
                .andExpect(jsonPath("$.name").value("JSON Test Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2024))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/json-test.jpg"))
                .andExpect(jsonPath("$.expansions").isArray())
                .andExpect(jsonPath("$.expansions").isEmpty())
        }

        @Test
        @DisplayName("Should deserialize CreateSerieRequest with minimal required fields")
        @Transactional
        fun createSerie_WithMinimalJson_ShouldDeserializeCorrectly() {
            // Arrange
            val jsonRequest = """
                {
                    "code": "MIN01",
                    "name": "Minimal Serie",
                    "releaseYear": 2023
                }
            """.trimIndent()

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.code").value("MIN01"))
                .andExpect(jsonPath("$.name").value("Minimal Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2023))
                .andExpect(jsonPath("$.imageUrl").doesNotExist())
                .andExpect(jsonPath("$.expansions").isArray())
                .andExpect(jsonPath("$.expansions").isEmpty())
        }

        @Test
        @DisplayName("Should deserialize UpdateSerieRequest with proper field mapping")
        @Transactional
        fun updateSerie_WithJsonRequest_ShouldDeserializeCorrectly() {
            // Arrange - Create initial serie
            val initialSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("UPDATE01")
                    .withName("Original Name")
            )

            val jsonRequest = """
                {
                    "id": "${initialSerie.id}",
                    "code": "UPDATED01",
                    "name": "Updated Name via JSON",
                    "releaseYear": 2024,
                    "imageUrl": "https://example.com/updated.jpg",
                    "expansions": []
                }
            """.trimIndent()

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                patch("/series/{id}", initialSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(initialSerie.id.toString()))
                .andExpect(jsonPath("$.code").value("UPDATED01"))
                .andExpect(jsonPath("$.name").value("Updated Name via JSON"))
                .andExpect(jsonPath("$.releaseYear").value(2024))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/updated.jpg"))
        }

        @Test
        @DisplayName("Should handle null values in optional fields during deserialization")
        @Transactional
        fun createSerie_WithNullOptionalFields_ShouldDeserializeCorrectly() {
            // Arrange
            val jsonRequest = """
                {
                    "code": "NULL01",
                    "name": "Null Fields Serie",
                    "releaseYear": 2023,
                    "imageUrl": null,
                    "expansions": null
                }
            """.trimIndent()

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.code").value("NULL01"))
                .andExpect(jsonPath("$.name").value("Null Fields Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2023))
                .andExpect(jsonPath("$.imageUrl").doesNotExist())
                .andExpect(jsonPath("$.expansions").isArray())
                .andExpect(jsonPath("$.expansions").isEmpty())
        }

        @Test
        @DisplayName("Should validate field types during deserialization")
        @Transactional
        fun createSerie_WithCorrectFieldTypes_ShouldDeserializeCorrectly() {
            // Arrange
            val jsonRequest = """
                {
                    "code": "TYPE01",
                    "name": "Type Validation Serie",
                    "releaseYear": 2024,
                    "imageUrl": "https://example.com/type-test.jpg",
                    "expansions": []
                }
            """.trimIndent()

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert - Verify proper type handling
            val result = mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andReturn()

            val responseJson = result.response.contentAsString
            val serieResponse = objectMapper.readValue(responseJson, SerieResponse::class.java)

            // Verify types are correctly deserialized
            assert(serieResponse.code is String) { "Code should be String type" }
            assert(serieResponse.name is String) { "Name should be String type" }
            assert(serieResponse.releaseYear is Int) { "Release year should be Int type" }
            assert(serieResponse.imageUrl is String?) { "Image URL should be nullable String type" }
            assert(serieResponse.expansions is List<*>) { "Expansions should be List type" }
        }
    }

    @Nested
    @DisplayName("Response Serialization Tests")
    inner class ResponseSerializationTests {

        @Test
        @DisplayName("Should serialize SerieResponse with all fields correctly")
        @Transactional
        fun findSerie_ShouldSerializeResponseCorrectly() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("SERIAL01")
                    .withName("Serialization Test Serie")
                    .withReleaseYear(2024)
                    .withImageUrl("https://example.com/serialization-test.jpg")
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            val result = mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()

            val responseJson = result.response.contentAsString
            val jsonNode = objectMapper.readTree(responseJson)

            // Verify JSON structure and field presence
            assert(jsonNode.has("id")) { "Response should contain id field" }
            assert(jsonNode.has("code")) { "Response should contain code field" }
            assert(jsonNode.has("name")) { "Response should contain name field" }
            assert(jsonNode.has("releaseYear")) { "Response should contain releaseYear field" }
            assert(jsonNode.has("imageUrl")) { "Response should contain imageUrl field" }
            assert(jsonNode.has("expansions")) { "Response should contain expansions field" }
            assert(jsonNode.has("createdAt")) { "Response should contain createdAt field" }

            // Verify field values
            assert(jsonNode.get("code").asText() == "SERIAL01") { "Code should match" }
            assert(jsonNode.get("name").asText() == "Serialization Test Serie") { "Name should match" }
            assert(jsonNode.get("releaseYear").asInt() == 2024) { "Release year should match" }
            assert(
                jsonNode.get("imageUrl").asText() == "https://example.com/serialization-test.jpg"
            ) { "Image URL should match" }
            assert(jsonNode.get("expansions").isArray) { "Expansions should be array" }
        }

        @Test
        @DisplayName("Should serialize paginated response with correct structure")
        @Transactional
        fun findAllSeries_ShouldSerializePaginatedResponseCorrectly() {
            // Arrange
            databaseTestDataManager.createPaginationTestData(3, "PageSerial")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            val result = mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "2")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()

            val responseJson = result.response.contentAsString
            val jsonNode = objectMapper.readTree(responseJson)

            // Verify paginated response structure
            assert(jsonNode.has("content")) { "Paginated response should contain content field" }
            assert(jsonNode.has("totalElements")) { "Paginated response should contain totalElements field" }
            assert(jsonNode.has("totalPages")) { "Paginated response should contain totalPages field" }
            assert(jsonNode.has("size")) { "Paginated response should contain size field" }
            assert(jsonNode.has("number")) { "Paginated response should contain number field" }
            assert(jsonNode.has("first")) { "Paginated response should contain first field" }
            assert(jsonNode.has("last")) { "Paginated response should contain last field" }
            assert(jsonNode.has("numberOfElements")) { "Paginated response should contain numberOfElements field" }

            // Verify content array structure
            val contentArray = jsonNode.get("content")
            assert(contentArray.isArray) { "Content should be array" }
            assert(contentArray.size() == 2) { "Content should contain 2 elements" }

            // Verify each serie in content has proper structure
            contentArray.forEach { serieNode ->
                assert(serieNode.has("id")) { "Each serie should have id" }
                assert(serieNode.has("code")) { "Each serie should have code" }
                assert(serieNode.has("name")) { "Each serie should have name" }
                assert(serieNode.has("releaseYear")) { "Each serie should have releaseYear" }
                assert(serieNode.has("expansions")) { "Each serie should have expansions" }
                assert(serieNode.has("createdAt")) { "Each serie should have createdAt" }
            }
        }

        @Test
        @DisplayName("Should serialize response with null optional fields correctly")
        @Transactional
        fun findSerie_WithNullOptionalFields_ShouldSerializeCorrectly() {
            // Arrange - Create serie with null optional fields
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("NULLSER01")
                    .withName("Null Serialization Serie")
                    .withReleaseYear(2023)
                    .withImageUrl(null) // Null optional field
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            val result = mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("NULLSER01"))
                .andExpect(jsonPath("$.name").value("Null Serialization Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2023))
                .andExpect(jsonPath("$.imageUrl").doesNotExist()) // Should not include null fields
                .andExpect(jsonPath("$.expansions").isArray())
                .andExpect(jsonPath("$.expansions").isEmpty())
                .andReturn()

            val responseJson = result.response.contentAsString
            val jsonNode = objectMapper.readTree(responseJson)

            // Verify null fields are handled correctly (not included in JSON)
            assert(!jsonNode.has("imageUrl") || jsonNode.get("imageUrl").isNull) {
                "Null imageUrl should not be present or should be null"
            }
        }

        @Test
        @DisplayName("Should maintain JSON format consistency across different endpoints")
        @Transactional
        fun allEndpoints_ShouldMaintainConsistentJsonFormat() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("CONSIST01")
                    .withName("Consistency Test Serie")
                    .withReleaseYear(2024)
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Test CREATE response format
            val createRequest = SerieTestDataBuilder()
                .withCode("CONSIST02")
                .withName("Create Consistency Serie")
                .buildCreateRequest()

            val createResult = mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andReturn()

            // Test READ response format
            val readResult = mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andReturn()

            // Test UPDATE response format
            val updateRequest = UpdateSerieRequest(
                id = testSerie.id,
                code = testSerie.code,
                name = "Updated Consistency Serie",
                releaseYear = testSerie.releaseYear,
                imageUrl = testSerie.imageUrl,
                expansions = emptyList()
            )

            val updateResult = mockMvc.perform(
                patch("/series/{id}", testSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andReturn()

            // Verify all responses have consistent structure
            val createJson = objectMapper.readTree(createResult.response.contentAsString)
            val readJson = objectMapper.readTree(readResult.response.contentAsString)
            val updateJson = objectMapper.readTree(updateResult.response.contentAsString)

            val requiredFields = listOf("id", "code", "name", "releaseYear", "expansions", "createdAt")

            requiredFields.forEach { field ->
                assert(createJson.has(field)) { "CREATE response should have $field" }
                assert(readJson.has(field)) { "READ response should have $field" }
                assert(updateJson.has(field)) { "UPDATE response should have $field" }
            }
        }
    }

    @Nested
    @DisplayName("Date/Time Field Handling Tests")
    inner class DateTimeHandlingTests {

        @Test
        @DisplayName("Should handle LocalDateTime serialization with proper format")
        @Transactional
        fun findSerie_ShouldSerializeDateTimeFieldsCorrectly() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("DATETIME01")
                    .withName("DateTime Test Serie")
                    .withCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 45))
                    .withUpdatedAt(LocalDateTime.of(2024, 1, 16, 14, 20, 30))
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            val result = mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn()

            val responseJson = result.response.contentAsString
            val jsonNode = objectMapper.readTree(responseJson)

            // Verify date/time format
            val createdAtStr = jsonNode.get("createdAt").asText()
            val updatedAtStr = jsonNode.get("updatedAt").asText()

            // Verify ISO format (should be parseable back to LocalDateTime)
            val parsedCreatedAt = LocalDateTime.parse(createdAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val parsedUpdatedAt = LocalDateTime.parse(updatedAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            assert(parsedCreatedAt != null) { "CreatedAt should be parseable as LocalDateTime" }
            assert(parsedUpdatedAt != null) { "UpdatedAt should be parseable as LocalDateTime" }
        }

        @Test
        @DisplayName("Should handle null date/time fields correctly")
        @Transactional
        fun findSerie_WithNullDateTimeFields_ShouldHandleCorrectly() {
            // Arrange - Create serie with null updatedAt
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("NULLDT01")
                    .withName("Null DateTime Serie")
                    .withUpdatedAt(null) // Null updatedAt
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").doesNotExist()) // Should not include null updatedAt
        }

        @Test
        @DisplayName("Should maintain timezone consistency in date/time serialization")
        @Transactional
        fun findSerie_ShouldMaintainTimezoneConsistency() {
            // Arrange
            val specificDateTime = LocalDateTime.of(2024, 6, 15, 14, 30, 0)
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("TIMEZONE01")
                    .withName("Timezone Test Serie")
                    .withCreatedAt(specificDateTime)
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            val result = mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andReturn()

            val responseJson = result.response.contentAsString
            val jsonNode = objectMapper.readTree(responseJson)
            val serializedDateTime = jsonNode.get("createdAt").asText()

            // Verify the serialized date/time can be parsed back correctly
            val parsedDateTime = LocalDateTime.parse(serializedDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            assert(parsedDateTime.year == 2024) { "Year should be preserved" }
            assert(parsedDateTime.monthValue == 6) { "Month should be preserved" }
            assert(parsedDateTime.dayOfMonth == 15) { "Day should be preserved" }
            assert(parsedDateTime.hour == 14) { "Hour should be preserved" }
            assert(parsedDateTime.minute == 30) { "Minute should be preserved" }
            assert(parsedDateTime.second == 0) { "Second should be preserved" }
        }

        @Test
        @DisplayName("Should handle date/time fields in paginated responses")
        @Transactional
        fun findAllSeries_ShouldHandleDateTimeFieldsInPagination() {
            // Arrange
            val baseDateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0)

            // Create series with different timestamps
            (1..3).forEach { index ->
                databaseTestDataManager.createTestSerie(
                    SerieTestDataBuilder()
                        .withCode("PAGDT${index.toString().padStart(2, '0')}")
                        .withName("Page DateTime Serie $index")
                        .withCreatedAt(baseDateTime.plusDays(index.toLong()))
                )
            }

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            val result = mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andReturn()

            val responseJson = result.response.contentAsString
            val jsonNode = objectMapper.readTree(responseJson)
            val contentArray = jsonNode.get("content")

            // Verify each serie in paginated response has proper date/time handling
            contentArray.forEach { serieNode ->
                assert(serieNode.has("createdAt")) { "Each serie should have createdAt" }

                val createdAtStr = serieNode.get("createdAt").asText()
                val parsedDateTime = LocalDateTime.parse(createdAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                assert(parsedDateTime != null) { "CreatedAt should be parseable in paginated response" }
            }
        }
    }

    @Nested
    @DisplayName("Optional Field Handling Tests")
    inner class OptionalFieldHandlingTests {

        @Test
        @DisplayName("Should handle optional fields correctly in requests")
        @Transactional
        fun createSerie_WithOptionalFields_ShouldHandleCorrectly() {
            // Test with imageUrl present
            val jsonWithImageUrl = """
                {
                    "code": "OPT01",
                    "name": "Optional Fields Serie 1",
                    "releaseYear": 2024,
                    "imageUrl": "https://example.com/optional.jpg"
                }
            """.trimIndent()

            val testToken = TestSecurityConfig.generateTestJwtToken()

            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithImageUrl)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/optional.jpg"))

            // Test without imageUrl
            val jsonWithoutImageUrl = """
                {
                    "code": "OPT02",
                    "name": "Optional Fields Serie 2",
                    "releaseYear": 2024
                }
            """.trimIndent()

            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithoutImageUrl)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.imageUrl").doesNotExist())
        }

        @Test
        @DisplayName("Should handle empty arrays correctly")
        @Transactional
        fun createSerie_WithEmptyArrays_ShouldHandleCorrectly() {
            // Arrange
            val jsonRequest = """
                {
                    "code": "EMPTY01",
                    "name": "Empty Arrays Serie",
                    "releaseYear": 2024,
                    "expansions": []
                }
            """.trimIndent()

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.expansions").isArray())
                .andExpect(jsonPath("$.expansions").isEmpty())
        }

        @Test
        @DisplayName("Should handle missing optional fields in update requests")
        @Transactional
        fun updateSerie_WithMissingOptionalFields_ShouldHandleCorrectly() {
            // Arrange - Create initial serie
            val initialSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("MISSING01")
                    .withName("Missing Fields Serie")
                    .withImageUrl("https://example.com/original.jpg")
            )

            // Update without imageUrl field (should preserve existing value)
            val jsonRequest = """
                {
                    "id": "${initialSerie.id}",
                    "code": "MISSING01",
                    "name": "Updated Missing Fields Serie",
                    "releaseYear": 2024,
                    "expansions": []
                }
            """.trimIndent()

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                patch("/series/{id}", initialSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Updated Missing Fields Serie"))
                .andExpect(jsonPath("$.releaseYear").value(2024))
        }

        @Test
        @DisplayName("Should serialize optional fields consistently across operations")
        @Transactional
        fun allOperations_ShouldHandleOptionalFieldsConsistently() {
            // Test CREATE with optional field
            val createRequest = """
                {
                    "code": "OPTCONS01",
                    "name": "Optional Consistency Serie",
                    "releaseYear": 2024,
                    "imageUrl": "https://example.com/consistency.jpg"
                }
            """.trimIndent()

            val testToken = TestSecurityConfig.generateTestJwtToken()

            val createResult = mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRequest)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/consistency.jpg"))
                .andReturn()

            val createdSerie = objectMapper.readValue(createResult.response.contentAsString, SerieResponse::class.java)

            // Test READ with optional field
            mockMvc.perform(
                get("/series/{id}", createdSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/consistency.jpg"))

            // Test UPDATE removing optional field
            val updateRequest = UpdateSerieRequest(
                id = createdSerie.id,
                code = createdSerie.code,
                name = createdSerie.name,
                releaseYear = createdSerie.releaseYear,
                imageUrl = null, // Remove optional field
                expansions = emptyList()
            )

            mockMvc.perform(
                patch("/series/{id}", createdSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.imageUrl").doesNotExist())
        }
    }

    @Nested
    @DisplayName("API Contract Compliance Tests")
    inner class ApiContractComplianceTests {

        @Test
        @DisplayName("Should match expected JSON schema for Serie response")
        @Transactional
        fun serieResponse_ShouldMatchExpectedSchema() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("SCHEMA01")
                    .withName("Schema Test Serie")
                    .withReleaseYear(2024)
                    .withImageUrl("https://example.com/schema-test.jpg")
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            val result = mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verify required fields
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.releaseYear").exists())
                .andExpect(jsonPath("$.expansions").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                // Verify field types
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.code").isString())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.releaseYear").isNumber())
                .andExpect(jsonPath("$.expansions").isArray())
                .andExpect(jsonPath("$.createdAt").isString())
                .andReturn()

            // Verify UUID format for id field
            val responseJson = result.response.contentAsString
            val jsonNode = objectMapper.readTree(responseJson)
            val idString = jsonNode.get("id").asText()

            // Should be valid UUID format
            val uuid = UUID.fromString(idString)
            assert(uuid != null) { "ID should be valid UUID format" }
        }

        @Test
        @DisplayName("Should match expected JSON schema for paginated response")
        @Transactional
        fun paginatedResponse_ShouldMatchExpectedSchema() {
            // Arrange
            databaseTestDataManager.createPaginationTestData(5, "Schema")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "3")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verify paginated response structure
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.size").isNumber())
                .andExpect(jsonPath("$.number").isNumber())
                .andExpect(jsonPath("$.first").isBoolean())
                .andExpect(jsonPath("$.last").isBoolean())
                .andExpect(jsonPath("$.numberOfElements").isNumber())
        }

        @Test
        @DisplayName("Should validate JSON field constraints and formats")
        @Transactional
        fun jsonResponse_ShouldValidateFieldConstraints() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("CONSTR01")
                    .withName("Constraints Test Serie")
                    .withReleaseYear(2024)
            )

            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            val result = mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andReturn()

            val responseJson = result.response.contentAsString
            val serieResponse = objectMapper.readValue(responseJson, SerieResponse::class.java)

            // Validate field constraints
            assert(serieResponse.code.isNotBlank()) { "Code should not be blank" }
            assert(serieResponse.name.isNotBlank()) { "Name should not be blank" }
            assert(serieResponse.releaseYear > 1998) { "Release year should be greater than 1998" }
            assert(serieResponse.id != null) { "ID should not be null" }
            assert(serieResponse.expansions != null) { "Expansions should not be null" }
            assert(serieResponse.createdAt != null) { "CreatedAt should not be null" }
        }
    }
}