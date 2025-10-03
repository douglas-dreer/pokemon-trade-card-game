package br.com.pokemon.tradecardgame.integration

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

/**
 * Integration tests for pagination functionality in the Serie API.
 *
 * This test class validates pagination behavior including:
 * - Pagination with various page sizes and correct data subsets
 * - Pagination metadata calculation (total pages, elements, etc.)
 * - Edge cases like empty results, invalid page parameters, and large page sizes
 * - Proper handling of pagination boundaries and navigation
 *
 * Tests cover requirements: 5.1, 5.2, 5.3, 5.4
 */
@AutoConfigureMockMvc
@DisplayName("Pagination Integration Tests")
class PaginationIntegrationTest : AbstractIntegrationTest() {

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
    @DisplayName("Pagination Parameter Tests")
    inner class PaginationParameterTests {

        @Test
        @DisplayName("Should return correct data subset for first page with page size 3")
        @Transactional
        fun findAllSeries_FirstPageSize3_ShouldReturnCorrectSubset() {
            // Arrange - Create 10 test series
            databaseTestDataManager.createPaginationTestData(10, "FirstPage")
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
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.numberOfElements").value(3))
        }

        @Test
        @DisplayName("Should return correct data subset for middle page")
        @Transactional
        fun findAllSeries_MiddlePage_ShouldReturnCorrectSubset() {
            // Arrange - Create 15 test series
            databaseTestDataManager.createPaginationTestData(15, "MiddlePage")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert - Test page 2 (middle page)
            mockMvc.perform(
                get("/series")
                    .param("page", "2")
                    .param("size", "4")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.size").value(4))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.numberOfElements").value(4))
        }

