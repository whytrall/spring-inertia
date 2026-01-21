package co.trall.inertia.props

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PageDataTest {

    @Test
    fun `SimplePageData of creates correct pagination for first page`() {
        val page = SimplePageData.of(
            content = listOf("a", "b", "c"),
            pageNumber = 0,
            pageSize = 3,
            totalElements = 10
        )

        assertEquals(listOf("a", "b", "c"), page.content)
        assertEquals(0, page.number)
        assertEquals(4, page.totalPages) // ceil(10/3) = 4
        assertEquals(10, page.totalElements)
        assertTrue(page.hasNext)
        assertFalse(page.hasPrevious)
        assertTrue(page.isFirst)
        assertFalse(page.isLast)
    }

    @Test
    fun `SimplePageData of creates correct pagination for middle page`() {
        val page = SimplePageData.of(
            content = listOf("d", "e", "f"),
            pageNumber = 1,
            pageSize = 3,
            totalElements = 10
        )

        assertEquals(1, page.number)
        assertEquals(4, page.totalPages)
        assertTrue(page.hasNext)
        assertTrue(page.hasPrevious)
        assertFalse(page.isFirst)
        assertFalse(page.isLast)
    }

    @Test
    fun `SimplePageData of creates correct pagination for last page`() {
        val page = SimplePageData.of(
            content = listOf("j"),
            pageNumber = 3,
            pageSize = 3,
            totalElements = 10
        )

        assertEquals(3, page.number)
        assertEquals(4, page.totalPages)
        assertFalse(page.hasNext)
        assertTrue(page.hasPrevious)
        assertFalse(page.isFirst)
        assertTrue(page.isLast)
    }

    @Test
    fun `SimplePageData of handles empty content`() {
        val page = SimplePageData.of(
            content = emptyList<String>(),
            pageNumber = 0,
            pageSize = 10,
            totalElements = 0
        )

        assertTrue(page.content.isEmpty())
        assertEquals(0, page.number)
        assertEquals(0, page.totalPages)
        assertEquals(0, page.totalElements)
        assertFalse(page.hasNext)
        assertFalse(page.hasPrevious)
        assertTrue(page.isFirst)
        assertTrue(page.isLast)
    }

    @Test
    fun `SimplePageData of handles single page`() {
        val page = SimplePageData.of(
            content = listOf("a", "b"),
            pageNumber = 0,
            pageSize = 10,
            totalElements = 2
        )

        assertEquals(0, page.number)
        assertEquals(1, page.totalPages)
        assertFalse(page.hasNext)
        assertFalse(page.hasPrevious)
        assertTrue(page.isFirst)
        assertTrue(page.isLast)
    }

    @Test
    fun `SimplePageData of handles exact page boundary`() {
        val page = SimplePageData.of(
            content = listOf("a", "b", "c"),
            pageNumber = 0,
            pageSize = 3,
            totalElements = 6
        )

        assertEquals(2, page.totalPages) // exactly 6/3 = 2 pages
        assertTrue(page.hasNext)
        assertFalse(page.hasPrevious)
    }

    @Test
    fun `SimplePageData of handles pageSize of 1`() {
        val page = SimplePageData.of(
            content = listOf("a"),
            pageNumber = 2,
            pageSize = 1,
            totalElements = 5
        )

        assertEquals(5, page.totalPages)
        assertEquals(2, page.number)
        assertTrue(page.hasNext)
        assertTrue(page.hasPrevious)
    }

    @Test
    fun `SimplePageData single creates single page with all items`() {
        val page = SimplePageData.single(listOf("a", "b", "c"))

        assertEquals(listOf("a", "b", "c"), page.content)
        assertEquals(0, page.number)
        assertEquals(1, page.totalPages)
        assertEquals(3, page.totalElements)
        assertFalse(page.hasNext)
        assertFalse(page.hasPrevious)
        assertTrue(page.isFirst)
        assertTrue(page.isLast)
    }

    @Test
    fun `SimplePageData single handles empty list`() {
        val page = SimplePageData.single(emptyList<String>())

        assertTrue(page.content.isEmpty())
        assertEquals(0, page.number)
        assertEquals(1, page.totalPages)
        assertEquals(0, page.totalElements)
        assertFalse(page.hasNext)
        assertFalse(page.hasPrevious)
        assertTrue(page.isFirst)
        assertTrue(page.isLast)
    }

    @Test
    fun `SimplePageData is covariant`() {
        val stringPage: PageData<String> = SimplePageData.single(listOf("a", "b"))
        // Should compile - PageData<String> is assignable to PageData<Any>
        val anyPage: PageData<Any> = stringPage

        assertEquals(listOf("a", "b"), anyPage.content)
    }

    @Test
    fun `SimplePageData of handles zero pageSize gracefully`() {
        val page = SimplePageData.of(
            content = emptyList<String>(),
            pageNumber = 0,
            pageSize = 0,
            totalElements = 0
        )

        assertEquals(0, page.totalPages)
        assertTrue(page.isFirst)
        assertTrue(page.isLast)
    }

    @Test
    fun `hasNext is false when on last page`() {
        // Page 1 of 2 (0-indexed: page 0 and page 1)
        val lastPage = SimplePageData.of(
            content = listOf("c", "d"),
            pageNumber = 1,
            pageSize = 2,
            totalElements = 4
        )

        assertFalse(lastPage.hasNext)
        assertTrue(lastPage.isLast)
    }

    @Test
    fun `hasNext is true when not on last page`() {
        // Page 0 of 2
        val firstPage = SimplePageData.of(
            content = listOf("a", "b"),
            pageNumber = 0,
            pageSize = 2,
            totalElements = 4
        )

        assertTrue(firstPage.hasNext)
        assertFalse(firstPage.isLast)
    }
}
