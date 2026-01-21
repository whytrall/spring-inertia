package co.trall.inertia.http

import kotlin.test.Test
import kotlin.test.assertEquals

class InertiaHeadersTest {

    @Test
    fun `INERTIA header constant is correct`() {
        assertEquals("X-Inertia", InertiaHeaders.INERTIA)
    }

    @Test
    fun `VERSION header constant is correct`() {
        assertEquals("X-Inertia-Version", InertiaHeaders.VERSION)
    }

    @Test
    fun `PARTIAL_COMPONENT header constant is correct`() {
        assertEquals("X-Inertia-Partial-Component", InertiaHeaders.PARTIAL_COMPONENT)
    }

    @Test
    fun `PARTIAL_DATA header constant is correct`() {
        assertEquals("X-Inertia-Partial-Data", InertiaHeaders.PARTIAL_DATA)
    }

    @Test
    fun `PARTIAL_EXCEPT header constant is correct`() {
        assertEquals("X-Inertia-Partial-Except", InertiaHeaders.PARTIAL_EXCEPT)
    }

    @Test
    fun `RESET header constant is correct`() {
        assertEquals("X-Inertia-Reset", InertiaHeaders.RESET)
    }

    @Test
    fun `INFINITE_SCROLL_MERGE_INTENT header constant is correct`() {
        assertEquals("X-Inertia-Infinite-Scroll-Merge-Intent", InertiaHeaders.INFINITE_SCROLL_MERGE_INTENT)
    }

    @Test
    fun `EXCEPT_ONCE_PROPS header constant is correct`() {
        assertEquals("X-Inertia-Except-Once-Props", InertiaHeaders.EXCEPT_ONCE_PROPS)
    }

    @Test
    fun `ERROR_BAG header constant is correct`() {
        assertEquals("X-Inertia-Error-Bag", InertiaHeaders.ERROR_BAG)
    }

    @Test
    fun `LOCATION header constant is correct`() {
        assertEquals("X-Inertia-Location", InertiaHeaders.LOCATION)
    }

    @Test
    fun `VARY header constant is correct`() {
        assertEquals("Vary", InertiaHeaders.VARY)
    }
}