        @Test
        @DisplayName("Should return correct data subset for last page with remaining elements")
        @Transactional
        fun findAllSeries_LastPage_ShouldReturnRemainingElements() {
            // Arrange - Create 13 test series
            databaseTestDataManager.createPaginationTestData(13, "LastPage")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert - Test last page (page 2 with size 5 = 3 remaining elements)
            mockMvc.perform(
                get("/series")
                    .param("page", "2")
                    .param("size", "5")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3)) // Remaining elements
                .andExpect(jsonPath("$.totalElements").value(13))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.numberOfElements").value(3))
        }

        @Test
        @DisplayName("Should handle various page sizes correctly")
        @Transactional
        fun findAllSeries_VariousPageSizes_ShouldHandleCorrectly() {
            // Arrange - Create 20 test series
            databaseTestDataManager.createPaginationTestData(20, "VarSize")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Test page size 1
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "1")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalPages").value(20))
                .andExpect(jsonPath("$.size").value(1))

            // Test page size 10
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(10))

            // Test page size 25 (larger than total elements)
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "25")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(25))
        }
    }

    @Nested
    @DisplayName("Pagination Metadata Tests")
    inner class PaginationMetadataTests {

        @Test
        @DisplayName("Should calculate total pages correctly for various scenarios")
        @Transactional
        fun findAllSeries_ShouldCalculateTotalPagesCorrectly() {
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Test with 7 elements, page size 3 = 3 pages (3, 3, 1)
            databaseTestDataManager.createPaginationTestData(7, "TotalPages")

            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "3")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.totalElements").value(7))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.size").value(3))

            // Clean and test with exact division
            databaseTestDataManager.cleanupTestData()
            databaseTestDataManager.createPaginationTestData(9, "ExactDiv")

            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "3")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.totalElements").value(9))
                .andExpect(jsonPath("$.totalPages").value(3))
        }

        @Test
        @DisplayName("Should provide correct navigation metadata")
        @Transactional
        fun findAllSeries_ShouldProvideCorrectNavigationMetadata() {
            // Arrange - Create 12 test series
            databaseTestDataManager.createPaginationTestData(12, "Navigation")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Test first page navigation
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "4")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.number").value(0))

            // Test middle page navigation
            mockMvc.perform(
                get("/series")
                    .param("page", "1")
                    .param("size", "4")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.number").value(1))

            // Test last page navigation
            mockMvc.perform(
                get("/series")
                    .param("page", "2")
                    .param("size", "4")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.number").value(2))
        }

        @Test
        @DisplayName("Should calculate numberOfElements correctly for each page")
        @Transactional
        fun findAllSeries_ShouldCalculateNumberOfElementsCorrectly() {
            // Arrange - Create 11 test series
            databaseTestDataManager.createPaginationTestData(11, "NumElements")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // First page: 5 elements
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "5")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.numberOfElements").value(5))
                .andExpect(jsonPath("$.content.length()").value(5))

            // Second page: 5 elements
            mockMvc.perform(
                get("/series")
                    .param("page", "1")
                    .param("size", "5")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.numberOfElements").value(5))
                .andExpect(jsonPath("$.content.length()").value(5))

            // Third page: 1 element (remaining)
            mockMvc.perform(
                get("/series")
                    .param("page", "2")
                    .param("size", "5")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.content.length()").value(1))
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    inner class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty results correctly")
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
                .andExpect(jsonPath("$.numberOfElements").value(0))
        }

        @Test
        @DisplayName("Should handle page number beyond available pages")
        @Transactional
        fun findAllSeries_WithPageBeyondAvailable_ShouldReturnEmptyPage() {
            // Arrange - Create 5 test series
            databaseTestDataManager.createPaginationTestData(5, "Beyond")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert - Request page 10 when only 1 page exists
            mockMvc.perform(
                get("/series")
                    .param("page", "10")
                    .param("size", "10")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(10))
                .andExpect(jsonPath("$.numberOfElements").value(0))
        }

        @Test
        @DisplayName("Should handle invalid page parameters gracefully")
        @Transactional
        fun findAllSeries_WithInvalidPageParameters_ShouldHandleGracefully() {
            // Arrange
            databaseTestDataManager.createPaginationTestData(5, "Invalid")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Test negative page number - should default to 0
            mockMvc.perform(
                get("/series")
                    .param("page", "-1")
                    .param("size", "5")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.number").value(0))

            // Test zero page size - should handle appropriately
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "0")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("Should handle large page sizes correctly")
        @Transactional
        fun findAllSeries_WithLargePageSize_ShouldHandleCorrectly() {
            // Arrange - Create 50 test series
            databaseTestDataManager.createPaginationTestData(50, "Large")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Test very large page size
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "1000")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(50))
                .andExpect(jsonPath("$.totalElements").value(50))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(1000))
                .andExpect(jsonPath("$.numberOfElements").value(50))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
        }

        @Test
        @DisplayName("Should handle single element pagination correctly")
        @Transactional
        fun findAllSeries_WithSingleElement_ShouldHandleCorrectly() {
            // Arrange - Create 1 test serie
            databaseTestDataManager.createPaginationTestData(1, "Single")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Act & Assert
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
        }
    }

    @Nested
    @DisplayName("Large Dataset Handling")
    inner class LargeDatasetTests {

        @Test
        @DisplayName("Should handle large datasets efficiently")
        @Transactional
        fun findAllSeries_WithLargeDataset_ShouldHandleEfficiently() {
            // Arrange - Create 100 test series
            databaseTestDataManager.createPaginationTestData(100, "LargeDataset")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            // Test pagination through large dataset
            mockMvc.perform(
                get("/series")
                    .param("page", "0")
                    .param("size", "20")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.totalElements").value(100))
                .andExpect(jsonPath("$.totalPages").value(5))

            // Test middle page
            mockMvc.perform(
                get("/series")
                    .param("page", "2")
                    .param("size", "20")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false))

            // Test last page
            mockMvc.perform(
                get("/series")
                    .param("page", "4")
                    .param("size", "20")
                    .header("Authorization", "Bearer $testToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.number").value(4))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true))
        }

        @Test
        @DisplayName("Should maintain consistent pagination metadata across pages")
        @Transactional
        fun findAllSeries_LargeDataset_ShouldMaintainConsistentMetadata() {
            // Arrange - Create 77 test series (odd number for testing)
            databaseTestDataManager.createPaginationTestData(77, "Consistent")
            val testToken = TestSecurityConfig.generateTestJwtToken()

            val pageSize = 15
            val expectedTotalPages = 6 // 77 / 15 = 5.13... = 6 pages

            // Test each page for consistent metadata
            for (pageNumber in 0 until expectedTotalPages) {
                val expectedElementsOnPage = when (pageNumber) {
                    5 -> 2 // Last page: 77 - (5 * 15) = 2 elements
                    else -> 15 // Other pages: full page size
                }

                mockMvc.perform(
                    get("/series")
                        .param("page", pageNumber.toString())
                        .param("size", pageSize.toString())
                        .header("Authorization", "Bearer $testToken")
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.totalElements").value(77))
                    .andExpect(jsonPath("$.totalPages").value(expectedTotalPages))
                    .andExpect(jsonPath("$.size").value(pageSize))
                    .andExpect(jsonPath("$.number").value(pageNumber))
                    .andExpect(jsonPath("$.numberOfElements").value(expectedElementsOnPage))
                    .andExpect(jsonPath("$.content.length()").value(expectedElementsOnPage))
            }
        }
    }
}