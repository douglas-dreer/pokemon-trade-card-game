package br.com.pokemon.tradecardgame.integration.testdata

import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DatabaseTestDataManagerTest {

    private val mockRepository = mockk<SerieRepositoryPort>(relaxed = true)
    private lateinit var dataManager: DatabaseTestDataManager

    @BeforeEach
    fun setUp() {
        clearMocks(mockRepository)
        dataManager = DatabaseTestDataManager()
        // Use reflection to set the private repository field for testing
        val repositoryField = DatabaseTestDataManager::class.java.getDeclaredField("serieRepository")
        repositoryField.isAccessible = true
        repositoryField.set(dataManager, mockRepository)
    }

    @Test
    fun `createTestSerie should create and track serie with default builder`() {
        // Given
        val expectedEntity = SerieTestDataBuilder().buildEntity()
        val savedEntity = expectedEntity.copy(id = UUID.randomUUID())

        every { mockRepository.createSerie(any()) } returns savedEntity

        // When
        val result = dataManager.createTestSerie()

        // Then
        verify { mockRepository.createSerie(any()) }
        assertNotNull(result.id)
        assertEquals(savedEntity.id, result.id)
    }

    @Test
    fun `createTestSerie should create serie with custom builder`() {
        // Given
        val customBuilder = SerieTestDataBuilder()
            .withCode("CUSTOM01")
            .withName("Custom Test Serie")
            .withReleaseYear(2023)

        val expectedEntity = customBuilder.buildEntity()
        val savedEntity = expectedEntity.copy(id = UUID.randomUUID())

        every { mockRepository.createSerie(any()) } returns savedEntity

        // When
        val result = dataManager.createTestSerie(customBuilder)

        // Then
        verify { mockRepository.createSerie(match { it.code == "CUSTOM01" && it.name == "Custom Test Serie" }) }
        assertEquals("CUSTOM01", result.code)
        assertEquals("Custom Test Serie", result.name)
        assertEquals(2023, result.releaseYear)
    }

    @Test
    fun `createMultipleTestSeries should create multiple series with default factory`() {
        // Given
        val count = 3
        every { mockRepository.createSerie(any()) } returnsMany listOf(
            SerieTestDataBuilder().withCode("MULTI001").buildEntity().copy(id = UUID.randomUUID()),
            SerieTestDataBuilder().withCode("MULTI002").buildEntity().copy(id = UUID.randomUUID()),
            SerieTestDataBuilder().withCode("MULTI003").buildEntity().copy(id = UUID.randomUUID())
        )

        // When
        val result = dataManager.createMultipleTestSeries(count)

        // Then
        verify(exactly = 3) { mockRepository.createSerie(any()) }
        assertEquals(3, result.size)
        assertTrue(result[0].code.startsWith("MULTI"))
        assertTrue(result[1].code.startsWith("MULTI"))
        assertTrue(result[2].code.startsWith("MULTI"))
    }

    @Test
    fun `createMultipleTestSeries should create series with custom factory`() {
        // Given
        val count = 2
        every { mockRepository.createSerie(any()) } returnsMany listOf(
            SerieTestDataBuilder().withCode("FACTORY1").buildEntity().copy(id = UUID.randomUUID()),
            SerieTestDataBuilder().withCode("FACTORY2").buildEntity().copy(id = UUID.randomUUID())
        )

        // When
        val result = dataManager.createMultipleTestSeries(count) { index ->
            SerieTestDataBuilder()
                .withCode("FACTORY$index")
                .withName("Factory Serie $index")
        }

        // Then
        verify(exactly = 2) { mockRepository.createSerie(any()) }
        assertEquals(2, result.size)
    }

    @Test
    fun `createTestSerieWithCode should create serie with specific code`() {
        // Given
        val code = "SPECIFIC01"
        val name = "Specific Serie Name"
        val expectedEntity = SerieTestDataBuilder().withCode(code).withName(name).buildEntity()
        val savedEntity = expectedEntity.copy(id = UUID.randomUUID())

        every { mockRepository.createSerie(any()) } returns savedEntity

        // When
        val result = dataManager.createTestSerieWithCode(code, name)

        // Then
        verify { mockRepository.createSerie(match { it.code == code && it.name == name }) }
        assertEquals(code, result.code)
        assertEquals(name, result.name)
    }

    @Test
    fun `createTestSerieWithCode should create serie with generated name when name is null`() {
        // Given
        val code = "GENERATED01"
        val expectedEntity = SerieTestDataBuilder().withCode(code).buildEntity()
        val savedEntity = expectedEntity.copy(id = UUID.randomUUID())

        every { mockRepository.createSerie(any()) } returns savedEntity

        // When
        val result = dataManager.createTestSerieWithCode(code)

        // Then
        verify { mockRepository.createSerie(match { it.code == code }) }
        assertEquals(code, result.code)
        assertNotNull(result.name)
    }

    @Test
    fun `createTestSerieWithId should create serie with specific id`() {
        // Given
        val specificId = UUID.randomUUID()
        val expectedEntity = SerieTestDataBuilder().withId(specificId).buildEntity()
        val savedEntity = expectedEntity.copy(id = specificId)

        every { mockRepository.createSerie(any()) } returns savedEntity

        // When
        val result = dataManager.createTestSerieWithId(specificId)

        // Then
        verify { mockRepository.createSerie(match { it.id == specificId }) }
        assertEquals(specificId, result.id)
    }

    @Test
    fun `verifySerieExists should return true when serie exists`() {
        // Given
        val serieId = UUID.randomUUID()
        every { mockRepository.existsSerieById(serieId) } returns true

        // When
        val result = dataManager.verifySerieExists(serieId)

        // Then
        assertTrue(result)
        verify { mockRepository.existsSerieById(serieId) }
    }

    @Test
    fun `verifySerieExists should return false when serie does not exist`() {
        // Given
        val serieId = UUID.randomUUID()
        every { mockRepository.existsSerieById(serieId) } returns false

        // When
        val result = dataManager.verifySerieExists(serieId)

        // Then
        verify { mockRepository.existsSerieById(serieId) }
        assertFalse(result)
    }

    @Test
    fun `verifySerieExistsByCode should return true when serie exists`() {
        // Given
        val code = "EXISTS01"
        every { mockRepository.existsSerieByCode(code) } returns true

        // When
        val result = dataManager.verifySerieExistsByCode(code)

        // Then
        verify { mockRepository.existsSerieByCode(code) }
        assertTrue(result)
    }

    @Test
    fun `verifySerieExistsByCode should return false when serie does not exist`() {
        // Given
        val code = "NOTEXISTS01"
        every { mockRepository.existsSerieByCode(code) } returns false

        // When
        val result = dataManager.verifySerieExistsByCode(code)

        // Then
        assertFalse(result)
        verify { mockRepository.existsSerieByCode(code) }
    }

    @Test
    fun `getSerieCount should return correct count from repository`() {
        // Given
        val expectedCount = 5L
        val mockPage = PageImpl(
            listOf<SerieEntity>(),
            org.springframework.data.domain.PageRequest.of(0, Int.MAX_VALUE),
            expectedCount
        )
        every { mockRepository.findAllSeries(0, Int.MAX_VALUE) } returns mockPage

        // When
        val result = dataManager.getSerieCount()

        // Then
        verify { mockRepository.findAllSeries(0, Int.MAX_VALUE) }
        assertEquals(expectedCount, result)
    }

    @Test
    fun `getTestSerieById should return serie when found`() {
        // Given
        val serieId = UUID.randomUUID()
        val expectedSerie = SerieTestDataBuilder().withId(serieId).buildEntity()
        every { mockRepository.findSerieById(serieId) } returns expectedSerie

        // When
        val result = dataManager.getTestSerieById(serieId)

        // Then
        verify { mockRepository.findSerieById(serieId) }
        assertEquals(expectedSerie, result)
    }

    @Test
    fun `getTestSerieById should return null when not found`() {
        // Given
        val serieId = UUID.randomUUID()
        every { mockRepository.findSerieById(serieId) } returns null

        // When
        val result = dataManager.getTestSerieById(serieId)

        // Then
        verify { mockRepository.findSerieById(serieId) }
        assertEquals(null, result)
    }

    @Test
    fun `getTestSerieByCode should return serie when found`() {
        // Given
        val code = "FOUND01"
        val expectedSerie = SerieTestDataBuilder().withCode(code).buildEntity()
        every { mockRepository.findSerieByCode(code) } returns expectedSerie

        // When
        val result = dataManager.getTestSerieByCode(code)

        // Then
        verify { mockRepository.findSerieByCode(code) }
        assertEquals(expectedSerie, result)
    }

    @Test
    fun `getTestSerieByCode should return null when not found`() {
        // Given
        val code = "NOTFOUND01"
        every { mockRepository.findSerieByCode(code) } returns null

        // When
        val result = dataManager.getTestSerieByCode(code)

        // Then
        verify { mockRepository.findSerieByCode(code) }
        assertEquals(null, result)
    }

    @Test
    fun `cleanupTestData should delete all tracked series`() {
        // Given
        val serieId1 = UUID.randomUUID()
        val serieId2 = UUID.randomUUID()

        // Create some test data to track IDs
        val entity1 = SerieTestDataBuilder().buildEntity().copy(id = serieId1)
        val entity2 = SerieTestDataBuilder().buildEntity().copy(id = serieId2)

        every { mockRepository.createSerie(any()) } returnsMany listOf(entity1, entity2)
        every { mockRepository.existsSerieById(serieId1) } returns true
        every { mockRepository.existsSerieById(serieId2) } returns true
        every { mockRepository.deleteSerieById(any()) } just Runs

        // Create test data to populate the tracking set
        dataManager.createTestSerie()
        dataManager.createTestSerie()

        // When
        dataManager.cleanupTestData()

        // Then
        verify { mockRepository.deleteSerieById(serieId1) }
        verify { mockRepository.deleteSerieById(serieId2) }
    }

    @Test
    fun `cleanupTestData should handle deletion errors gracefully`() {
        // Given
        val serieId = UUID.randomUUID()
        val entity = SerieTestDataBuilder().buildEntity().copy(id = serieId)

        every { mockRepository.createSerie(any()) } returns entity
        every { mockRepository.existsSerieById(serieId) } returns true
        every { mockRepository.deleteSerieById(serieId) } throws RuntimeException("Database error")

        // Create test data
        dataManager.createTestSerie()

        // When & Then - Should not throw exception
        dataManager.cleanupTestData()

        verify { mockRepository.deleteSerieById(serieId) }
    }

    @Test
    fun `cleanupAllSeries should delete all series from repository`() {
        // Given
        val series = listOf(
            SerieTestDataBuilder().buildEntity().copy(id = UUID.randomUUID()),
            SerieTestDataBuilder().buildEntity().copy(id = UUID.randomUUID())
        )
        val mockPage = PageImpl(series)

        every { mockRepository.findAllSeries(0, Int.MAX_VALUE) } returns mockPage
        every { mockRepository.deleteSerieById(any()) } just Runs

        // When
        dataManager.cleanupAllSeries()

        // Then
        verify { mockRepository.findAllSeries(0, Int.MAX_VALUE) }
        verify { mockRepository.deleteSerieById(series[0].id!!) }
        verify { mockRepository.deleteSerieById(series[1].id!!) }
    }

    @Test
    fun `setupCleanState should cleanup all series and reset tracking`() {
        // Given
        val series = listOf(SerieTestDataBuilder().buildEntity().copy(id = UUID.randomUUID()))
        val mockPage = PageImpl(series)

        every { mockRepository.findAllSeries(0, Int.MAX_VALUE) } returns mockPage
        every { mockRepository.deleteSerieById(any()) } just Runs

        // When
        dataManager.setupCleanState()

        // Then
        verify { mockRepository.findAllSeries(0, Int.MAX_VALUE) }
        verify { mockRepository.deleteSerieById(series[0].id!!) }
    }

    @Test
    fun `createPaginationTestData should create series for pagination testing`() {
        // Given
        val totalItems = 3
        val namePrefix = "Pagination"

        every { mockRepository.createSerie(any()) } returnsMany listOf(
            SerieTestDataBuilder().withCode("PAGE0001").buildEntity().copy(id = UUID.randomUUID()),
            SerieTestDataBuilder().withCode("PAGE0002").buildEntity().copy(id = UUID.randomUUID()),
            SerieTestDataBuilder().withCode("PAGE0003").buildEntity().copy(id = UUID.randomUUID())
        )

        // When
        val result = dataManager.createPaginationTestData(totalItems, namePrefix)

        // Then
        verify(exactly = 3) { mockRepository.createSerie(any()) }
        assertEquals(3, result.size)
    }

    @Test
    fun `createTestDataByYear should create series for specific years`() {
        // Given
        val years = listOf(2020, 2021, 2022)

        every { mockRepository.createSerie(match { it.releaseYear == 2020 }) } returns
                SerieTestDataBuilder().withReleaseYear(2020).buildEntity().copy(id = UUID.randomUUID())
        every { mockRepository.createSerie(match { it.releaseYear == 2021 }) } returns
                SerieTestDataBuilder().withReleaseYear(2021).buildEntity().copy(id = UUID.randomUUID())
        every { mockRepository.createSerie(match { it.releaseYear == 2022 }) } returns
                SerieTestDataBuilder().withReleaseYear(2022).buildEntity().copy(id = UUID.randomUUID())

        // When
        val result = dataManager.createTestDataByYear(years)

        // Then
        verify(exactly = 3) { mockRepository.createSerie(any()) }
        assertEquals(3, result.size)
        assertTrue(result.containsKey(2020))
        assertTrue(result.containsKey(2021))
        assertTrue(result.containsKey(2022))
    }

    @Test
    fun `should track created serie IDs for cleanup`() {
        // Given
        val serieId1 = UUID.randomUUID()
        val serieId2 = UUID.randomUUID()

        val entity1 = SerieTestDataBuilder().buildEntity().copy(id = serieId1)
        val entity2 = SerieTestDataBuilder().buildEntity().copy(id = serieId2)

        every { mockRepository.createSerie(any()) } returnsMany listOf(entity1, entity2)
        every { mockRepository.existsSerieById(any()) } returns true
        every { mockRepository.deleteSerieById(any()) } just Runs

        // When
        dataManager.createTestSerie()
        dataManager.createTestSerie()
        dataManager.cleanupTestData()

        // Then
        verify { mockRepository.deleteSerieById(serieId1) }
        verify { mockRepository.deleteSerieById(serieId2) }
    }

    @Test
    fun `should handle null serie ID gracefully during tracking`() {
        // Given
        val entityWithNullId = SerieTestDataBuilder().buildEntity().copy(id = null)
        every { mockRepository.createSerie(any()) } returns entityWithNullId

        // When & Then - Should not throw exception
        dataManager.createTestSerie()
        dataManager.cleanupTestData()

        // Verify no delete calls were made since ID was null
        verify(exactly = 0) { mockRepository.deleteSerieById(any()) }
        verify(exactly = 0) { mockRepository.existsSerieById(any()) }
    }

    @Test
    fun `should skip deletion for non-existent series during cleanup`() {
        // Given
        val serieId = UUID.randomUUID()
        val entity = SerieTestDataBuilder().buildEntity().copy(id = serieId)

        every { mockRepository.createSerie(any()) } returns entity
        every { mockRepository.existsSerieById(serieId) } returns false

        // Create test data
        dataManager.createTestSerie()

        // When
        dataManager.cleanupTestData()

        // Then
        verify { mockRepository.existsSerieById(serieId) }
        verify(exactly = 0) { mockRepository.deleteSerieById(serieId) }
    }
}