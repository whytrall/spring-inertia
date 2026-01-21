package co.trall.inertia.props

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlwaysPropTest {

    @Test
    fun `always prop implements AlwaysInclude`() {
        val prop = AlwaysProp { "value" }
        assertTrue(prop is AlwaysInclude)
    }

    @Test
    fun `always prop implements InertiaProp`() {
        val prop = AlwaysProp { "value" }
        assertTrue(prop is InertiaProp)
    }

    @Test
    fun `always prop does not implement IgnoreFirstLoad`() {
        val prop = AlwaysProp { "value" }
        assertFalse(prop is IgnoreFirstLoad)
    }

    @Test
    fun `always prop resolves callback value`() {
        val prop = AlwaysProp { "always included" }
        assertEquals("always included", prop.resolve())
    }

    @Test
    fun `always prop callback is invoked on each resolve`() {
        var counter = 0
        val prop = AlwaysProp { ++counter }

        assertEquals(1, prop.resolve())
        assertEquals(2, prop.resolve())
        assertEquals(3, prop.resolve())
    }

    @Test
    fun `always prop can return null`() {
        val prop = AlwaysProp { null }
        assertEquals(null, prop.resolve())
    }

    @Test
    fun `always prop can return complex objects`() {
        val user = mapOf("id" to 1, "name" to "John")
        val prop = AlwaysProp { user }
        assertEquals(user, prop.resolve())
    }
}
