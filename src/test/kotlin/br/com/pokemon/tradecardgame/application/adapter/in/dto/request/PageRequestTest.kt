package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PageRequestTest {

    @Test
    fun `should create page request with all fields`() {
        // When
        val pageRequest = PageRequest(
            page = 2,
            pageSize = 10,
            sort = "ASC",
            direction = "name"
        )

        // Then
        assertEquals(1, pageRequest.page) // page is adjusted (2-1=1)
        assertEquals(10, pageRequest.pageSize)
        assertEquals("ASC", pageRequest.sort)
        assertEquals("name", pageRequest.direction)
    }

    @Test
    fun `should adjust page number when greater than 0`() {
        // When
        val pageRequest = PageRequest(
            page = 5,
            pageSize = 20,
            sort = "DESC",
            direction = "id"
        )

        // Then
        assertEquals(4, pageRequest.page) // 5-1=4
        assertEquals(20, pageRequest.pageSize)
        assertEquals("DESC", pageRequest.sort)
        assertEquals("id", pageRequest.direction)
    }

    @Test
    fun `should set page to 0 when input is 0`() {
        // When
        val pageRequest = PageRequest(
            page = 0,
            pageSize = 15,
            sort = "ASC",
            direction = "code"
        )

        // Then
        assertEquals(0, pageRequest.page)
        assertEquals(15, pageRequest.pageSize)
        assertEquals("ASC", pageRequest.sort)
        assertEquals("code", pageRequest.direction)
    }

    @Test
    fun `should set page to 0 when input is negative`() {
        // When
        val pageRequest = PageRequest(
            page = -1,
            pageSize = 25,
            sort = "DESC",
            direction = "releaseYear"
        )

        // Then
        assertEquals(0, pageRequest.page)
        assertEquals(25, pageRequest.pageSize)
        assertEquals("DESC", pageRequest.sort)
        assertEquals("releaseYear", pageRequest.direction)
    }

    @Test
    fun `should handle null sort and direction`() {
        // When
        val pageRequest = PageRequest(
            page = 1,
            pageSize = 10,
            sort = null,
            direction = null
        )

        // Then
        assertEquals(0, pageRequest.page) // 1-1=0
        assertEquals(10, pageRequest.pageSize)
        assertNull(pageRequest.sort)
        assertNull(pageRequest.direction)
    }

    @Test
    fun `should handle default sort value`() {
        // When
        val pageRequest = PageRequest(
            page = 3,
            pageSize = 5,
            direction = "name"
        )

        // Then
        assertEquals(2, pageRequest.page) // 3-1=2
        assertEquals(5, pageRequest.pageSize)
        assertEquals("ASC", pageRequest.sort) // default value
        assertEquals("name", pageRequest.direction)
    }

    @Test
    fun `should convert to query successfully`() {
        // Given
        val pageRequest = PageRequest(
            page = 2,
            pageSize = 15,
            sort = "DESC",
            direction = "code"
        )

        // When
        val query = pageRequest.toQuery()

        // Then
        assertEquals(1, query.page) // adjusted page from PageRequest
        assertEquals(15, query.pageSize)
    }

    @Test
    fun `should convert to query with page 0`() {
        // Given
        val pageRequest = PageRequest(
            page = 1,
            pageSize = 20,
            sort = "ASC",
            direction = "id"
        )

        // When
        val query = pageRequest.toQuery()

        // Then
        assertEquals(0, query.page) // 1-1=0
        assertEquals(20, query.pageSize)
    }

    @Test
    fun `should convert to query ignoring sort and direction`() {
        // Given
        val pageRequest = PageRequest(
            page = 4,
            pageSize = 8,
            sort = "DESC",
            direction = "name"
        )

        // When
        val query = pageRequest.toQuery()

        // Then
        // Query only contains page and pageSize, sort and direction are not included
        assertEquals(3, query.page) // 4-1=3
        assertEquals(8, query.pageSize)
    }

    @Test
    fun `should handle edge case with page 1`() {
        // When
        val pageRequest = PageRequest(
            page = 1,
            pageSize = 50,
            sort = "ASC",
            direction = "releaseYear"
        )

        // Then
        assertEquals(0, pageRequest.page) // 1-1=0 (first page in 0-based indexing)
        assertEquals(50, pageRequest.pageSize)
    }

    @Test
    fun `should handle large page numbers`() {
        // When
        val pageRequest = PageRequest(
            page = 1000,
            pageSize = 100,
            sort = "DESC",
            direction = "updatedAt"
        )

        // Then
        assertEquals(999, pageRequest.page) // 1000-1=999
        assertEquals(100, pageRequest.pageSize)
    }

    @Test
    fun `should handle different page sizes`() {
        // Given & When
        val smallPage = PageRequest(page = 1, pageSize = 5, direction = "id")
        val mediumPage = PageRequest(page = 1, pageSize = 25, direction = "id")
        val largePage = PageRequest(page = 1, pageSize = 100, direction = "id")

        // Then
        assertEquals(5, smallPage.pageSize)
        assertEquals(25, mediumPage.pageSize)
        assertEquals(100, largePage.pageSize)

        // All should have page adjusted to 0
        assertEquals(0, smallPage.page)
        assertEquals(0, mediumPage.page)
        assertEquals(0, largePage.page)
    }

    @Test
    fun `should preserve original values except page adjustment`() {
        // Given
        val originalPage = 10
        val pageSize = 30
        val sort = "DESC"
        val direction = "createdAt"

        // When
        val pageRequest = PageRequest(
            page = originalPage,
            pageSize = pageSize,
            sort = sort,
            direction = direction
        )

        // Then
        assertEquals(originalPage - 1, pageRequest.page) // only page is adjusted
        assertEquals(pageSize, pageRequest.pageSize)
        assertEquals(sort, pageRequest.sort)
        assertEquals(direction, pageRequest.direction)
    }

    @Test
    fun `should handle various direction values`() {
        // Given & When
        val directionId = PageRequest(page = 1, pageSize = 10, direction = "id")
        val directionCode = PageRequest(page = 1, pageSize = 10, direction = "code")
        val directionName = PageRequest(page = 1, pageSize = 10, direction = "name")
        val directionYear = PageRequest(page = 1, pageSize = 10, direction = "releaseYear")

        // Then
        assertEquals("id", directionId.direction)
        assertEquals("code", directionCode.direction)
        assertEquals("name", directionName.direction)
        assertEquals("releaseYear", directionYear.direction)
    }
}