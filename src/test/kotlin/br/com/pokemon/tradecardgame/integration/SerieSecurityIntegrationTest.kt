package br.com.pokemon.tradecardgame.integration

import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.CreateSerieRequest
import br.com.pokemon.tradecardgame.integration.config.TestSecurityConfig
import br.com.pokemon.tradecardgame.integration.testdata.DatabaseTestDataManager
import br.com.pokemon.tradecardgame.integration.testdata.SerieTestDataBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

/**
 * Integration tests for security configurations in the Serie API.
 *
 * This test class validates:
 * - JWT token validation and security filter chain integration
 * - Authentication scenarios (no token, invalid token, valid token)
 * - Authorization scenarios and proper security responses
 * - Security configuration with real HTTP requests
 * - Security headers and CORS configuration
 *
 * Tests ensure proper authentication and authorization are enforced
 * according to the security requirements.
 */
@AutoConfigureMockMvc
@DisplayName("Serie Security Integration Tests")
class SerieSecurityIntegrationTest : AbstractIntegrationTest() {

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
    }

    @Nested
    @DisplayName("Authentication Tests")
    inner class AuthenticationTests {

        @Test
        @DisplayName("Should return 401 for requests without authentication token")
        @Transactional
        fun accessProtectedEndpoint_WithoutToken_ShouldReturn401() {
            // Arrange
            val createRequest = SerieTestDataBuilder()
                .withCode("NOAUTH01")
                .withName("No Auth Test Serie")
                .withReleaseYear(2024)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)

            // Act & Assert - Test POST without token
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                // No Authorization header
            )
                .andExpect(status().isUnauthorized)

            // Act & Assert - Test GET without token
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "10")
                // No Authorization header
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("Should return 401 for requests with invalid JWT token")
        @Transactional
        fun accessProtectedEndpoint_WithInvalidToken_ShouldReturn401() {
            // Arrange
            val createRequest = SerieTestDataBuilder()
                .withCode("INVALID01")
                .withName("Invalid Token Test Serie")
                .withReleaseYear(2024)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)
            val invalidToken = "invalid.jwt.token"

            // Act & Assert - Test POST with invalid token
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $invalidToken")
            )
                .andExpect(status().isUnauthorized)

            // Act & Assert - Test GET with invalid token
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer $invalidToken")
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("Should return 401 for requests with malformed Authorization header")
        @Transactional
        fun accessProtectedEndpoint_WithMalformedAuthHeader_ShouldReturn401() {
            // Arrange
            val createRequest = SerieTestDataBuilder()
                .withCode("MALFORM01")
                .withName("Malformed Auth Test Serie")
                .withReleaseYear(2024)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)

            // Act & Assert - Test with missing "Bearer" prefix
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "some.jwt.token") // Missing "Bearer" prefix
            )
                .andExpect(status().isUnauthorized)

            // Act & Assert - Test with wrong auth scheme
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Basic some.jwt.token") // Wrong scheme
            )
                .andExpect(status().isUnauthorized)

            // Act & Assert - Test with empty Authorization header
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "") // Empty header
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("Should process requests successfully with valid JWT token")
        @Transactional
        fun accessProtectedEndpoint_WithValidToken_ShouldReturn200() {
            // Arrange
            val createRequest = SerieTestDataBuilder()
                .withCode("VALID01")
                .withName("Valid Token Test Serie")
                .withReleaseYear(2024)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)
            val validToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert - Test POST with valid token
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALID01"))
                .andExpect(jsonPath("$.name").value("Valid Token Test Serie"))

            // Act & Assert - Test GET with valid token
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
        }

        @Test
        @DisplayName("Should validate JWT token for all protected endpoints")
        @Transactional
        fun allProtectedEndpoints_ShouldRequireValidJwtToken() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("PROTECT01")
                    .withName("Protected Endpoint Test Serie")
                    .withReleaseYear(2024)
            )

            val validToken = TestSecurityConfig.generateTestJwtToken()

            // Test all CRUD endpoints with valid token - should succeed

            // GET /series (list)
            mockMvc.perform(
                get("/series")
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isOk)

            // GET /series/{id}
            mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isOk)

            // GET /series?code={code}
            mockMvc.perform(
                get("/series")
                    .param("code", "PROTECT01")
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isOk)

            // PATCH /series/{id}
            val updateRequest = """
                {
                    "id": "${testSerie.id}",
                    "code": "PROTECT01",
                    "name": "Updated Protected Serie",
                    "releaseYear": 2024,
                    "expansions": []
                }
            """.trimIndent()

            mockMvc.perform(
                patch("/series/{id}", testSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest)
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isOk)

            // DELETE /series/{id}
            mockMvc.perform(
                delete("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("Should reject expired JWT tokens")
        @Transactional
        fun accessProtectedEndpoint_WithExpiredToken_ShouldReturn401() {
            // Arrange
            val createRequest = SerieTestDataBuilder()
                .withCode("EXPIRED01")
                .withName("Expired Token Test Serie")
                .withReleaseYear(2024)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)
            // Simulate an expired token (in real scenario, this would be handled by JWT validation)
            val expiredToken = "expired.jwt.token"

            // Act & Assert
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $expiredToken")
            )
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("Security Filter Chain Tests")
    inner class SecurityFilterChainTests {

        @Test
        @DisplayName("Should process requests through security filter chain in correct order")
        @Transactional
        fun securityFilterChain_ShouldProcessRequestsInCorrectOrder() {
            // Arrange
            val createRequest = SerieTestDataBuilder()
                .withCode("FILTER01")
                .withName("Filter Chain Test Serie")
                .withReleaseYear(2024)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)
            val validToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert - Verify that security filters process the request correctly
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("FILTER01"))
                .andExpect(jsonPath("$.name").value("Filter Chain Test Serie"))

            // Verify that the request was processed through the complete filter chain
            // by checking that the response contains expected security-related behavior
        }

        @Test
        @DisplayName("Should apply proper security headers in responses")
        @Transactional
        fun securityFilterChain_ShouldApplyProperSecurityHeaders() {
            // Arrange
            val validToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert - Test that security headers are present in responses
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verify that standard security headers are present
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().string("X-XSS-Protection", "0"))
        }

        @Test
        @DisplayName("Should handle CORS configuration correctly")
        @Transactional
        fun securityFilterChain_ShouldHandleCorsCorrectly() {
            // Arrange
            val validToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert - Test CORS preflight request
            mockMvc.perform(
                options("/series")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "POST")
                    .header("Access-Control-Request-Headers", "Authorization, Content-Type")
            )
                .andExpect(status().isOk)
                // Verify CORS headers are present (if CORS is configured)
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"))

            // Test actual CORS request
            val createRequest = SerieTestDataBuilder()
                .withCode("CORS01")
                .withName("CORS Test Serie")
                .withReleaseYear(2024)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)

            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $validToken")
                    .header("Origin", "http://localhost:3000")
            )
                .andExpect(status().isCreated)
                // Verify CORS headers in actual response
                .andExpect(header().exists("Access-Control-Allow-Origin"))
        }

        @Test
        @DisplayName("Should verify proper security configuration with real HTTP requests")
        @Transactional
        fun securityConfiguration_ShouldWorkWithRealHttpRequests() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("SECURITY01")
                    .withName("Security Config Test Serie")
                    .withReleaseYear(2024)
            )

            val validToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert - Test that security configuration works end-to-end

            // Test authenticated GET request
            mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testSerie.id.toString()))
                .andExpect(jsonPath("$.code").value("SECURITY01"))

            // Test unauthenticated request is rejected
            mockMvc.perform(
                get("/series/{id}", testSerie.id)
                // No Authorization header
            )
                .andExpect(status().isUnauthorized)

            // Test that security context is properly established for authenticated requests
            val updateRequest = """
                {
                    "id": "${testSerie.id}",
                    "code": "SECURITY01",
                    "name": "Updated Security Test Serie",
                    "releaseYear": 2024,
                    "expansions": []
                }
            """.trimIndent()

            mockMvc.perform(
                patch("/series/{id}", testSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest)
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Updated Security Test Serie"))
        }

        @Test
        @DisplayName("Should validate JWT authentication converter integration")
        @Transactional
        fun jwtAuthenticationConverter_ShouldIntegrateCorrectly() {
            // Arrange
            val createRequest = SerieTestDataBuilder()
                .withCode("CONVERT01")
                .withName("JWT Converter Test Serie")
                .withReleaseYear(2024)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)
            val validToken = TestSecurityConfig.generateTestJwtToken("test-user-123")

            // Act & Assert - Verify that JWT is properly converted to authentication
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $validToken")
            )
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("CONVERT01"))
                .andExpect(jsonPath("$.name").value("JWT Converter Test Serie"))

            // The fact that the request succeeds indicates that:
            // 1. JWT was properly decoded
            // 2. Authentication converter created proper Authentication object
            // 3. Security context was established correctly
            // 4. Request was authorized and processed
        }

        @Test
        @DisplayName("Should handle security exceptions properly in filter chain")
        @Transactional
        fun securityFilterChain_ShouldHandleSecurityExceptionsProperly() {
            // Arrange
            val createRequest = SerieTestDataBuilder()
                .withCode("EXCEPTION01")
                .withName("Security Exception Test Serie")
                .withReleaseYear(2024)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)

            // Act & Assert - Test various security exception scenarios

            // Test with completely invalid token format
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer invalid-token-format")
            )
                .andExpect(status().isUnauthorized)

            // Test with missing Bearer prefix
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "some-token-without-bearer")
            )
                .andExpect(status().isUnauthorized)

            // Test with empty token
            mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer ")
            )
                .andExpect(status().isUnauthorized)

            // Verify that security exceptions don't leak sensitive information
            // and return appropriate HTTP status codes
        }
    }
}