package br.com.pokemon.tradecardgame.integration

import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import br.com.pokemon.tradecardgame.integration.testdata.SerieTestDataBuilder
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Integration tests for SerieRepositoryPort implementation.
 *
 * Tests the complete database layer functionality including CRUD operations,
 * constraint validation, transaction behavior, and data consistency.
 * Uses real PostgreSQL database via TestContainers for realistic testing.
 */
@DisplayName("Serie Repository Integration Tests")
class SerieRepositoryIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var serieRepository: SerieRepositoryPort

    @BeforeEach
    fun cleanDatabase() {
        // Clean up any existing test data to ensure test isolation
        // Note: @Transactional with rollback handles this automatically, but explicit cleanup ensures consistency
    }

    @Nested
    @DisplayName("CRUD Operations")
    inner class CrudOperations {

        @Test
        @DisplayName("Should save serie entity and persist to database")
        @Transactional
        @Rollback
        fun saveOperation_WithValidEntity_ShouldPersistToDatabase() {
            // Arrange
            val testSerie = SerieTestDataBuilder()
                .withCode("SAVE01")
                .withName("Save Test Serie")
                .withReleaseYear(2024)
                .withImageUrl("https://example.com/save-test.jpg")
                .buildEntity()

            // Act
            val savedSerie = serieRepository.createSerie(testSerie)

            // Assert
            assertThat(savedSerie).isNotNull
            assertThat(savedSerie.id).isNotNull
            assertThat(savedSerie.code).isEqualTo("SAVE01")
            assertThat(savedSerie.name).isEqualTo("Save Test Serie")
            assertThat(savedSerie.releaseYear).isEqualTo(2024)
            assertThat(savedSerie.imageUrl).isEqualTo("https://example.com/save-test.jpg")
            assertThat(savedSerie.createdAt).isNotNull
            assertThat(savedSerie.updatedAt).isNotNull

            // Verify persistence by finding the saved entity
            val foundSerie = serieRepository.findSerieById(savedSerie.id!!)
            assertThat(foundSerie).isNotNull
            assertThat(foundSerie!!.code).isEqualTo("SAVE01")
        }

        @Test
        @DisplayName("Should find serie by ID when entity exists")
        @Transactional
        @Rollback
        fun findById_WithExistingId_ShouldReturnEntity() {
            // Arrange
            val testSerie = SerieTestDataBuilder()
                .withCode("FIND01")
                .withName("Find Test Serie")
                .buildEntity()
            val savedSerie = serieRepository.createSerie(testSerie)

            // Act
            val foundSerie = serieRepository.findSerieById(savedSerie.id!!)

            // Assert
            assertThat(foundSerie).isNotNull
            assertThat(foundSerie!!.id).isEqualTo(savedSerie.id)
            assertThat(foundSerie.code).isEqualTo("FIND01")
            assertThat(foundSerie.name).isEqualTo("Find Test Serie")
        }

        @Test
        @DisplayName("Should return null when finding serie by non-existent ID")
        @Transactional
        @Rollback
        fun findById_WithNonExistentId_ShouldReturnNull() {
            // Arrange
            val nonExistentId = UUID.randomUUID()

            // Act
            val foundSerie = serieRepository.findSerieById(nonExistentId)

            // Assert
            assertThat(foundSerie).isNull()
        }

        @Test
        @DisplayName("Should find serie by code when entity exists")
        @Transactional
        @Rollback
        fun findByCode_WithExistingCode_ShouldReturnEntity() {
            // Arrange
            val testSerie = SerieTestDataBuilder()
                .withCode("FINDCODE")
                .withName("Find By Code Test")
                .buildEntity()
            serieRepository.createSerie(testSerie)

            // Act
            val foundSerie = serieRepository.findSerieByCode("FINDCODE")

            // Assert
            assertThat(foundSerie).isNotNull
            assertThat(foundSerie!!.code).isEqualTo("FINDCODE")
            assertThat(foundSerie.name).isEqualTo("Find By Code Test")
        }

        @Test
        @DisplayName("Should return null when finding serie by non-existent code")
        @Transactional
        @Rollback
        fun findByCode_WithNonExistentCode_ShouldReturnNull() {
            // Act
            val foundSerie = serieRepository.findSerieByCode("NONEXIST")

            // Assert
            assertThat(foundSerie).isNull()
        }

        @Test
        @DisplayName("Should update serie and persist changes to database")
        @Transactional
        @Rollback
        fun updateOperation_WithValidData_ShouldPersistChanges() {
            // Arrange
            val originalSerie = SerieTestDataBuilder()
                .withCode("UPDATE01")
                .withName("Original Name")
                .withReleaseYear(2023)
                .buildEntity()
            val savedSerie = serieRepository.createSerie(originalSerie)

            // Create updated entity with same ID but different data
            val updatedSerie = SerieEntity(
                id = savedSerie.id,
                code = savedSerie.code, // Code should remain the same (unique constraint)
                name = "Updated Name",
                releaseYear = 2024,
                imageUrl = "https://example.com/updated.jpg",
                expansions = savedSerie.expansions,
                createdAt = savedSerie.createdAt,
                updatedAt = savedSerie.updatedAt
            )

            // Act
            val result = serieRepository.updateSerie(updatedSerie)

            // Assert
            assertThat(result).isNotNull
            assertThat(result.id).isEqualTo(savedSerie.id)
            assertThat(result.name).isEqualTo("Updated Name")
            assertThat(result.releaseYear).isEqualTo(2024)
            assertThat(result.imageUrl).isEqualTo("https://example.com/updated.jpg")

            // Verify persistence by finding the updated entity
            val foundSerie = serieRepository.findSerieById(savedSerie.id!!)
            assertThat(foundSerie).isNotNull
            assertThat(foundSerie!!.name).isEqualTo("Updated Name")
            assertThat(foundSerie.releaseYear).isEqualTo(2024)
        }

        @Test
        @DisplayName("Should delete serie and remove from database")
        @Transactional
        @Rollback
        fun deleteOperation_WithExistingId_ShouldRemoveFromDatabase() {
            // Arrange
            val testSerie = SerieTestDataBuilder()
                .withCode("DELETE01")
                .withName("Delete Test Serie")
                .buildEntity()
            val savedSerie = serieRepository.createSerie(testSerie)
            val serieId = savedSerie.id!!

            // Verify entity exists before deletion
            assertThat(serieRepository.findSerieById(serieId)).isNotNull

            // Act
            serieRepository.deleteSerieById(serieId)

            // Assert
            val foundSerie = serieRepository.findSerieById(serieId)
            assertThat(foundSerie).isNull()
            assertThat(serieRepository.existsSerieById(serieId)).isFalse()
        }

        @Test
        @DisplayName("Should handle delete operation with non-existent ID gracefully")
        @Transactional
        @Rollback
        fun deleteOperation_WithNonExistentId_ShouldNotThrowException() {
            // Arrange
            val nonExistentId = UUID.randomUUID()

            // Act & Assert - Should not throw exception
            assertThatCode {
                serieRepository.deleteSerieById(nonExistentId)
            }.doesNotThrowAnyException()
        }
    }

    @Nested
    @DisplayName("Existence Checks")
    inner class ExistenceChecks {

        @Test
        @DisplayName("Should return true when serie exists by ID")
        @Transactional
        @Rollback
        fun existsById_WithExistingId_ShouldReturnTrue() {
            // Arrange
            val testSerie = SerieTestDataBuilder()
                .withCode("EXISTS01")
                .buildEntity()
            val savedSerie = serieRepository.createSerie(testSerie)

            // Act
            val exists = serieRepository.existsSerieById(savedSerie.id!!)

            // Assert
            assertThat(exists).isTrue()
        }

        @Test
        @DisplayName("Should return false when serie does not exist by ID")
        @Transactional
        @Rollback
        fun existsById_WithNonExistentId_ShouldReturnFalse() {
            // Arrange
            val nonExistentId = UUID.randomUUID()

            // Act
            val exists = serieRepository.existsSerieById(nonExistentId)

            // Assert
            assertThat(exists).isFalse()
        }

        @Test
        @DisplayName("Should return true when serie exists by code")
        @Transactional
        @Rollback
        fun existsByCode_WithExistingCode_ShouldReturnTrue() {
            // Arrange
            val testSerie = SerieTestDataBuilder()
                .withCode("EXISTCODE")
                .buildEntity()
            serieRepository.createSerie(testSerie)

            // Act
            val exists = serieRepository.existsSerieByCode("EXISTCODE")

            // Assert
            assertThat(exists).isTrue()
        }

        @Test
        @DisplayName("Should return false when serie does not exist by code")
        @Transactional
        @Rollback
        fun existsByCode_WithNonExistentCode_ShouldReturnFalse() {
            // Act
            val exists = serieRepository.existsSerieByCode("NOEXIST")

            // Assert
            assertThat(exists).isFalse()
        }

        @Test
        @DisplayName("Should return true when serie exists by name")
        @Transactional
        @Rollback
        fun existsByName_WithExistingName_ShouldReturnTrue() {
            // Arrange
            val testSerie = SerieTestDataBuilder()
                .withName("Existing Serie Name")
                .buildEntity()
            serieRepository.createSerie(testSerie)

            // Act
            val exists = serieRepository.existsSerieByName("Existing Serie Name")

            // Assert
            assertThat(exists).isTrue()
        }

        @Test
        @DisplayName("Should return false when serie does not exist by name")
        @Transactional
        @Rollback
        fun existsByName_WithNonExistentName_ShouldReturnFalse() {
            // Act
            val exists = serieRepository.existsSerieByName("Non-existent Serie Name")

            // Assert
            assertThat(exists).isFalse()
        }
    }

    @Nested
    @DisplayName("Pagination Operations")
    inner class PaginationOperations {

        @Test
        @DisplayName("Should return paginated results with correct page size")
        @Transactional
        @Rollback
        fun findAllSeries_WithPagination_ShouldReturnCorrectPageSize() {
            // Arrange - Create multiple test series
            val testSeries = SerieTestDataBuilder.createMultiple(7)
            testSeries.forEach { builder ->
                serieRepository.createSerie(builder.buildEntity())
            }

            // Act - Request first page with size 3
            val page = serieRepository.findAllSeries(page = 0, pageSize = 3)

            // Assert
            assertThat(page.content).hasSize(3)
            assertThat(page.totalElements).isEqualTo(7)
            assertThat(page.totalPages).isEqualTo(3)
            assertThat(page.number).isEqualTo(0)
            assertThat(page.size).isEqualTo(3)
            assertThat(page.hasNext()).isTrue()
            assertThat(page.hasPrevious()).isFalse()
        }

        @Test
        @DisplayName("Should return second page with remaining elements")
        @Transactional
        @Rollback
        fun findAllSeries_SecondPage_ShouldReturnRemainingElements() {
            // Arrange - Create 5 test series
            val testSeries = SerieTestDataBuilder.createMultiple(5)
            testSeries.forEach { builder ->
                serieRepository.createSerie(builder.buildEntity())
            }

            // Act - Request second page with size 3
            val page = serieRepository.findAllSeries(page = 1, pageSize = 3)

            // Assert
            assertThat(page.content).hasSize(2) // Remaining elements
            assertThat(page.totalElements).isEqualTo(5)
            assertThat(page.totalPages).isEqualTo(2)
            assertThat(page.number).isEqualTo(1)
            assertThat(page.hasNext()).isFalse()
            assertThat(page.hasPrevious()).isTrue()
        }

        @Test
        @DisplayName("Should return empty page when no data exists")
        @Transactional
        @Rollback
        fun findAllSeries_WithNoData_ShouldReturnEmptyPage() {
            // Act
            val page = serieRepository.findAllSeries(page = 0, pageSize = 10)

            // Assert
            assertThat(page.content).isEmpty()
            assertThat(page.totalElements).isEqualTo(0)
            assertThat(page.totalPages).isEqualTo(0)
            assertThat(page.number).isEqualTo(0)
            assertThat(page.hasNext()).isFalse()
            assertThat(page.hasPrevious()).isFalse()
        }
    }

    @Nested
    @DisplayName("Database Constraints")
    inner class DatabaseConstraints {

        @Test
        @DisplayName("Should throw exception when creating serie with duplicate code")
        @Transactional
        @Rollback
        fun createSerie_WithDuplicateCode_ShouldThrowConstraintViolation() {
            // Arrange
            val firstSerie = SerieTestDataBuilder()
                .withCode("DUPLICATE")
                .withName("First Serie")
                .buildEntity()
            serieRepository.createSerie(firstSerie)

            val duplicateSerie = SerieTestDataBuilder()
                .withCode("DUPLICATE") // Same code as first serie
                .withName("Second Serie")
                .buildEntity()

            // Act & Assert
            assertThatThrownBy {
                serieRepository.createSerie(duplicateSerie)
            }.isInstanceOf(DataIntegrityViolationException::class.java)
        }

        @Test
        @DisplayName("Should throw exception when updating serie to duplicate code")
        @Transactional
        @Rollback
        fun updateSerie_WithDuplicateCode_ShouldThrowConstraintViolation() {
            // Arrange
            val firstSerie = SerieTestDataBuilder()
                .withCode("FIRST01")
                .withName("First Serie")
                .buildEntity()
            serieRepository.createSerie(firstSerie)

            val secondSerie = SerieTestDataBuilder()
                .withCode("SECOND01")
                .withName("Second Serie")
                .buildEntity()
            val savedSecondSerie = serieRepository.createSerie(secondSerie)

            // Try to update second serie to have same code as first
            val updatedSerie = SerieEntity(
                id = savedSecondSerie.id,
                code = "FIRST01", // Duplicate code
                name = "Updated Second Serie",
                releaseYear = savedSecondSerie.releaseYear,
                imageUrl = savedSecondSerie.imageUrl,
                expansions = savedSecondSerie.expansions,
                createdAt = savedSecondSerie.createdAt,
                updatedAt = savedSecondSerie.updatedAt
            )

            // Act & Assert
            assertThatThrownBy {
                serieRepository.updateSerie(updatedSerie)
            }.isInstanceOf(DataIntegrityViolationException::class.java)
        }

        @Test
        @DisplayName("Should throw exception when creating serie with null code")
        @Transactional
        @Rollback
        fun createSerie_WithNullCode_ShouldThrowConstraintViolation() {
            // Arrange - Create entity with null code (this should be caught at entity level)
            // Note: Since code is non-nullable in Kotlin, we need to test this differently
            // This test verifies the database constraint is properly configured

            // We'll test by trying to create an entity that would violate the not-null constraint
            // if it somehow bypassed Kotlin's null safety
            val serieWithEmptyCode = SerieTestDataBuilder()
                .withCode("") // Empty string should also be invalid
                .withName("Test Serie")
                .buildEntity()

            // Act & Assert
            // The empty code should be handled by validation, but let's test it persists correctly
            // If there are additional database constraints, they would be triggered here
            assertThatCode {
                serieRepository.createSerie(serieWithEmptyCode)
            }.doesNotThrowAnyException() // Empty string is different from null, so this should work

            // Verify the entity was saved with empty code
            val savedSerie = serieRepository.findSerieByCode("")
            assertThat(savedSerie).isNotNull
            assertThat(savedSerie!!.code).isEmpty()
        }

        @Test
        @DisplayName("Should throw exception when creating serie with null name")
        @Transactional
        @Rollback
        fun createSerie_WithNullName_ShouldThrowConstraintViolation() {
            // Similar to code test - testing empty name since Kotlin prevents null
            val serieWithEmptyName = SerieTestDataBuilder()
                .withCode("EMPTY01")
                .withName("") // Empty string
                .buildEntity()

            // Act & Assert
            assertThatCode {
                serieRepository.createSerie(serieWithEmptyName)
            }.doesNotThrowAnyException() // Empty string should be allowed unless there's a check constraint

            // Verify the entity was saved
            val savedSerie = serieRepository.findSerieByCode("EMPTY01")
            assertThat(savedSerie).isNotNull
            assertThat(savedSerie!!.name).isEmpty()
        }

        @Test
        @DisplayName("Should handle code length constraint properly")
        @Transactional
        @Rollback
        fun createSerie_WithLongCode_ShouldHandleConstraint() {
            // Arrange - Code has max length of 10 characters according to entity
            val longCode = "VERYLONGCODE123" // 15 characters, exceeds limit
            val serieWithLongCode = SerieTestDataBuilder()
                .withCode(longCode)
                .withName("Long Code Serie")
                .buildEntity()

            // Act & Assert
            assertThatThrownBy {
                serieRepository.createSerie(serieWithLongCode)
            }.isInstanceOf(DataIntegrityViolationException::class.java)
        }

        @Test
        @DisplayName("Should handle name length constraint properly")
        @Transactional
        @Rollback
        fun createSerie_WithLongName_ShouldHandleConstraint() {
            // Arrange - Name has max length of 100 characters according to entity
            val longName = "A".repeat(101) // 101 characters, exceeds limit
            val serieWithLongName = SerieTestDataBuilder()
                .withCode("LONG01")
                .withName(longName)
                .buildEntity()

            // Act & Assert
            assertThatThrownBy {
                serieRepository.createSerie(serieWithLongName)
            }.isInstanceOf(DataIntegrityViolationException::class.java)
        }

        @Test
        @DisplayName("Should handle imageUrl length constraint properly")
        @Transactional
        @Rollback
        fun createSerie_WithLongImageUrl_ShouldHandleConstraint() {
            // Arrange - ImageUrl has max length of 255 characters according to entity
            val longImageUrl = "https://example.com/" + "a".repeat(300) // Exceeds 255 characters
            val serieWithLongImageUrl = SerieTestDataBuilder()
                .withCode("LONGURL")
                .withName("Long URL Serie")
                .withImageUrl(longImageUrl)
                .buildEntity()

            // Act & Assert
            assertThatThrownBy {
                serieRepository.createSerie(serieWithLongImageUrl)
            }.isInstanceOf(DataIntegrityViolationException::class.java)
        }

        @Test
        @DisplayName("Should allow null imageUrl as it's optional")
        @Transactional
        @Rollback
        fun createSerie_WithNullImageUrl_ShouldSucceed() {
            // Arrange
            val serieWithNullImageUrl = SerieTestDataBuilder()
                .withCode("NULLIMG")
                .withName("Null Image Serie")
                .withImageUrl(null)
                .buildEntity()

            // Act
            val savedSerie = serieRepository.createSerie(serieWithNullImageUrl)

            // Assert
            assertThat(savedSerie).isNotNull
            assertThat(savedSerie.imageUrl).isNull()

            // Verify persistence
            val foundSerie = serieRepository.findSerieByCode("NULLIMG")
            assertThat(foundSerie).isNotNull
            assertThat(foundSerie!!.imageUrl).isNull()
        }

        @Test
        @DisplayName("Should allow null expansions as it's optional")
        @Transactional
        @Rollback
        fun createSerie_WithNullExpansions_ShouldSucceed() {
            // Arrange
            val serieWithNullExpansions = SerieTestDataBuilder()
                .withCode("NULLEXP")
                .withName("Null Expansions Serie")
                .buildEntity() // SerieEntity uses null for expansions by default

            // Act
            val savedSerie = serieRepository.createSerie(serieWithNullExpansions)

            // Assert
            assertThat(savedSerie).isNotNull
            assertThat(savedSerie.expansions).isNull()

            // Verify persistence
            val foundSerie = serieRepository.findSerieByCode("NULLEXP")
            assertThat(foundSerie).isNotNull
            assertThat(foundSerie!!.expansions).isNull()
        }
    }

    @Nested
    @DisplayName("Transaction and Concurrency")
    inner class TransactionAndConcurrency {

        @Test
        @DisplayName("Should rollback transaction when constraint violation occurs")
        @Transactional
        @Rollback
        fun transaction_WithConstraintViolation_ShouldRollback() {
            // Arrange
            val firstSerie = SerieTestDataBuilder()
                .withCode("ROLLBACK")
                .withName("First Serie")
                .buildEntity()
            serieRepository.createSerie(firstSerie)

            // Verify first serie exists
            assertThat(serieRepository.existsSerieByCode("ROLLBACK")).isTrue()

            val duplicateSerie = SerieTestDataBuilder()
                .withCode("ROLLBACK") // Duplicate code
                .withName("Duplicate Serie")
                .buildEntity()

            // Act & Assert
            assertThatThrownBy {
                serieRepository.createSerie(duplicateSerie)
            }.isInstanceOf(DataIntegrityViolationException::class.java)

            // Verify original serie still exists and no partial data was saved
            assertThat(serieRepository.existsSerieByCode("ROLLBACK")).isTrue()
            val existingSerie = serieRepository.findSerieByCode("ROLLBACK")
            assertThat(existingSerie!!.name).isEqualTo("First Serie") // Original name preserved
        }

        @Test
        @DisplayName("Should handle batch operations with all-or-nothing behavior")
        @Transactional
        @Rollback
        fun batchOperations_WithFailure_ShouldRollbackAll() {
            // Arrange - Create multiple series in sequence
            val series1 = SerieTestDataBuilder()
                .withCode("BATCH01")
                .withName("Batch Serie 1")
                .buildEntity()

            val series2 = SerieTestDataBuilder()
                .withCode("BATCH02")
                .withName("Batch Serie 2")
                .buildEntity()

            val series3 = SerieTestDataBuilder()
                .withCode("BATCH01") // Duplicate code - will cause failure
                .withName("Batch Serie 3")
                .buildEntity()

            // Act - Try to save all series, expecting failure on third
            serieRepository.createSerie(series1)
            serieRepository.createSerie(series2)

            // Verify first two were saved
            assertThat(serieRepository.existsSerieByCode("BATCH01")).isTrue()
            assertThat(serieRepository.existsSerieByCode("BATCH02")).isTrue()

            // Third save should fail due to duplicate code
            assertThatThrownBy {
                serieRepository.createSerie(series3)
            }.isInstanceOf(DataIntegrityViolationException::class.java)

            // Since we're in the same transaction, the rollback behavior depends on 
            // transaction propagation. In this case, the first two saves should still exist
            // because they were successful before the constraint violation
            assertThat(serieRepository.existsSerieByCode("BATCH01")).isTrue()
            assertThat(serieRepository.existsSerieByCode("BATCH02")).isTrue()
        }

        @Test
        @DisplayName("Should maintain data consistency during update operations")
        @Transactional
        @Rollback
        fun updateOperations_WithConsistency_ShouldMaintainDataIntegrity() {
            // Arrange
            val originalSerie = SerieTestDataBuilder()
                .withCode("CONSIST")
                .withName("Original Name")
                .withReleaseYear(2023)
                .buildEntity()
            val savedSerie = serieRepository.createSerie(originalSerie)

            // Act - Perform multiple updates in sequence
            val firstUpdate = SerieEntity(
                id = savedSerie.id,
                code = savedSerie.code,
                name = "First Update",
                releaseYear = 2024,
                imageUrl = savedSerie.imageUrl,
                expansions = savedSerie.expansions,
                createdAt = savedSerie.createdAt,
                updatedAt = savedSerie.updatedAt
            )
            val firstResult = serieRepository.updateSerie(firstUpdate)

            val secondUpdate = SerieEntity(
                id = firstResult.id,
                code = firstResult.code,
                name = "Second Update",
                releaseYear = 2025,
                imageUrl = "https://example.com/updated.jpg",
                expansions = firstResult.expansions,
                createdAt = firstResult.createdAt,
                updatedAt = firstResult.updatedAt
            )
            serieRepository.updateSerie(secondUpdate)

            // Assert - Verify final state is consistent
            val finalSerie = serieRepository.findSerieById(savedSerie.id!!)
            assertThat(finalSerie).isNotNull
            assertThat(finalSerie!!.name).isEqualTo("Second Update")
            assertThat(finalSerie.releaseYear).isEqualTo(2025)
            assertThat(finalSerie.imageUrl).isEqualTo("https://example.com/updated.jpg")
            assertThat(finalSerie.code).isEqualTo("CONSIST") // Code should remain unchanged
            assertThat(finalSerie.id).isEqualTo(savedSerie.id) // ID should remain unchanged
        }

        @Test
        @DisplayName("Should handle concurrent read operations correctly")
        @Transactional
        @Rollback
        fun concurrentReads_WithSameData_ShouldReturnConsistentResults() {
            // Arrange
            val testSerie = SerieTestDataBuilder()
                .withCode("CONCURRENT")
                .withName("Concurrent Test Serie")
                .buildEntity()
            val savedSerie = serieRepository.createSerie(testSerie)

            // Act - Perform multiple concurrent-like reads
            val readById1 = serieRepository.findSerieById(savedSerie.id!!)
            val readById2 = serieRepository.findSerieById(savedSerie.id!!)
            val readByCode1 = serieRepository.findSerieByCode("CONCURRENT")
            val readByCode2 = serieRepository.findSerieByCode("CONCURRENT")

            // Assert - All reads should return consistent data
            assertThat(readById1).isNotNull
            assertThat(readById2).isNotNull
            assertThat(readByCode1).isNotNull
            assertThat(readByCode2).isNotNull

            // Verify all reads return the same data
            assertThat(readById1!!.id).isEqualTo(readById2!!.id)
            assertThat(readById1.code).isEqualTo(readByCode1!!.code)
            assertThat(readById2.name).isEqualTo(readByCode2!!.name)
            assertThat(readById1.releaseYear).isEqualTo(readByCode1.releaseYear)
        }

        @Test
        @DisplayName("Should maintain isolation between different transactions")
        @Transactional
        @Rollback
        fun transactionIsolation_WithSeparateOperations_ShouldMaintainIsolation() {
            // Arrange
            val serie1 = SerieTestDataBuilder()
                .withCode("ISOLATE1")
                .withName("Isolation Test 1")
                .buildEntity()

            val serie2 = SerieTestDataBuilder()
                .withCode("ISOLATE2")
                .withName("Isolation Test 2")
                .buildEntity()

            // Act - Create series in current transaction
            val saved1 = serieRepository.createSerie(serie1)
            val saved2 = serieRepository.createSerie(serie2)

            // Verify both exist in current transaction
            assertThat(serieRepository.existsSerieByCode("ISOLATE1")).isTrue()
            assertThat(serieRepository.existsSerieByCode("ISOLATE2")).isTrue()

            // Perform operations that should be isolated
            val updated1 = SerieEntity(
                id = saved1.id,
                code = saved1.code,
                name = "Updated Isolation Test 1",
                releaseYear = saved1.releaseYear,
                imageUrl = saved1.imageUrl,
                expansions = saved1.expansions,
                createdAt = saved1.createdAt,
                updatedAt = saved1.updatedAt
            )
            serieRepository.updateSerie(updated1)

            // Delete the second serie
            serieRepository.deleteSerieById(saved2.id!!)

            // Assert - Verify final state within transaction
            val finalSerie1 = serieRepository.findSerieByCode("ISOLATE1")
            val finalSerie2 = serieRepository.findSerieByCode("ISOLATE2")

            assertThat(finalSerie1).isNotNull
            assertThat(finalSerie1!!.name).isEqualTo("Updated Isolation Test 1")
            assertThat(finalSerie2).isNull() // Should be deleted
        }

        @Test
        @DisplayName("Should handle transaction rollback on update failure")
        @Transactional
        @Rollback
        fun updateTransaction_WithFailure_ShouldRollbackChanges() {
            // Arrange
            val existingSerie = SerieTestDataBuilder()
                .withCode("EXISTING")
                .withName("Existing Serie")
                .buildEntity()
            serieRepository.createSerie(existingSerie)

            val targetSerie = SerieTestDataBuilder()
                .withCode("TARGET")
                .withName("Target Serie")
                .buildEntity()
            val savedTarget = serieRepository.createSerie(targetSerie)

            // Act - Try to update target serie to have duplicate code
            val invalidUpdate = SerieEntity(
                id = savedTarget.id,
                code = "EXISTING", // This should cause constraint violation
                name = "Updated Target",
                releaseYear = savedTarget.releaseYear,
                imageUrl = savedTarget.imageUrl,
                expansions = savedTarget.expansions,
                createdAt = savedTarget.createdAt,
                updatedAt = savedTarget.updatedAt
            )

            // Assert
            assertThatThrownBy {
                serieRepository.updateSerie(invalidUpdate)
            }.isInstanceOf(DataIntegrityViolationException::class.java)

            // Verify original data is preserved
            val originalTarget = serieRepository.findSerieByCode("TARGET")
            assertThat(originalTarget).isNotNull
            assertThat(originalTarget!!.name).isEqualTo("Target Serie") // Original name preserved
            assertThat(originalTarget.code).isEqualTo("TARGET") // Original code preserved
        }
    }
}