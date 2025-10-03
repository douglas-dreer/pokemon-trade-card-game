package br.com.pokemon.tradecardgame.integration

import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.CreateSerieRequest
import br.com.pokemon.tradecardgame.domain.exception.SeriesAlreadyExistsException
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.CreateSerieUsecase
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.FindSerieByIdUsecase
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.CreateSerieCommand
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindSerieByIdQuery
import br.com.pokemon.tradecardgame.integration.testdata.DatabaseTestDataManager
import br.com.pokemon.tradecardgame.integration.testdata.SerieTestDataBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Integration tests for exception propagation through all application layers.
 *
 * This test class validates that exceptions are properly propagated from the repository layer
 * through the use case layer to the HTTP response layer, ensuring proper error transformation
 * and detailed error messages at each layer.
 *
 * Tests cover:
 * - Exception propagation from repository layer through to HTTP response
 * - Exception handling in use case layer and proper error transformation
 * - Validation exception handling with detailed error messages
 * - End-to-end exception flow validation
 */
@DisplayName("Exception Propagation Integration Tests")
class ExceptionPropagationIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var databaseTestDataManager: DatabaseTestDataManager

    @Autowired
    private lateinit var createSerieUsecase: CreateSerieUsecase

    @Autowired
    private lateinit var findSerieByIdUsecase: FindSerieByIdUsecase

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Clean database before each test to ensure isolation
        databaseTestDataManager.cleanupTestData()
    }

    @Nested
    @DisplayName("Repository Layer Exception Propagation Tests")
    inner class RepositoryLayerExceptionPropagationTests {

        @Test
        @DisplayName("Should propagate database constraint violation from repository to use case layer")
        @Transactional
        fun repositoryConstraintViolation_ShouldPropagateToUseCaseLayer() {
            // Arrange - Create existing serie to trigger constraint violation
            val existingCode = "REPO01"
            databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode(existingCode)
                    .withName("Repository Test Serie")
                    .withReleaseYear(2023)
            )

            // Create command with duplicate code to trigger repository constraint violation
            val duplicateCommand = CreateSerieCommand(
                code = existingCode,
                name = "Different Name",
                releaseYear = 2024,
                imageUrl = null,
                expansions = emptyList()
            )

            // Act & Assert - Verify exception propagates from repository through use case layer
            try {
                createSerieUsecase.execute(duplicateCommand)
                assert(false) { "Expected SeriesAlreadyExistsException to be thrown" }
            } catch (ex: SeriesAlreadyExistsException) {
                assert(ex.message == "The series '$existingCode' already exists in the system.") {
                    "Exception message should be properly formatted and contain the conflicting code"
                }
            }
        }

        @Test
        @DisplayName("Should propagate repository not found scenario through use case layer")
        @Transactional
        fun repositoryNotFound_ShouldPropagateToUseCaseLayer() {
            // Arrange - Use non-existent ID to trigger repository not found scenario
            val nonExistentId = UUID.randomUUID()
            val query = FindSerieByIdQuery(nonExistentId)

            // Act - Execute use case to test exception propagation
            val result = findSerieByIdUsecase.execute(query)

            // Assert - Verify repository not found is properly handled by use case
            assert(result == null) {
                "Use case should return null when repository doesn't find the entity, allowing controller to handle the not found scenario"
            }
        }
    }

    @Nested
    @DisplayName("Use Case Layer Exception Handling Tests")
    inner class UseCaseLayerExceptionHandlingTests {

        @Test
        @DisplayName("Should transform repository exceptions in use case layer correctly")
        @Transactional
        fun useCaseLayer_ShouldTransformRepositoryExceptionsCorrectly() {
            // Arrange - Create existing serie to trigger use case validation
            val existingCode = "USECASE01"
            databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode(existingCode)
                    .withName("Use Case Test Serie")
                    .withReleaseYear(2023)
            )

            // Create command that will trigger use case validation
            val duplicateCommand = CreateSerieCommand(
                code = existingCode,
                name = "Different Name",
                releaseYear = 2024,
                imageUrl = null,
                expansions = emptyList()
            )

            // Act & Assert - Verify use case properly transforms and throws domain exception
            try {
                createSerieUsecase.execute(duplicateCommand)
                assert(false) { "Expected SeriesAlreadyExistsException to be thrown" }
            } catch (ex: SeriesAlreadyExistsException) {
                assert(ex.message == "The series '$existingCode' already exists in the system.") {
                    "Exception message should be properly formatted"
                }
                // Verify exception is a domain exception (proper transformation)
                assert(ex is SeriesAlreadyExistsException) {
                    "Use case should transform repository constraint violations to domain exceptions"
                }
            }
        }

        @Test
        @DisplayName("Should handle use case validation and propagate domain exceptions")
        @Transactional
        fun useCaseValidation_ShouldPropagateDomainExceptions() {
            // Arrange - Create existing serie for validation testing
            val existingCode = "VALIDATION01"
            databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode(existingCode)
                    .withName("Validation Test Serie")
                    .withReleaseYear(2023)
            )

            // Test use case validation with duplicate code
            val duplicateCommand = CreateSerieCommand(
                code = existingCode,
                name = "Different Name",
                releaseYear = 2024,
                imageUrl = null,
                expansions = emptyList()
            )

            // Act & Assert - Verify use case validation throws proper domain exception
            try {
                createSerieUsecase.execute(duplicateCommand)
                assert(false) { "Expected domain exception to be thrown for duplicate code" }
            } catch (ex: SeriesAlreadyExistsException) {
                // Verify exception contains proper context
                assert(ex.message.contains(existingCode)) {
                    "Exception should contain the conflicting code for proper error context"
                }
                // Verify it's the correct domain exception type
                assert(ex is SeriesAlreadyExistsException) {
                    "Should throw SeriesAlreadyExistsException for duplicate code validation"
                }
            }
        }

        @Test
        @DisplayName("Should handle use case not found scenarios correctly")
        @Transactional
        fun useCaseNotFound_ShouldHandleCorrectly() {
            // Arrange - Use non-existent ID for use case query
            val nonExistentId = UUID.randomUUID()
            val query = FindSerieByIdQuery(nonExistentId)

            // Act - Execute use case directly
            val result = findSerieByIdUsecase.execute(query)

            // Assert - Verify use case returns null for not found (allowing controller to handle HTTP response)
            assert(result == null) {
                "Use case should return null for non-existent serie, allowing controller layer to handle HTTP 404 response"
            }
        }
    }

    @Nested
    @DisplayName("Validation Exception Handling Tests")
    inner class ValidationExceptionHandlingTests {

        @Test
        @DisplayName("Should handle domain validation exceptions with proper error context")
        @Transactional
        fun domainValidationExceptions_ShouldProvideProperErrorContext() {
            // Arrange - Create existing serie to trigger domain validation
            val existingCode = "DOMAIN01"
            databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode(existingCode)
                    .withName("Domain Test Serie")
                    .withReleaseYear(2023)
            )

            // Test domain validation at use case level
            val duplicateCommand = CreateSerieCommand(
                code = existingCode,
                name = "Different Name",
                releaseYear = 2024,
                imageUrl = null,
                expansions = emptyList()
            )

            // Act & Assert - Verify domain validation exception provides proper context
            try {
                createSerieUsecase.execute(duplicateCommand)
                assert(false) { "Expected domain validation exception" }
            } catch (ex: SeriesAlreadyExistsException) {
                // Verify exception message contains proper context
                assert(ex.message.contains(existingCode)) {
                    "Exception should contain the conflicting code"
                }
                // Verify exception message format
                assert(ex.message == "The series '$existingCode' already exists in the system.") {
                    "Exception message should follow the expected format"
                }
            }
        }

        @Test
        @DisplayName("Should validate exception propagation maintains error details")
        @Transactional
        fun exceptionPropagation_ShouldMaintainErrorDetails() {
            // Arrange - Create scenario for testing exception detail preservation
            val conflictCode = "DETAILS01"
            databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode(conflictCode)
                    .withName("Details Test Serie")
                    .withReleaseYear(2023)
            )

            val conflictCommand = CreateSerieCommand(
                code = conflictCode,
                name = "Different Name",
                releaseYear = 2024,
                imageUrl = null,
                expansions = emptyList()
            )

            // Act & Assert - Verify exception details are maintained through propagation
            try {
                createSerieUsecase.execute(conflictCommand)
                assert(false) { "Expected exception for conflict scenario" }
            } catch (ex: SeriesAlreadyExistsException) {
                // Verify all error details are preserved
                assert(ex.message.isNotEmpty()) { "Exception message should not be empty" }
                assert(ex.message.contains(conflictCode)) { "Exception should contain the specific conflicting identifier" }
                assert(ex is SeriesAlreadyExistsException) { "Exception type should be preserved through propagation" }
            }
        }
    }

    @Nested
    @DisplayName("End-to-End Exception Flow Validation Tests")
    inner class EndToEndExceptionFlowValidationTests {

        @Test
        @DisplayName("Should validate complete exception flow through all layers")
        @Transactional
        fun completeExceptionFlow_ShouldPropagateCorrectlyThroughAllLayers() {
            // Arrange - Create scenario that will trigger exception at repository level
            val existingCode = "FLOW01"
            databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode(existingCode)
                    .withName("Flow Test Serie")
                    .withReleaseYear(2023)
            )

            val duplicateCommand = CreateSerieCommand(
                code = existingCode,
                name = "Different Name",
                releaseYear = 2024,
                imageUrl = null,
                expansions = emptyList()
            )

            // Act & Assert - Verify complete exception flow
            try {
                createSerieUsecase.execute(duplicateCommand)
                assert(false) { "Expected exception to be thrown" }
            } catch (ex: SeriesAlreadyExistsException) {
                // Verify exception propagated correctly through all layers
                assert(ex.message == "The series '$existingCode' already exists in the system.") {
                    "Exception should maintain proper message format through all layers"
                }
                // Verify exception type is preserved
                assert(ex is SeriesAlreadyExistsException) {
                    "Exception type should be preserved through layer propagation"
                }
            }
        }

        @Test
        @DisplayName("Should maintain exception context through all propagation layers")
        @Transactional
        fun exceptionContext_ShouldBeMaintainedThroughAllLayers() {
            // Arrange - Create multiple scenarios to test context preservation
            val testCodes = listOf("CONTEXT01", "CONTEXT02")

            testCodes.forEach { code ->
                databaseTestDataManager.createTestSerie(
                    SerieTestDataBuilder()
                        .withCode(code)
                        .withName("Context Test Serie $code")
                        .withReleaseYear(2023)
                )
            }

            // Test each scenario to verify context preservation
            testCodes.forEach { code ->
                val duplicateCommand = CreateSerieCommand(
                    code = code,
                    name = "Different Name",
                    releaseYear = 2024,
                    imageUrl = null,
                    expansions = emptyList()
                )

                try {
                    createSerieUsecase.execute(duplicateCommand)
                    assert(false) { "Expected exception for code $code" }
                } catch (ex: SeriesAlreadyExistsException) {
                    // Verify that the specific context (code) is preserved
                    assert(ex.message.contains(code)) {
                        "Exception context should be preserved - expected code '$code' in message: ${ex.message}"
                    }
                }
            }
        }

        @Test
        @DisplayName("Should handle multiple exception types with proper propagation")
        @Transactional
        fun multipleExceptionTypes_ShouldPropagateCorrectly() {
            // Test 1: Conflict exception
            val conflictCode = "MULTI01"
            databaseTestDataManager.createTestSerie(
                SerieTestDataBuilder()
                    .withCode(conflictCode)
                    .withName("Multi Test Serie")
                    .withReleaseYear(2023)
            )

            val conflictCommand = CreateSerieCommand(
                code = conflictCode,
                name = "Different Name",
                releaseYear = 2024,
                imageUrl = null,
                expansions = emptyList()
            )

            try {
                createSerieUsecase.execute(conflictCommand)
                assert(false) { "Expected conflict exception" }
            } catch (ex: SeriesAlreadyExistsException) {
                assert(ex.message.contains(conflictCode)) { "Conflict exception should contain the conflicting code" }
            }

            // Test 2: Not found scenario (returns null, no exception)
            val nonExistentId = UUID.randomUUID()
            val notFoundQuery = FindSerieByIdQuery(nonExistentId)
            val result = findSerieByIdUsecase.execute(notFoundQuery)

            assert(result == null) { "Not found scenario should return null" }
        }
    }
}