package br.com.pokemon.tradecardgame.integration.testdata

import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Manages test data lifecycle for integration tests.
 *
 * This component provides utilities for creating, managing, and cleaning up
 * test data in the database during integration tests. It ensures proper
 * data isolation between tests and provides convenient methods for
 * setting up test scenarios.
 *
 * Key features:
 * - Automatic test data cleanup
 * - Convenient data creation methods
 * - Transaction management for test data
 * - Data verification utilities
 */
@Component
@Transactional
class DatabaseTestDataManager {

    @Autowired
    private lateinit var serieRepository: SerieRepositoryPort

    private val createdSerieIds = mutableSetOf<UUID>()

    /**
     * Creates a test Serie in the database using the provided builder.
     * The created Serie ID is tracked for automatic cleanup.
     *
     * @param builder The SerieTestDataBuilder to use for data creation
     * @return The created SerieEntity with generated ID
     */
    fun createTestSerie(builder: SerieTestDataBuilder = SerieTestDataBuilder()): SerieEntity {
        val entity = builder.buildEntity()
        val savedEntity = serieRepository.createSerie(entity)
        savedEntity.id?.let { createdSerieIds.add(it) }
        return savedEntity
    }

    /**
     * Creates multiple test Series in the database.
     *
     * @param count The number of Series to create
     * @param builderFactory Function to create builders for each Serie
     * @return List of created SerieEntity objects
     */
    fun createMultipleTestSeries(
        count: Int,
        builderFactory: (Int) -> SerieTestDataBuilder = { index ->
            SerieTestDataBuilder()
                .withCode("MULTI${index.toString().padStart(3, '0')}")
                .withName("Multi Serie $index")
        }
    ): List<SerieEntity> {
        return (1..count).map { index ->
            createTestSerie(builderFactory(index))
        }
    }

    /**
     * Creates a test Serie with a specific code.
     * Useful for testing scenarios that require known codes.
     *
     * @param code The specific code for the Serie
     * @param name Optional name (defaults to generated name)
     * @return The created SerieEntity
     */
    fun createTestSerieWithCode(code: String, name: String? = null): SerieEntity {
        val builder = SerieTestDataBuilder()
            .withCode(code)

        name?.let { builder.withName(it) }

        return createTestSerie(builder)
    }

    /**
     * Creates a test Serie with a specific ID.
     * Useful for testing scenarios that require known IDs.
     *
     * @param id The specific UUID for the Serie
     * @return The created SerieEntity
     */
    fun createTestSerieWithId(id: UUID): SerieEntity {
        val builder = SerieTestDataBuilder().withId(id)
        return createTestSerie(builder)
    }

    /**
     * Verifies that a Serie exists in the database.
     *
     * @param serieId The ID of the Serie to verify
     * @return true if the Serie exists, false otherwise
     */
    fun verifySerieExists(serieId: UUID): Boolean {
        return serieRepository.existsSerieById(serieId)
    }

    /**
     * Verifies that a Serie with the given code exists in the database.
     *
     * @param code The code of the Serie to verify
     * @return true if the Serie exists, false otherwise
     */
    fun verifySerieExistsByCode(code: String): Boolean {
        return serieRepository.existsSerieByCode(code)
    }

    /**
     * Gets the count of Series in the database.
     * Useful for verifying data state in tests.
     *
     * @return The total number of Series in the database
     */
    fun getSerieCount(): Long {
        return serieRepository.findAllSeries(0, Int.MAX_VALUE).totalElements
    }

    /**
     * Retrieves a test Serie by its ID.
     *
     * @param serieId The ID of the Serie to retrieve
     * @return The SerieEntity if found, null otherwise
     */
    fun getTestSerieById(serieId: UUID): SerieEntity? {
        return serieRepository.findSerieById(serieId)
    }

    /**
     * Retrieves a test Serie by its code.
     *
     * @param code The code of the Serie to retrieve
     * @return The SerieEntity if found, null otherwise
     */
    fun getTestSerieByCode(code: String): SerieEntity? {
        return serieRepository.findSerieByCode(code)
    }

    /**
     * Cleans up all test data created by this manager.
     * This method should be called after each test to ensure data isolation.
     */
    fun cleanupTestData() {
        createdSerieIds.forEach { serieId ->
            try {
                if (serieRepository.existsSerieById(serieId)) {
                    serieRepository.deleteSerieById(serieId)
                }
            } catch (e: Exception) {
                // Log but don't fail cleanup for individual items
                println("Warning: Failed to cleanup Serie with ID $serieId: ${e.message}")
            }
        }
        createdSerieIds.clear()
    }

    /**
     * Cleans up all Series from the database.
     * Use with caution - this removes ALL Series data.
     */
    fun cleanupAllSeries() {
        val allSeries = serieRepository.findAllSeries(0, Int.MAX_VALUE)
        allSeries.content.forEach { serie ->
            serie.id?.let { serieRepository.deleteSerieById(it) }
        }
        createdSerieIds.clear()
    }

    /**
     * Sets up a clean database state for testing.
     * Removes all existing data and resets tracking.
     */
    fun setupCleanState() {
        cleanupAllSeries()
        createdSerieIds.clear()
    }

    /**
     * Creates test data for pagination testing.
     *
     * @param totalItems The total number of items to create
     * @param namePrefix Prefix for the Serie names
     * @return List of created SerieEntity objects
     */
    fun createPaginationTestData(totalItems: Int, namePrefix: String = "Page"): List<SerieEntity> {
        return createMultipleTestSeries(totalItems) { index ->
            SerieTestDataBuilder()
                .withCode("PAGE${index.toString().padStart(4, '0')}")
                .withName("$namePrefix Serie $index")
                .withReleaseYear(2020 + (index % 5))
        }
    }

    /**
     * Creates test data with specific release years for filtering tests.
     *
     * @param years List of release years to create Series for
     * @return Map of year to created SerieEntity
     */
    fun createTestDataByYear(years: List<Int>): Map<Int, SerieEntity> {
        return years.associateWith { year ->
            createTestSerie(
                SerieTestDataBuilder()
                    .withCode("YEAR$year")
                    .withName("Serie from $year")
                    .withReleaseYear(year)
            )
        }
    }
}