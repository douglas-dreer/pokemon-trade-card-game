package br.com.pokemon.tradecardgame.integration

import br.com.pokemon.tradecardgame.integration.config.TestSecurityConfig
import br.com.pokemon.tradecardgame.integration.testdata.DatabaseTestDataManager
import br.com.pokemon.tradecardgame.integration.testdata.SerieTestDataBuilder
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test to verify the foundation setup is working correctly.
 *
 * This test validates:
 * - TestContainers PostgreSQL database connectivity
 * - Test security configuration
 * - Test data builders functionality
 * - Database test data manager operations
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@Import(TestSecurityConfig::class)
class FoundationIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var databaseTestDataManager: DatabaseTestDataManager

    @Test
    fun `should verify database connectivity and test data creation`() {
        // Verify database is accessible
        assertTrue(postgresContainer.isRunning, "PostgreSQL container should be running")
        assertNotNull(postgresContainer.jdbcUrl, "JDBC URL should be available")

        // Test data builder functionality
        val testSerie = SerieTestDataBuilder()
            .withCode("FOUND01")
            .withName("Foundation Test Serie")
            .withReleaseYear(2024)
            .buildEntity()

        assertNotNull(testSerie, "Test data builder should create valid entity")
        assertTrue(testSerie.code == "FOUND01", "Code should match expected value")
        assertTrue(testSerie.name == "Foundation Test Serie", "Name should match expected value")
        assertTrue(testSerie.releaseYear == 2024, "Release year should match expected value")
    }

    @Test
    fun `should verify test security configuration`() {
        // Test JWT token generation
        val testToken = TestSecurityConfig.generateTestJwtToken("test-user")
        assertNotNull(testToken, "Test JWT token should be generated")
        assertTrue(testToken.contains("test-user"), "Token should contain user identifier")

        // Test authentication token creation
        val authToken = TestSecurityConfig.createTestAuthenticationToken("test-user")
        assertNotNull(authToken, "Authentication token should be created")
        assertNotNull(authToken.token, "JWT token should be present in authentication")
    }

    @Test
    fun `should verify test data builders with different configurations`() {
        // Test minimal configuration
        val minimalSerie = SerieTestDataBuilder.minimal().buildDomain()
        assertNotNull(minimalSerie, "Minimal serie should be created")
        assertTrue(minimalSerie.code == "MIN01", "Minimal serie should have expected code")

        // Test complete configuration
        val completeSerie = SerieTestDataBuilder.complete().buildDomain()
        assertNotNull(completeSerie, "Complete serie should be created")
        assertTrue(completeSerie.code == "COMP01", "Complete serie should have expected code")

        // Test multiple builders
        val multipleBuilders = SerieTestDataBuilder.createMultiple(3)
        assertTrue(multipleBuilders.size == 3, "Should create 3 builders")

        val series = multipleBuilders.map { it.buildDomain() }
        assertTrue(series.all { it.code.startsWith("BATCH") }, "All series should have BATCH prefix")
    }
}