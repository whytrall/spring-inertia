package co.trall.inertia.props

import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MergePropTest {

    @Test
    fun `merge prop implements Mergeable`() {
        val prop = MergeProp { "value" }
        assertTrue(prop is Mergeable)
    }

    @Test
    fun `merge prop implements Onceable`() {
        val prop = MergeProp { "value" }
        assertTrue(prop is Onceable)
    }

    @Test
    fun `merge prop does not implement IgnoreFirstLoad`() {
        val prop = MergeProp { "value" }
        assertFalse(prop is IgnoreFirstLoad)
    }

    @Test
    fun `merge prop resolves callback value`() {
        val prop = MergeProp { listOf(1, 2, 3) }
        assertEquals(listOf(1, 2, 3), prop.resolve())
    }

    @Test
    fun `merge prop defaults to APPEND mode`() {
        val prop = MergeProp { "value" }
        assertEquals(MergeMode.APPEND, prop.mergeMode())
    }

    @Test
    fun `merge prop can be set to PREPEND mode`() {
        val prop = MergeProp { "value" }.prepend()
        assertEquals(MergeMode.PREPEND, prop.mergeMode())
    }

    @Test
    fun `merge prop can be set to DEEP mode`() {
        val prop = MergeProp { "value" }.deep()
        assertEquals(MergeMode.DEEP, prop.mergeMode())
    }

    @Test
    fun `merge prop is not once by default`() {
        val prop = MergeProp { "value" }
        assertFalse(prop.isOnce())
    }

    @Test
    fun `merge prop can be marked as once`() {
        val prop = MergeProp { "value" }.once()
        assertTrue(prop.isOnce())
    }

    @Test
    fun `merge prop can have expiration`() {
        val expiration = Instant.now().plusSeconds(3600)
        val prop = MergeProp { "value" }.until(expiration)
        assertEquals(expiration, prop.expiresAt())
    }

    @Test
    fun `merge prop can have expiration with Duration`() {
        val prop = MergeProp { "value" }.until(Duration.ofMinutes(30))
        val expiration = prop.expiresAt()
        assertTrue(expiration != null)
        assertTrue(expiration!!.isAfter(Instant.now()))
    }

    @Test
    fun `merge prop can have custom key`() {
        val prop = MergeProp { "value" }.`as`("customKey")
        assertEquals("customKey", prop.key())
    }

    @Test
    fun `merge prop can be marked as fresh`() {
        val prop = MergeProp { "value" }.fresh()
        assertTrue(prop.isFresh())
    }

    @Test
    fun `merge prop fluent API allows chaining`() {
        val prop = MergeProp { listOf(1, 2, 3) }
            .prepend()
            .once()
            .`as`("items")
            .fresh()

        assertEquals(MergeMode.PREPEND, prop.mergeMode())
        assertTrue(prop.isOnce())
        assertEquals("items", prop.key())
        assertTrue(prop.isFresh())
    }
}
