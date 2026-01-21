package co.trall.inertia.props

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScrollPropTest {

    @Test
    fun `resolves to ScrollData with items and metadata`() {
        val page = SimplePageData.of(
            content = listOf("a", "b", "c"),
            pageNumber = 0,
            pageSize = 10,
            totalElements = 25
        )
        val prop = ScrollProp(page)

        val result = prop.resolve() as ScrollData<*>

        assertEquals(listOf("a", "b", "c"), result.items)
        assertEquals(0, result.meta.currentPage)
        assertEquals(3, result.meta.totalPages)
        assertEquals(25, result.meta.totalItems)
        assertTrue(result.meta.hasNextPage)
        assertFalse(result.meta.hasPreviousPage)
        assertTrue(result.meta.isFirstPage)
        assertFalse(result.meta.isLastPage)
    }

    @Test
    fun `default merge mode is APPEND`() {
        val page = SimplePageData.single(listOf(1, 2, 3))
        val prop = ScrollProp(page)

        assertEquals(MergeMode.APPEND, prop.mergeMode())
    }

    @Test
    fun `prepend sets merge mode to PREPEND`() {
        val page = SimplePageData.single(listOf(1, 2, 3))
        val prop = ScrollProp(page).prepend()

        assertEquals(MergeMode.PREPEND, prop.mergeMode())
    }

    @Test
    fun `defer enables deferred behavior with default group`() {
        val page = SimplePageData.single(listOf(1, 2, 3))
        val prop = ScrollProp(page).defer()

        assertTrue(prop.isDeferred())
        assertEquals("default", prop.group())
    }

    @Test
    fun `defer with custom group`() {
        val page = SimplePageData.single(listOf(1, 2, 3))
        val prop = ScrollProp(page).defer("pagination")

        assertTrue(prop.isDeferred())
        assertEquals("pagination", prop.group())
    }

    @Test
    fun `is not deferred by default`() {
        val page = SimplePageData.single(listOf(1, 2, 3))
        val prop = ScrollProp(page)

        assertFalse(prop.isDeferred())
    }

    @Test
    fun `implements Mergeable interface`() {
        val page = SimplePageData.single(listOf(1, 2, 3))
        val prop = ScrollProp(page)

        assertTrue(prop is Mergeable)
    }

    @Test
    fun `implements Deferrable interface`() {
        val page = SimplePageData.single(listOf(1, 2, 3))
        val prop = ScrollProp(page)

        assertTrue(prop is Deferrable)
    }

    @Test
    fun `chaining prepend and defer works correctly`() {
        val page = SimplePageData.single(listOf(1, 2, 3))
        val prop = ScrollProp(page)
            .prepend()
            .defer("custom")

        assertEquals(MergeMode.PREPEND, prop.mergeMode())
        assertTrue(prop.isDeferred())
        assertEquals("custom", prop.group())
    }

    @Test
    fun `handles empty page`() {
        val page = SimplePageData.of(
            content = emptyList<String>(),
            pageNumber = 0,
            pageSize = 10,
            totalElements = 0
        )
        val prop = ScrollProp(page)

        val result = prop.resolve() as ScrollData<*>

        assertTrue(result.items.isEmpty())
        assertEquals(0, result.meta.currentPage)
        assertEquals(0, result.meta.totalPages)
        assertEquals(0, result.meta.totalItems)
        assertFalse(result.meta.hasNextPage)
        assertFalse(result.meta.hasPreviousPage)
        assertTrue(result.meta.isFirstPage)
        assertTrue(result.meta.isLastPage)
    }

    @Test
    fun `handles middle page correctly`() {
        val page = SimplePageData.of(
            content = listOf("d", "e", "f"),
            pageNumber = 1,
            pageSize = 3,
            totalElements = 9
        )
        val prop = ScrollProp(page)

        val result = prop.resolve() as ScrollData<*>

        assertEquals(1, result.meta.currentPage)
        assertEquals(3, result.meta.totalPages)
        assertTrue(result.meta.hasNextPage)
        assertTrue(result.meta.hasPreviousPage)
        assertFalse(result.meta.isFirstPage)
        assertFalse(result.meta.isLastPage)
    }

    @Test
    fun `handles last page correctly`() {
        val page = SimplePageData.of(
            content = listOf("g", "h", "i"),
            pageNumber = 2,
            pageSize = 3,
            totalElements = 9
        )
        val prop = ScrollProp(page)

        val result = prop.resolve() as ScrollData<*>

        assertEquals(2, result.meta.currentPage)
        assertFalse(result.meta.hasNextPage)
        assertTrue(result.meta.hasPreviousPage)
        assertFalse(result.meta.isFirstPage)
        assertTrue(result.meta.isLastPage)
    }
}
