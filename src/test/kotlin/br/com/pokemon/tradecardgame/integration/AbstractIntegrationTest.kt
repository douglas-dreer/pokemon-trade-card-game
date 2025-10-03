package br.com.pokemon.tradecardgame.integration

import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Abstract base class for integration tests that provides common configuration
 * and setup for testing with TestContainers PostgreSQL database.
 *
 * This class configures:
 * - TestContainers PostgreSQL database with proper lifecycle management
 * - Spring Boot test environment with random port
 * - Integration test profile activation
 * - Dynamic database connection properties
 * - Transactional test execution with rollback
 *
 * All integration test classes should extend this base class to inherit
 * the common test infrastructure and database setup.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@Transactional
abstract class AbstractIntegrationTest {

    companion object {
        /**
         * PostgreSQL TestContainer instance that provides an isolated database
         * for integration tests. The container is shared across all test methods
         * in the test class for performance optimization.
         */
        @Container
        @JvmStatic
        val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("pokemon_tcg_integration_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true)

        /**
         * Configures dynamic properties for the Spring application context
         * based on the TestContainer database connection details.
         *
         * This method is called by Spring to set up the database connection
         * properties dynamically after the container is started.
         *
         * @param registry The dynamic property registry to configure database properties
         */
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
            registry.add("spring.datasource.username") { postgresContainer.username }
            registry.add("spring.datasource.password") { postgresContainer.password }
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
        }
    }

    /**
     * Setup method executed before each test method.
     * Can be overridden by subclasses to provide additional test setup.
     */
    @BeforeEach
    fun setUp() {
        // Base setup - can be extended by subclasses
    }
}