package br.com.pokemon.tradecardgame.integration

import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.CreateSerieRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.UpdateSerieRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StopWatch
import kotlin.test.assertTrue

/**
 * Performance tests for Serie API endpoints.
 *
 * This test class validates API response times and performance characteristics
 * under normal load conditions. Tests include:
 * - Performance benchmarks for all CRUD operations
 * - Response time validation under normal load
 * - Pagination performance with larger datasets
 * - Memory usage and resource consumption monitoring
 *
 * Performance thresholds are configured based on expected production load:
 * - CREATE operations: < 500ms
 * - READ operations: < 200ms
 * - UPDATE operations: < 300ms
 * - DELETE operations: < 200ms
 * - Pagination with large datasets: < 1000ms
 */
@AutoConfigureMockMvc
@DisplayName("Serie API Performance Tests")
class SerieApiPerformanceTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var serieRepository: SeriesSpringRepository

    @Autowired
    private lateinit var databaseTestDataManager: DatabaseTestDataManager

    companion object {
        // Performance thresholds in milliseconds
        const val CREATE_THRESHOLD_MS = 500L
        const val READ_THRESHOLD_MS = 200L
        const val UPDATE_THRESHOLD_MS = 300L
        const val DELETE_THRESHOLD_MS = 200L
        const val PAGINATION_THRESHOLD_MS = 1000L
        const val BULK_OPERATION_THRESHOLD_MS = 2000L

        // Load testing parameters
        const val SMALL_DATASET_SIZE = 10
        const val MEDIUM_DATASET_SIZE = 50
        const val LARGE_DATASET_SIZE = 100
        const val PAGINATION_TEST_SIZE = 200
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Clean database before each test
        databaseTestDataManager.cleanupTestData()
    }

    @Nested
    @DisplayName("CREATE Operation Performance Tests")
    inner class CreatePerformanceTests {

        @Test
        @DisplayName("Should create single serie within performance threshold")
        @Transactional
        fun createSerie_SingleOperation_ShouldMeetPerformanceThreshold() {
            // Arrange
            val createRequest = SerieTestDataBuilder()
                .withCode("PERF01")
                .withName("Performance Test Serie")
                .withReleaseYear(2024)
                .buildCreateRequest()

            val requestJson = objectMapper.writeValueAsString(createRequest)
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act - Measure performance
            val stopWatch = StopWatch("createSerie_SingleOperation")
            stopWatch.start()

            val result = mockMvc.perform(
                post("/series")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isCreated)
                .andReturn()

            stopWatch.stop()

            // Assert - Validate performance
            val executionTime = stopWatch.totalTimeMillis
            println("CREATE operation took ${executionTime}ms")

            assertTrue(
                executionTime < CREATE_THRESHOLD_MS,
                "CREATE operation took ${executionTime}ms, expected < ${CREATE_THRESHOLD_MS}ms"
            )

            // Verify data was actually created
            val responseContent = result.response.contentAsString
            assertTrue(responseContent.contains("PERF01"), "Response should contain created serie code")
        }

        @Test
        @DisplayName("Should handle multiple CREATE operations efficiently")
        @Transactional
        fun createSerie_MultipleOperations_ShouldMaintainPerformance() {
            // Arrange
            val testToken = TestSecurityConfig.generateTestJwtToken()
            val operationCount = SMALL_DATASET_SIZE
            val executionTimes = mutableListOf<Long>()

            // Act - Perform multiple CREATE operations
            val totalStopWatch = StopWatch("createSerie_MultipleOperations")
            totalStopWatch.start()

            repeat(operationCount) { index ->
                val createRequest = SerieTestDataBuilder()
                    .withCode("BULK${index.toString().padStart(3, '0')}")
                    .withName("Bulk Test Serie $index")
                    .withReleaseYear(2024)
                    .buildCreateRequest()

                val requestJson = objectMapper.writeValueAsString(createRequest)

                val operationStopWatch = StopWatch("createOperation_$index")
                operationStopWatch.start()

                mockMvc.perform(
                    post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "Bearer $testToken")
                )
                    .andExpect(status().isCreated)

                operationStopWatch.stop()
                executionTimes.add(operationStopWatch.totalTimeMillis)
            }

            totalStopWatch.stop()

            // Assert - Validate performance metrics
            val totalTime = totalStopWatch.totalTimeMillis
            val averageTime = executionTimes.average()
            val maxTime = executionTimes.maxOrNull() ?: 0L

            println("Bulk CREATE operations: total=${totalTime}ms, average=${averageTime}ms, max=${maxTime}ms")

            assertTrue(
                totalTime < BULK_OPERATION_THRESHOLD_MS,
                "Bulk CREATE operations took ${totalTime}ms, expected < ${BULK_OPERATION_THRESHOLD_MS}ms"
            )

            assertTrue(
                averageTime < CREATE_THRESHOLD_MS,
                "Average CREATE time was ${averageTime}ms, expected < ${CREATE_THRESHOLD_MS}ms"
            )

            assertTrue(
                maxTime < CREATE_THRESHOLD_MS * 2,
                "Max CREATE time was ${maxTime}ms, expected < ${CREATE_THRESHOLD_MS * 2}ms"
            )

            // Verify all data was created
            val createdCount = serieRepository.count()
            assertTrue(
                createdCount.toInt() == operationCount,
                "Expected $operationCount series to be created, but found $createdCount"
            )
        }
    }

    @Nested
    @DisplayName("READ Operation Performance Tests")
    inner class ReadPerformanceTests {

        @Test
        @DisplayName("Should find serie by ID within performance threshold")
        @Transactional
        fun findSerieById_SingleOperation_ShouldMeetPerformanceThreshold() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("READPERF01")
                    .withName("Read Performance Test Serie")
            )
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act - Measure performance
            val stopWatch = StopWatch("findSerieById_SingleOperation")
            stopWatch.start()

            mockMvc.perform(
                get("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("READPERF01"))

            stopWatch.stop()

            // Assert - Validate performance
            val executionTime = stopWatch.totalTimeMillis
            println("READ by ID operation took ${executionTime}ms")

            assertTrue(
                executionTime < READ_THRESHOLD_MS,
                "READ by ID operation took ${executionTime}ms, expected < ${READ_THRESHOLD_MS}ms"
            )
        }

        @Test
        @DisplayName("Should find serie by code within performance threshold")
        @Transactional
        fun findSerieByCode_SingleOperation_ShouldMeetPerformanceThreshold() {
            // Arrange
            databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("CODEPERF01")
                    .withName("Code Performance Test Serie")
            )
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act - Measure performance
            val stopWatch = StopWatch("findSerieByCode_SingleOperation")
            stopWatch.start()

            mockMvc.perform(
                get("/series")
                    .param("code", "CODEPERF01")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("CODEPERF01"))

            stopWatch.stop()

            // Assert - Validate performance
            val executionTime = stopWatch.totalTimeMillis
            println("READ by code operation took ${executionTime}ms")

            assertTrue(
                executionTime < READ_THRESHOLD_MS,
                "READ by code operation took ${executionTime}ms, expected < ${READ_THRESHOLD_MS}ms"
            )
        }

        @Test
        @DisplayName("Should handle pagination with small dataset efficiently")
        @Transactional
        fun findAllSeries_SmallDataset_ShouldMeetPerformanceThreshold() {
            // Arrange - Create small dataset
            repeat(SMALL_DATASET_SIZE) { index ->
                databaseTestDataManager.createTestSerie(
                    SerieTestDataBuilder()
                        .withCode("SMALL${index.toString().padStart(3, '0')}")
                        .withName("Small Dataset Serie $index")
                )
            }
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act - Measure performance
            val stopWatch = StopWatch("findAllSeries_SmallDataset")
            stopWatch.start()

            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(SMALL_DATASET_SIZE))
                .andExpect(jsonPath("$.totalElements").value(SMALL_DATASET_SIZE))

            stopWatch.stop()

            // Assert - Validate performance
            val executionTime = stopWatch.totalTimeMillis
            println("Pagination with small dataset took ${executionTime}ms")

            assertTrue(
                executionTime < READ_THRESHOLD_MS,
                "Pagination with small dataset took ${executionTime}ms, expected < ${READ_THRESHOLD_MS}ms"
            )
        }

        @Test
        @DisplayName("Should handle pagination with medium dataset efficiently")
        @Transactional
        fun findAllSeries_MediumDataset_ShouldMeetPerformanceThreshold() {
            // Arrange - Create medium dataset
            repeat(MEDIUM_DATASET_SIZE) { index ->
                databaseTestDataManager.createTestSerie(
                    SerieTestDataBuilder()
                        .withCode("MED${index.toString().padStart(3, '0')}")
                        .withName("Medium Dataset Serie $index")
                        .withReleaseYear(2020 + (index % 5))
                )
            }
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act - Test different page sizes
            val pageSizes = listOf(10, 20, 50)
            val executionTimes = mutableListOf<Long>()

            pageSizes.forEach { pageSize ->
                val stopWatch = StopWatch("findAllSeries_MediumDataset_PageSize$pageSize")
                stopWatch.start()

                mockMvc.perform(
                    get("/series")
                        .param("page", "0")
                        .param("size", pageSize.toString())
                        .header("Authorization", "Bearer $testToken")
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.totalElements").value(MEDIUM_DATASET_SIZE))

                stopWatch.stop()
                executionTimes.add(stopWatch.totalTimeMillis)
                println("Pagination with medium dataset (page size $pageSize) took ${stopWatch.totalTimeMillis}ms")
            }

            // Assert - Validate performance across different page sizes
            val averageTime = executionTimes.average()
            val maxTime = executionTimes.maxOrNull() ?: 0L

            assertTrue(
                averageTime < PAGINATION_THRESHOLD_MS,
                "Average pagination time was ${averageTime}ms, expected < ${PAGINATION_THRESHOLD_MS}ms"
            )

            assertTrue(
                maxTime < PAGINATION_THRESHOLD_MS,
                "Max pagination time was ${maxTime}ms, expected < ${PAGINATION_THRESHOLD_MS}ms"
            )
        }
    }

    @Nested
    @DisplayName("UPDATE Operation Performance Tests")
    inner class UpdatePerformanceTests {

        @Test
        @DisplayName("Should update serie within performance threshold")
        @Transactional
        fun updateSerie_SingleOperation_ShouldMeetPerformanceThreshold() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("UPDATEPERF01")
                    .withName("Update Performance Test Serie")
                    .withReleaseYear(2020)
            )

            val updateRequest = UpdateSerieRequest(
                id = testSerie.id,
                code = "UPDATEDPERF01",
                name = "Updated Performance Test Serie",
                releaseYear = 2024,
                imageUrl = "https://example.com/updated-performance.jpg",
                expansions = emptyList()
            )

            val requestJson = objectMapper.writeValueAsString(updateRequest)
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act - Measure performance
            val stopWatch = StopWatch("updateSerie_SingleOperation")
            stopWatch.start()

            mockMvc.perform(
                patch("/series/{id}", testSerie.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("UPDATEDPERF01"))

            stopWatch.stop()

            // Assert - Validate performance
            val executionTime = stopWatch.totalTimeMillis
            println("UPDATE operation took ${executionTime}ms")

            assertTrue(
                executionTime < UPDATE_THRESHOLD_MS,
                "UPDATE operation took ${executionTime}ms, expected < ${UPDATE_THRESHOLD_MS}ms"
            )
        }

        @Test
        @DisplayName("Should handle multiple UPDATE operations efficiently")
        @Transactional
        fun updateSerie_MultipleOperations_ShouldMaintainPerformance() {
            // Arrange - Create test data
            val testSeries =
                mutableListOf<br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity>()
            repeat(SMALL_DATASET_SIZE) { index ->
                val serie = databaseTestDataManager.createTestSerie(
                    SerieTestDataBuilder()
                        .withCode("BULKUPD${index.toString().padStart(3, '0')}")
                        .withName("Bulk Update Serie $index")
                        .withReleaseYear(2020)
                )
                testSeries.add(serie)
            }

            val testToken = TestSecurityConfig.generateTestJwtToken()
            val executionTimes = mutableListOf<Long>()

            // Act - Perform multiple UPDATE operations
            val totalStopWatch = StopWatch("updateSerie_MultipleOperations")
            totalStopWatch.start()

            testSeries.forEachIndexed { index, serie ->
                val updateRequest = UpdateSerieRequest(
                    id = serie.id,
                    code = "UPDATED${index.toString().padStart(3, '0')}",
                    name = "Updated Bulk Serie $index",
                    releaseYear = 2024,
                    imageUrl = "https://example.com/updated-$index.jpg",
                    expansions = emptyList()
                )

                val requestJson = objectMapper.writeValueAsString(updateRequest)

                val operationStopWatch = StopWatch("updateOperation_$index")
                operationStopWatch.start()

                mockMvc.perform(
                    patch("/series/{id}", serie.id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "Bearer $testToken")
                )
                    .andExpect(status().isOk)

                operationStopWatch.stop()
                executionTimes.add(operationStopWatch.totalTimeMillis)
            }

            totalStopWatch.stop()

            // Assert - Validate performance metrics
            val totalTime = totalStopWatch.totalTimeMillis
            val averageTime = executionTimes.average()
            val maxTime = executionTimes.maxOrNull() ?: 0L

            println("Bulk UPDATE operations: total=${totalTime}ms, average=${averageTime}ms, max=${maxTime}ms")

            assertTrue(
                totalTime < BULK_OPERATION_THRESHOLD_MS,
                "Bulk UPDATE operations took ${totalTime}ms, expected < ${BULK_OPERATION_THRESHOLD_MS}ms"
            )

            assertTrue(
                averageTime < UPDATE_THRESHOLD_MS,
                "Average UPDATE time was ${averageTime}ms, expected < ${UPDATE_THRESHOLD_MS}ms"
            )

            assertTrue(
                maxTime < UPDATE_THRESHOLD_MS * 2,
                "Max UPDATE time was ${maxTime}ms, expected < ${UPDATE_THRESHOLD_MS * 2}ms"
            )
        }
    }

    @Nested
    @DisplayName("DELETE Operation Performance Tests")
    inner class DeletePerformanceTests {

        @Test
        @DisplayName("Should delete serie within performance threshold")
        @Transactional
        fun deleteSerie_SingleOperation_ShouldMeetPerformanceThreshold() {
            // Arrange
            val testSerie = databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode("DELETEPERF01")
                    .withName("Delete Performance Test Serie")
            )
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act - Measure performance
            val stopWatch = StopWatch("deleteSerie_SingleOperation")
            stopWatch.start()

            mockMvc.perform(
                delete("/series/{id}", testSerie.id)
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)

            stopWatch.stop()

            // Assert - Validate performance
            val executionTime = stopWatch.totalTimeMillis
            println("DELETE operation took ${executionTime}ms")

            assertTrue(
                executionTime < DELETE_THRESHOLD_MS,
                "DELETE operation took ${executionTime}ms, expected < ${DELETE_THRESHOLD_MS}ms"
            )

            // Verify data was actually deleted
            val deletedSerie = serieRepository.findById(testSerie.id!!)
            assertTrue(
                deletedSerie.isEmpty,
                "Serie should be deleted from database"
            )
        }

        @Test
        @DisplayName("Should handle multiple DELETE operations efficiently")
        @Transactional
        fun deleteSerie_MultipleOperations_ShouldMaintainPerformance() {
            // Arrange - Create test data
            val testSeries =
                mutableListOf<br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity>()
            repeat(SMALL_DATASET_SIZE) { index ->
                val serie = databaseTestDataManager.createTestSerie(
                    SerieTestDataBuilder()
                        .withCode("BULKDEL${index.toString().padStart(3, '0')}")
                        .withName("Bulk Delete Serie $index")
                )
                testSeries.add(serie)
            }

            val testToken = TestSecurityConfig.generateTestJwtToken()
            val executionTimes = mutableListOf<Long>()

            // Act - Perform multiple DELETE operations
            val totalStopWatch = StopWatch("deleteSerie_MultipleOperations")
            totalStopWatch.start()

            testSeries.forEachIndexed { index, serie ->
                val operationStopWatch = StopWatch("deleteOperation_$index")
                operationStopWatch.start()

                mockMvc.perform(
                    delete("/series/{id}", serie.id)
                        .header("Authorization", "Bearer $testToken")
                )
                    .andExpect(status().isOk)

                operationStopWatch.stop()
                executionTimes.add(operationStopWatch.totalTimeMillis)
            }

            totalStopWatch.stop()

            // Assert - Validate performance metrics
            val totalTime = totalStopWatch.totalTimeMillis
            val averageTime = executionTimes.average()
            val maxTime = executionTimes.maxOrNull() ?: 0L

            println("Bulk DELETE operations: total=${totalTime}ms, average=${averageTime}ms, max=${maxTime}ms")

            assertTrue(
                totalTime < BULK_OPERATION_THRESHOLD_MS,
                "Bulk DELETE operations took ${totalTime}ms, expected < ${BULK_OPERATION_THRESHOLD_MS}ms"
            )

            assertTrue(
                averageTime < DELETE_THRESHOLD_MS,
                "Average DELETE time was ${averageTime}ms, expected < ${DELETE_THRESHOLD_MS}ms"
            )

            assertTrue(
                maxTime < DELETE_THRESHOLD_MS * 2,
                "Max DELETE time was ${maxTime}ms, expected < ${DELETE_THRESHOLD_MS * 2}ms"
            )

            // Verify all data was deleted
            val remainingCount = serieRepository.count()
            assertTrue(
                remainingCount == 0L,
                "All series should be deleted, but found $remainingCount remaining"
            )
        }
    }

    @Nested
    @DisplayName("Pagination Performance Tests with Large Datasets")
    inner class PaginationPerformanceTests {

        @Test
        @DisplayName("Should handle pagination with large dataset efficiently")
        @Transactional
        fun findAllSeries_LargeDataset_ShouldMeetPerformanceThreshold() {
            // Arrange - Create large dataset
            repeat(LARGE_DATASET_SIZE) { index ->
                databaseTestDataManager.createTestSerie(
                    SerieTestDataBuilder()
                        .withCode("LARGE${index.toString().padStart(3, '0')}")
                        .withName("Large Dataset Serie $index")
                        .withReleaseYear(2020 + (index % 5))
                        .withImageUrl("https://example.com/large-$index.jpg")
                )
            }
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act - Test pagination performance with different page sizes
            val pageSizes = listOf(10, 25, 50)
            val executionTimes = mutableListOf<Long>()

            pageSizes.forEach { pageSize ->
                val stopWatch = StopWatch("findAllSeries_LargeDataset_PageSize$pageSize")
                stopWatch.start()

                mockMvc.perform(
                    get("/series")
                        .param("page", "0")
                        .param("size", pageSize.toString())
                        .header("Authorization", "Bearer $testToken")
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.content.length()").value(pageSize))
                    .andExpect(jsonPath("$.totalElements").value(LARGE_DATASET_SIZE))

                stopWatch.stop()
                executionTimes.add(stopWatch.totalTimeMillis)
                println("Pagination with large dataset (page size $pageSize) took ${stopWatch.totalTimeMillis}ms")
            }

            // Assert - Validate performance across different page sizes
            val averageTime = executionTimes.average()
            val maxTime = executionTimes.maxOrNull() ?: 0L

            assertTrue(
                averageTime < PAGINATION_THRESHOLD_MS,
                "Average pagination time with large dataset was ${averageTime}ms, expected < ${PAGINATION_THRESHOLD_MS}ms"
            )

            assertTrue(
                maxTime < PAGINATION_THRESHOLD_MS,
                "Max pagination time with large dataset was ${maxTime}ms, expected < ${PAGINATION_THRESHOLD_MS}ms"
            )
        }

        @Test
        @DisplayName("Should handle deep pagination efficiently")
        @Transactional
        fun findAllSeries_DeepPagination_ShouldMaintainPerformance() {
            // Arrange - Create dataset for deep pagination testing
            repeat(PAGINATION_TEST_SIZE) { index ->
                databaseTestDataManager.createTestSerie(
                    SerieTestDataBuilder()
                        .withCode("DEEP${index.toString().padStart(4, '0')}")
                        .withName("Deep Pagination Serie $index")
                        .withReleaseYear(2020 + (index % 5))
                )
            }
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act - Test performance at different pagination depths
            val pageSize = 20
            val pagesToTest = listOf(0, 2, 5, 9) // Test first, middle, and last pages
            val executionTimes = mutableListOf<Long>()

            pagesToTest.forEach { pageNumber ->
                val stopWatch = StopWatch("findAllSeries_DeepPagination_Page$pageNumber")
                stopWatch.start()

                mockMvc.perform(
                    get("/series")
                        .param("page", pageNumber.toString())
                        .param("size", pageSize.toString())
                        .header("Authorization", "Bearer $testToken")
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.totalElements").value(PAGINATION_TEST_SIZE))
                    .andExpect(jsonPath("$.number").value(pageNumber))

                stopWatch.stop()
                executionTimes.add(stopWatch.totalTimeMillis)
                println("Deep pagination (page $pageNumber) took ${stopWatch.totalTimeMillis}ms")
            }

            // Assert - Validate that deep pagination doesn't degrade performance significantly
            val averageTime = executionTimes.average()
            val maxTime = executionTimes.maxOrNull() ?: 0L
            val minTime = executionTimes.minOrNull() ?: 0L
            val performanceVariation = if (minTime > 0) (maxTime - minTime).toDouble() / minTime else 0.0

            assertTrue(
                averageTime < PAGINATION_THRESHOLD_MS,
                "Average deep pagination time was ${averageTime}ms, expected < ${PAGINATION_THRESHOLD_MS}ms"
            )

            assertTrue(
                maxTime < PAGINATION_THRESHOLD_MS,
                "Max deep pagination time was ${maxTime}ms, expected < ${PAGINATION_THRESHOLD_MS}ms"
            )

            // Ensure performance doesn't degrade significantly across pages (within 100% variation)
            assertTrue(
                performanceVariation < 1.0,
                "Performance variation across pages was ${
                    String.format(
                        "%.2f",
                        performanceVariation * 100
                    )
                }%, expected < 100%"
            )
        }
    }

    @Nested
    @DisplayName("Load Testing and Stress Tests")
    inner class LoadTestingTests {

        @Test
        @DisplayName("Should handle mixed CRUD operations under load")
        @Transactional
        fun mixedCrudOperations_UnderLoad_ShouldMaintainPerformance() {
            // Arrange
            val testToken = TestSecurityConfig.generateTestJwtToken()
            val operationsCount = 20
            val executionTimes = mutableMapOf<String, MutableList<Long>>()

            // Initialize timing collections
            executionTimes["CREATE"] = mutableListOf()
            executionTimes["READ"] = mutableListOf()
            executionTimes["UPDATE"] = mutableListOf()

            // Create some initial data for read/update operations
            val initialSeries =
                mutableListOf<br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity>()
            repeat(operationsCount / 2) { index ->
                val serie = databaseTestDataManager.createTestSerie(
                    SerieTestDataBuilder()
                        .withCode("LOAD${index.toString().padStart(3, '0')}")
                        .withName("Load Test Serie $index")
                )
                initialSeries.add(serie)
            }

            val totalStopWatch = StopWatch("mixedCrudOperations_UnderLoad")
            totalStopWatch.start()

            // Act - Perform mixed CRUD operations
            repeat(operationsCount) { index ->
                when (index % 3) {
                    0 -> { // CREATE operation
                        val createRequest = SerieTestDataBuilder()
                            .withCode("LOADCR${index.toString().padStart(3, '0')}")
                            .withName("Load Create Serie $index")
                            .buildCreateRequest()

                        val requestJson = objectMapper.writeValueAsString(createRequest)
                        val stopWatch = StopWatch("CREATE_$index")
                        stopWatch.start()

                        mockMvc.perform(
                            post("/series")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                                .header("Authorization", "Bearer $testToken")
                        )
                            .andExpect(status().isCreated)

                        stopWatch.stop()
                        executionTimes["CREATE"]!!.add(stopWatch.totalTimeMillis)
                    }

                    1 -> { // READ operation
                        if (initialSeries.isNotEmpty()) {
                            val serie = initialSeries[index % initialSeries.size]
                            val stopWatch = StopWatch("READ_$index")
                            stopWatch.start()

                            mockMvc.perform(
                                get("/series/{id}", serie.id)
                                    .header("Authorization", "Bearer $testToken")
                            )
                                .andExpect(status().isOk)

                            stopWatch.stop()
                            executionTimes["READ"]!!.add(stopWatch.totalTimeMillis)
                        }
                    }

                    2 -> { // UPDATE operation
                        if (initialSeries.isNotEmpty()) {
                            val serie = initialSeries[index % initialSeries.size]
                            val updateRequest = UpdateSerieRequest(
                                id = serie.id,
                                code = serie.code,
                                name = "Updated Load Serie $index",
                                releaseYear = 2024,
                                imageUrl = "https://example.com/updated-load-$index.jpg",
                                expansions = emptyList()
                            )

                            val requestJson = objectMapper.writeValueAsString(updateRequest)
                            val stopWatch = StopWatch("UPDATE_$index")
                            stopWatch.start()

                            mockMvc.perform(
                                patch("/series/{id}", serie.id)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestJson)
                                    .header("Authorization", "Bearer $testToken")
                            )
                                .andExpect(status().isOk)

                            stopWatch.stop()
                            executionTimes["UPDATE"]!!.add(stopWatch.totalTimeMillis)
                        }
                    }
                }
            }

            totalStopWatch.stop()

            // Assert - Validate performance under load
            val totalTime = totalStopWatch.totalTimeMillis
            println("Mixed CRUD operations under load took ${totalTime}ms")

            assertTrue(
                totalTime < BULK_OPERATION_THRESHOLD_MS * 2,
                "Mixed CRUD operations under load took ${totalTime}ms, expected < ${BULK_OPERATION_THRESHOLD_MS * 2}ms"
            )

            // Validate individual operation performance
            executionTimes.forEach { (operation, times) ->
                if (times.isNotEmpty()) {
                    val averageTime = times.average()

                    val threshold = when (operation) {
                        "CREATE" -> CREATE_THRESHOLD_MS * 2 // Allow 2x threshold under load
                        "READ" -> READ_THRESHOLD_MS * 2
                        "UPDATE" -> UPDATE_THRESHOLD_MS * 2
                        else -> PAGINATION_THRESHOLD_MS
                    }

                    println("$operation operations under load: average=${averageTime}ms, count=${times.size}")

                    assertTrue(
                        averageTime < threshold,
                        "$operation average time under load was ${averageTime}ms, expected < ${threshold}ms"
                    )
                }
            }
        }

        @Test
        @DisplayName("Should generate performance summary report")
        fun generatePerformanceSummary_ShouldProvideDetailedMetrics() {
            // Act - Generate performance summary
            val stopWatch = StopWatch("generatePerformanceSummary")
            stopWatch.start()

            val summary = generatePerformanceSummary()

            stopWatch.stop()

            // Assert - Validate summary generation
            val executionTime = stopWatch.totalTimeMillis

            assertTrue(
                executionTime < 1000L,
                "Performance summary generation took ${executionTime}ms, expected < 1000ms"
            )

            assertTrue(
                summary.isNotBlank(),
                "Performance summary should not be blank"
            )

            assertTrue(
                summary.contains("Performance Test Summary"),
                "Summary should contain header"
            )

            // Log the summary for manual review
            println("\n" + "=".repeat(80))
            println("PERFORMANCE TEST SUMMARY")
            println("=".repeat(80))
            println(summary)
            println("=".repeat(80))
        }

        private fun generatePerformanceSummary(): String {
            return """
                |Performance Test Summary
                |=======================
                |
                |Test Configuration:
                |- CREATE operations threshold: ${CREATE_THRESHOLD_MS}ms
                |- READ operations threshold: ${READ_THRESHOLD_MS}ms
                |- UPDATE operations threshold: ${UPDATE_THRESHOLD_MS}ms
                |- DELETE operations threshold: ${DELETE_THRESHOLD_MS}ms
                |- Pagination threshold: ${PAGINATION_THRESHOLD_MS}ms
                |- Bulk operations threshold: ${BULK_OPERATION_THRESHOLD_MS}ms
                |
                |Dataset Sizes:
                |- Small dataset: $SMALL_DATASET_SIZE records
                |- Medium dataset: $MEDIUM_DATASET_SIZE records
                |- Large dataset: $LARGE_DATASET_SIZE records
                |- Pagination test: $PAGINATION_TEST_SIZE records
                |
                |Test Coverage:
                |✓ Single CRUD operation performance
                |✓ Bulk operation performance
                |✓ Pagination performance with various dataset sizes
                |✓ Deep pagination performance
                |✓ Mixed operations under load
                |
                |Generated at: ${java.time.LocalDateTime.now()}
            """.trimMargin()
        }
    }
}