package br.com.pokemon.tradecardgame.integration

import br.com.pokemon.tradecardgame.integration.config.TestSecurityConfig
import br.com.pokemon.tradecardgame.integration.testdata.SerieTestDataBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Simple test to verify the integration test foundation components work correctly
 * without requiring full Spring context or database connectivity.
 */
class SimpleFoundationTest {

    @Test
    fun `should create test data using SerieTestDataBuilder`() {
        // Test basic builder functionality
        val builder = SerieTestDataBuilder()
            .withCode("TEST01")
            .withName("Test Serie")
            .withReleaseYear(2024)

        val domain = builder.buildDomain()
        assertNotNull(domain)
        assertTrue(domain.code == "TEST01")
        assertTrue(domain.name == "Test Serie")
        assertTrue(domain.releaseYear == 2024)

        val entity = builder.buildEntity()
        assertNotNull(entity)
        assertTrue(entity.code == "TEST01")
        assertTrue(entity.name == "Test Serie")
        assertTrue(entity.releaseYear == 2024)

        val createRequest = builder.buildCreateRequest()
        assertNotNull(createRequest)
        assertTrue(createRequest.code == "TEST01")
        assertTrue(createRequest.name == "Test Serie")
        assertTrue(createRequest.releaseYear == 2024)
    }

    @Test
    fun `should create test JWT tokens`() {
        val token = TestSecurityConfig.generateTestJwtToken("test-user")
        assertNotNull(token)
        assertTrue(token.contains("test-user"))

        val authToken = TestSecurityConfig.createTestAuthenticationToken("test-user")
        assertNotNull(authToken)
        assertNotNull(authToken.token)
    }

    @Test
    fun `should create multiple test data builders`() {
        val builders = SerieTestDataBuilder.createMultiple(3)
        assertTrue(builders.size == 3)

        val series = builders.map { it.buildDomain() }
        assertTrue(series.all { it.code.startsWith("BATCH") })
        assertTrue(series.map { it.code }.distinct().size == 3) // All codes should be unique
    }

    @Test
    fun `should create minimal and complete test data`() {
        val minimal = SerieTestDataBuilder.minimal().buildDomain()
        assertNotNull(minimal)
        assertTrue(minimal.code == "MIN01")
        assertTrue(minimal.imageUrl == null)

        val complete = SerieTestDataBuilder.complete().buildDomain()
        assertNotNull(complete)
        assertTrue(complete.code == "COMP01")
        assertNotNull(complete.imageUrl)
    }
}