package co.trall.inertia.props

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LazyPropTest {

    @Test
    fun `lazy prop implements IgnoreFirstLoad`() {
        val prop = LazyProp { "value" }
        assertTrue(prop is IgnoreFirstLoad)
    }

    @Test
    fun `lazy prop implements InertiaProp`() {
        val prop = LazyProp { "value" }
        assertTrue(prop is InertiaProp)
    }

    @Test
    fun `lazy prop does not implement AlwaysInclude`() {
        val prop = LazyProp { "value" }
        assertFalse(prop is AlwaysInclude)
    }

    @Test
    fun `lazy prop resolves callback value`() {
        val prop = LazyProp { "test value" }
        assertEquals("test value", prop.resolve())
    }

    @Test
    fun `lazy prop callback is invoked on each resolve`() {
        var counter = 0
        val prop = LazyProp { ++counter }

        assertEquals(1, prop.resolve())
        assertEquals(2, prop.resolve())
        assertEquals(3, prop.resolve())
    }

    @Test
    fun `lazy prop can return null`() {
        val prop = LazyProp { null }
        assertEquals(null, prop.resolve())
    }

    @Test
    fun `lazy prop can return complex objects`() {
        val data = mapOf("key" to "value", "number" to 42)
        val prop = LazyProp { data }
        assertEquals(data, prop.resolve())
    }
}
