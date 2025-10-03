package br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response

import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PageResponseTest {

    @Test
    fun `should create page response with primary constructor`() {
        // Given
        val content = listOf("Item1", "Item2", "Item3")
        val page = 0
        val pageSize = 10
        val totalElements = 25L
        val totalPages = 3
        val last = false

        // When
        val pageResponse = PageResponse(
            content = content,
            page = page,
            pageSize = pageSize,
            totalElements = totalElements,
            totalPages = totalPages,
            last = last
        )

        // Then
        assertEquals(content, pageResponse.content)
        assertEquals(page, pageResponse.page)
        assertEquals(pageSize, pageResponse.pageSize)
        assertEquals(totalElements, pageResponse.totalElements)
        assertEquals(totalPages, pageResponse.totalPages)
        assertEquals(last, pageResponse.last)
    }

    @Test
    fun `should create page response from Spring Page with data`() {
        // Given
        val content = listOf("Serie1", "Serie2", "Serie3")
        val pageRequest = PageRequest.of(1, 5)
        val springPage = PageImpl(content, pageRequest, 15L)

        // When
        val pageResponse = PageResponse(springPage)

        // Then
        assertEquals(content, pageResponse.content)
        assertEquals(1, pageResponse.page)
        assertEquals(5, pageResponse.pageSize)
        assertEquals(15L, pageResponse.totalElements)
        assertEquals(3, pageResponse.totalPages) // 15 elements / 5 per page = 3 pages
        assertFalse(pageResponse.last) // page 1 of 3, so not last
    }

    @Test
    fun `should create page response from empty Spring Page`() {
        // Given
        val emptyContent = emptyList<String>()
        val pageRequest = PageRequest.of(0, 10)
        val emptySpringPage = PageImpl(emptyContent, pageRequest, 0L)

        // When
        val pageResponse = PageResponse(emptySpringPage)

        // Then
        assertTrue(pageResponse.content.isEmpty())
        assertEquals(0, pageResponse.page)
        assertEquals(10, pageResponse.pageSize)
        assertEquals(0L, pageResponse.totalElements)
        assertEquals(0, pageResponse.totalPages)
        assertTrue(pageResponse.last) // empty page is considered last
    }

    @Test
    fun `should create page response from last page`() {
        // Given
        val content = listOf("LastItem1", "LastItem2")
        val pageRequest = PageRequest.of(2, 5) // page 2 (0-based), 5 items per page
        val springPage = PageImpl(content, pageRequest, 12L) // total 12 items

        // When
        val pageResponse = PageResponse(springPage)

        // Then
        assertEquals(content, pageResponse.content)
        assertEquals(2, pageResponse.page)
        assertEquals(5, pageResponse.pageSize)
        assertEquals(12L, pageResponse.totalElements)
        assertEquals(3, pageResponse.totalPages) // 12 elements / 5 per page = 3 pages (0, 1, 2)
        assertTrue(pageResponse.last) // page 2 is the last page (0-based)
    }

    @Test
    fun `should create page response from first page`() {
        // Given
        val content = listOf("FirstItem1", "FirstItem2", "FirstItem3")
        val pageRequest = PageRequest.of(0, 3)
        val springPage = PageImpl(content, pageRequest, 10L)

        // When
        val pageResponse = PageResponse(springPage)

        // Then
        assertEquals(content, pageResponse.content)
        assertEquals(0, pageResponse.page)
        assertEquals(3, pageResponse.pageSize)
        assertEquals(10L, pageResponse.totalElements)
        assertEquals(4, pageResponse.totalPages) // 10 elements / 3 per page = 4 pages
        assertFalse(pageResponse.last) // page 0 is not the last page
    }

    @Test
    fun `should create page response with single page`() {
        // Given
        val content = listOf("OnlyItem")
        val pageRequest = PageRequest.of(0, 10)
        val springPage = PageImpl(content, pageRequest, 1L)

        // When
        val pageResponse = PageResponse(springPage)

        // Then
        assertEquals(content, pageResponse.content)
        assertEquals(0, pageResponse.page)
        assertEquals(10, pageResponse.pageSize)
        assertEquals(1L, pageResponse.totalElements)
        assertEquals(1, pageResponse.totalPages)
        assertTrue(pageResponse.last) // single page is always last
    }

    @Test
    fun `should handle different content types`() {
        // Given
        data class TestItem(val id: Int, val name: String)

        val content = listOf(
            TestItem(1, "Item1"),
            TestItem(2, "Item2")
        )
        val pageRequest = PageRequest.of(0, 5)
        val springPage = PageImpl(content, pageRequest, 2L)

        // When
        val pageResponse = PageResponse(springPage)

        // Then
        assertEquals(2, pageResponse.content.size)
        assertEquals(TestItem(1, "Item1"), pageResponse.content[0])
        assertEquals(TestItem(2, "Item2"), pageResponse.content[1])
        assertEquals(0, pageResponse.page)
        assertEquals(5, pageResponse.pageSize)
        assertEquals(2L, pageResponse.totalElements)
        assertEquals(1, pageResponse.totalPages)
        assertTrue(pageResponse.last)
    }

    @Test
    fun `should handle large datasets`() {
        // Given
        val content = (1..50).map { "Item$it" }
        val pageRequest = PageRequest.of(5, 50) // page 5, 50 items per page
        val springPage = PageImpl(content, pageRequest, 1000L) // total 1000 items

        // When
        val pageResponse = PageResponse(springPage)

        // Then
        assertEquals(50, pageResponse.content.size)
        assertEquals(5, pageResponse.page)
        assertEquals(50, pageResponse.pageSize)
        assertEquals(1000L, pageResponse.totalElements)
        assertEquals(20, pageResponse.totalPages) // 1000 / 50 = 20 pages
        assertFalse(pageResponse.last) // page 5 of 20, not last
    }

    @Test
    fun `should preserve Spring Page properties correctly`() {
        // Given
        val content = listOf("A", "B", "C", "D")
        val pageRequest = PageRequest.of(3, 4) // page 3, 4 items per page
        val springPage = PageImpl(content, pageRequest, 16L) // exactly 4 pages

        // When
        val pageResponse = PageResponse(springPage)

        // Then
        assertEquals(springPage.content, pageResponse.content)
        assertEquals(springPage.number, pageResponse.page)
        assertEquals(springPage.size, pageResponse.pageSize)
        assertEquals(springPage.totalElements, pageResponse.totalElements)
        assertEquals(springPage.totalPages, pageResponse.totalPages)
        assertEquals(springPage.isLast, pageResponse.last)
    }

    @Test
    fun `should handle edge case with exact page division`() {
        // Given
        val content = listOf("Item1", "Item2", "Item3", "Item4", "Item5")
        val pageRequest = PageRequest.of(1, 5) // page 1, 5 items per page
        val springPage = PageImpl(content, pageRequest, 10L) // exactly 2 pages

        // When
        val pageResponse = PageResponse(springPage)

        // Then
        assertEquals(5, pageResponse.content.size)
        assertEquals(1, pageResponse.page)
        assertEquals(5, pageResponse.pageSize)
        assertEquals(10L, pageResponse.totalElements)
        assertEquals(2, pageResponse.totalPages) // 10 / 5 = 2 pages exactly
        assertTrue(pageResponse.last) // page 1 is the last page (0-based indexing)
    }

    @Test
    fun `should handle partial last page`() {
        // Given
        val content = listOf("LastItem1", "LastItem2") // only 2 items on last page
        val pageRequest = PageRequest.of(2, 5) // page 2, 5 items per page
        val springPage = PageImpl(content, pageRequest, 12L) // 12 total items

        // When
        val pageResponse = PageResponse(springPage)

        // Then
        assertEquals(2, pageResponse.content.size) // partial page
        assertEquals(2, pageResponse.page)
        assertEquals(5, pageResponse.pageSize)
        assertEquals(12L, pageResponse.totalElements)
        assertEquals(3, pageResponse.totalPages) // ceil(12/5) = 3 pages
        assertTrue(pageResponse.last) // this is the last page
    }
}