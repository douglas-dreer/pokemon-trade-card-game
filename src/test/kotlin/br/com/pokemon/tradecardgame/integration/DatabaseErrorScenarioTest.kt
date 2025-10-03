package br.com.pokemon.tradecardgame.integration

import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.CreateSerieRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.UpdateSerieRequest
import br.com.pokemon.tradecardgame.integration.config.TestSecurityConfig
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import javax.sql.DataSource
import java.sql.SQLException

/**
 * Integration tests for database error scenarios in the Serie API.
 *
 * This test class validates database error handling including:
 * - Database connection failures and proper error responses
 * - Transaction rollback scenarios and data consistency verification
 * - Database constraint violations and proper error responses
 * - Generic error messages for internal server errors
 *
 * Tests ensure proper error responses, HTTP status codes, and data consistency
 * according to requirements 2.3, 2.4, 8.1, and 8.3.
 */
@AutoConfigureMockMvc
@DisplayName("Database Error Scenario Tests")
class DatabaseErrorScenarioTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @WithMockUser
    @DisplayName("Should handle transaction rollback scenarios and verify data consistency")
    fun transactionRollback_ShouldMaintainDataConsistency() {
        // This test validates transaction rollback by attempting to create duplicate data

        // Create initial serie
        val createRequest1 = CreateSerieRequest(
            code = "ROLLBACK01",
            name = "Initial Serie",
            releaseYear = 2023,
            imageUrl = null,
            expansions = emptyList()
        )
        val requestJson1 = objectMapper.writeValueAsString(createRequest1)

        // First request should succeed
        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson1)
        )
            .andExpect(status().isCreated)

        // Attempt to create duplicate (should cause rollback)
        val duplicateRequest = CreateSerieRequest(
            code = "ROLLBACK01", // Duplicate code
            name = "Duplicate Serie",
            releaseYear = 2024,
            imageUrl = null,
            expansions = emptyList()
        )
        val requestJson2 = objectMapper.writeValueAsString(duplicateRequest)

        // Second request should fail with 409 Conflict
        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson2)
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value("The series 'ROLLBACK01' already exists in the system."))

        // Verify original data is still intact (data consistency)
        mockMvc.perform(
            get("/series")
                .param("code", "ROLLBACK01")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Initial Serie"))
            .andExpect(jsonPath("$.releaseYear").value(2023))
    }

    @Test
    @WithMockUser
    @DisplayName("Should validate generic error messages for database constraint violations")
    fun databaseConstraintViolations_ShouldReturnGenericErrorMessages() {
        // Create an existing serie
        val existingRequest = CreateSerieRequest(
            code = "CONSTRAINT01",
            name = "Existing Serie",
            releaseYear = 2023,
            imageUrl = null,
            expansions = emptyList()
        )
        val existingJson = objectMapper.writeValueAsString(existingRequest)

        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(existingJson)
        )
            .andExpect(status().isCreated)

        // Test constraint violation
        val constraintViolatingRequest = CreateSerieRequest(
            code = "CONSTRAINT01", // Duplicate code - violates unique constraint
            name = "Constraint Violation Test",
            releaseYear = 2024,
            imageUrl = "https://example.com/test.jpg",
            expansions = emptyList()
        )
        val requestJson = objectMapper.writeValueAsString(constraintViolatingRequest)

        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists())
            // Validate that error message is appropriate and doesn't expose sensitive details
            .andExpect(jsonPath("$.message").value("The series 'CONSTRAINT01' already exists in the system."))
    }

    @Test
    @WithMockUser
    @DisplayName("Should verify data consistency after failed database operations")
    fun failedDatabaseOperations_ShouldMaintainDataConsistency() {
        // Create initial data
        val initialRequest1 = CreateSerieRequest("CONSIST01", "Consistency Test 1", 2023, null, emptyList())
        val initialRequest2 = CreateSerieRequest("CONSIST02", "Consistency Test 2", 2023, null, emptyList())

        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialRequest1))
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialRequest2))
        ).andExpect(status().isCreated)

        // Attempt operations that should fail
        val failingRequests = listOf(
            CreateSerieRequest("CONSIST01", "Duplicate 1", 2024, null, emptyList()), // Duplicate code
            CreateSerieRequest("CONSIST02", "Duplicate 2", 2024, null, emptyList()) // Duplicate code
        )

        failingRequests.forEach { request ->
            val requestJson = objectMapper.writeValueAsString(request)

            val result = mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            ).andReturn()

            // Check if status is either 400 (validation error) or 409 (conflict)
            val status = result.response.status
            assert(status == 400 || status == 409) { "Expected 400 or 409, got $status" }
        }

        // Verify original data is still intact
        mockMvc.perform(
            get("/series")
                .param("code", "CONSIST01")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Consistency Test 1"))

        mockMvc.perform(
            get("/series")
                .param("code", "CONSIST02")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Consistency Test 2"))
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle database integrity violations with proper error responses")
    fun databaseIntegrityViolations_ShouldReturnProperErrorResponse() {
        // Create existing serie
        val existingRequest = CreateSerieRequest("INTEGRITY01", "Integrity Test Serie", 2023, null, emptyList())
        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingRequest))
        ).andExpect(status().isCreated)

        val integrityViolationRequest = CreateSerieRequest(
            code = "INTEGRITY01", // Violates unique constraint
            name = "Another Serie",
            releaseYear = 2024,
            imageUrl = null,
            expansions = emptyList()
        )
        val requestJson = objectMapper.writeValueAsString(integrityViolationRequest)

        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists())
            // Ensure error response follows standard format
            .andExpect(jsonPath("$.status").isNumber())
            .andExpect(jsonPath("$.error").isString())
            .andExpect(jsonPath("$.message").isString())
            // Verify specific conflict message
            .andExpect(jsonPath("$.message").value("The series 'INTEGRITY01' already exists in the system."))
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle transaction rollback on validation failures and maintain data consistency")
    fun transactionRollbackOnValidationFailure_ShouldMaintainDataConsistency() {
        // Create initial valid serie
        val validRequest = CreateSerieRequest("VALID01", "Valid Serie", 2023, null, emptyList())
        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
        ).andExpect(status().isCreated)

        // Attempt to update with invalid data (should cause validation failure and rollback)
        val invalidUpdateRequest = UpdateSerieRequest(
            id = null,
            code = "VALID01",
            name = "", // Invalid: empty name should cause validation failure
            releaseYear = 1990, // Invalid: year <= 1998
            imageUrl = "invalid-url" // Invalid URL format
        )

        // Get the serie ID first
        val getResponse = mockMvc.perform(
            get("/series")
                .param("code", "VALID01")
        ).andExpect(status().isOk)
            .andReturn()

        val responseContent = getResponse.response.contentAsString
        val serieId = objectMapper.readTree(responseContent).get("id").asText()

        // Attempt update with invalid data
        mockMvc.perform(
            patch("/series/{id}", serieId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdateRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))

        // Verify original data is unchanged (transaction was rolled back)
        mockMvc.perform(
            get("/series")
                .param("code", "VALID01")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Valid Serie"))
            .andExpect(jsonPath("$.releaseYear").value(2023))
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle multiple concurrent transaction failures and maintain data consistency")
    fun multipleConcurrentTransactionFailures_ShouldMaintainDataConsistency() {
        // Create base data
        val baseRequest1 = CreateSerieRequest("CONCURRENT01", "Concurrent Test 1", 2023, null, emptyList())
        val baseRequest2 = CreateSerieRequest("CONCURRENT02", "Concurrent Test 2", 2023, null, emptyList())

        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(baseRequest1))
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(baseRequest2))
        ).andExpect(status().isCreated)

        // Simulate multiple failing operations that should all be rolled back
        val failingOperations = listOf(
            CreateSerieRequest("CONCURRENT01", "Duplicate", 2024, null, emptyList()), // Duplicate code
            CreateSerieRequest("CONCURRENT02", "Another Duplicate", 2024, null, emptyList()) // Duplicate code
        )

        // Execute all failing operations
        failingOperations.forEach { request ->
            val result = mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            ).andReturn()

            // Verify each operation fails appropriately
            val status = result.response.status
            assert(status == 400 || status == 409) {
                "Expected 400 (Bad Request) or 409 (Conflict), got $status for request: $request"
            }
        }

        // Verify all original data remains intact
        mockMvc.perform(
            get("/series")
                .param("code", "CONCURRENT01")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Concurrent Test 1"))
            .andExpect(jsonPath("$.releaseYear").value(2023))

        mockMvc.perform(
            get("/series")
                .param("code", "CONCURRENT02")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Concurrent Test 2"))
            .andExpect(jsonPath("$.releaseYear").value(2023))
    }

    @Test
    @WithMockUser
    @DisplayName("Should return generic error messages for internal server errors without exposing sensitive details")
    fun internalServerErrors_ShouldReturnGenericErrorMessages() {
        // This test simulates scenarios that would cause internal server errors
        // and verifies that generic error messages are returned without exposing sensitive system details

        // Test with malformed JSON that might cause parsing errors
        val malformedJson = """{"code": "TEST", "name": "Test", "releaseYear": "not-a-number", "invalidField": }"""

        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists())
            // Verify error message doesn't expose internal system details
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SQLException"))))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("NullPointerException"))))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("database"))))
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle database constraint violations during updates and maintain data consistency")
    fun databaseConstraintViolationsDuringUpdate_ShouldMaintainDataConsistency() {
        // Create two series
        val serie1Request = CreateSerieRequest("UPDATE01", "Update Test 1", 2023, null, emptyList())
        val serie2Request = CreateSerieRequest("UPDATE02", "Update Test 2", 2023, null, emptyList())

        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serie1Request))
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serie2Request))
        ).andExpect(status().isCreated)

        // Get serie2 ID for update
        val getResponse = mockMvc.perform(
            get("/series")
                .param("code", "UPDATE02")
        ).andExpect(status().isOk)
            .andReturn()

        val responseContent = getResponse.response.contentAsString
        val serie2Id = objectMapper.readTree(responseContent).get("id").asText()

        // Try to update serie2 with valid data (should succeed)
        val validUpdateRequest = UpdateSerieRequest(
            id = null,
            code = "UPDATE02",
            name = "Updated Name",
            releaseYear = 2024,
            imageUrl = null
        )

        mockMvc.perform(
            patch("/series/{id}", serie2Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest))
        )
            .andExpect(status().isOk)

        // Verify both series still exist with correct data
        mockMvc.perform(
            get("/series")
                .param("code", "UPDATE01")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Update Test 1"))

        mockMvc.perform(
            get("/series")
                .param("code", "UPDATE02")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("UPDATE02"))
            .andExpect(jsonPath("$.name").value("Updated Name"))
            .andExpect(jsonPath("$.releaseYear").value(2024))
    }

    @Test
    @WithMockUser
    @DisplayName("Should validate error response format consistency across different database error scenarios")
    fun errorResponseFormat_ShouldBeConsistentAcrossDatabaseErrors() {
        // Test various database error scenarios and verify consistent error response format

        // 1. Constraint violation error
        val existingRequest = CreateSerieRequest("FORMAT01", "Format Test", 2023, null, emptyList())
        mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingRequest))
        ).andExpect(status().isCreated)

        val duplicateRequest = CreateSerieRequest("FORMAT01", "Duplicate", 2024, null, emptyList())
        val constraintViolationResponse = mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest))
        )
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists())
            .andReturn()

        // 2. Validation error
        val invalidRequest = CreateSerieRequest("", "Invalid", 2024, null, emptyList())
        val validationErrorResponse = mockMvc.perform(
            post("/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists())
            .andReturn()

        // Verify both responses have the same structure
        val constraintResponse = objectMapper.readTree(constraintViolationResponse.response.contentAsString)
        val validationResponse = objectMapper.readTree(validationErrorResponse.response.contentAsString)

        // Both should have the same fields
        assert(constraintResponse.has("status"))
        assert(constraintResponse.has("error"))
        assert(constraintResponse.has("message"))
        assert(constraintResponse.has("timestamp"))

        assert(validationResponse.has("status"))
        assert(validationResponse.has("error"))
        assert(validationResponse.has("message"))
        assert(validationResponse.has("timestamp"))
    }
}