package co.trall.inertia.props

import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeferPropTest {

    @Test
    fun `defer prop implements IgnoreFirstLoad`() {
        val prop = DeferProp({ "value" })
        assertTrue(prop is IgnoreFirstLoad)
    }

    @Test
    fun `defer prop implements Deferrable`() {
        val prop = DeferProp({ "value" })
        assertTrue(prop is Deferrable)
    }

    @Test
    fun `defer prop implements Onceable`() {
        val prop = DeferProp({ "value" })
        assertTrue(prop is Onceable)
    }

    @Test
    fun `defer prop implements Mergeable`() {
        val prop = DeferProp({ "value" })
        assertTrue(prop is Mergeable)
    }

    @Test
    fun `defer prop resolves callback value`() {
        val prop = DeferProp({ "test value" })
        assertEquals("test value", prop.resolve())
    }

    @Test
    fun `defer prop has default group`() {
        val prop = DeferProp({ "value" })
        assertEquals("default", prop.group())
    }

    @Test
    fun `defer prop can have custom group`() {
        val prop = DeferProp({ "value" }, "analytics")
        assertEquals("analytics", prop.group())
    }

    @Test
    fun `defer prop is not once by default`() {
        val prop = DeferProp({ "value" })
        assertFalse(prop.isOnce())
    }

    @Test
    fun `defer prop can be marked as once`() {
        val prop = DeferProp({ "value" }).once()
        assertTrue(prop.isOnce())
    }

    @Test
    fun `defer prop is not merge by default`() {
        val prop = DeferProp({ "value" })
        assertFalse(prop.isMerge())
    }

    @Test
    fun `defer prop can be marked as merge (append)`() {
        val prop = DeferProp({ "value" }).merge()
        assertTrue(prop.isMerge())
        assertEquals(MergeMode.APPEND, prop.mergeMode())
    }

    @Test
    fun `defer prop can be marked as prepend`() {
        val prop = DeferProp({ "value" }).prepend()
        assertTrue(prop.isMerge())
        assertEquals(MergeMode.PREPEND, prop.mergeMode())
    }

    @Test
    fun `defer prop can be marked as deep merge`() {
        val prop = DeferProp({ "value" }).deepMerge()
        assertTrue(prop.isMerge())
        assertEquals(MergeMode.DEEP, prop.mergeMode())
    }

    @Test
    fun `defer prop can have expiration`() {
        val expiration = Instant.now().plusSeconds(3600)
        val prop = DeferProp({ "value" }).until(expiration)
        assertEquals(expiration, prop.expiresAt())
    }

    @Test
    fun `defer prop can have custom key`() {
        val prop = DeferProp({ "value" }).`as`("customKey")
        assertEquals("customKey", prop.key())
    }

    @Test
    fun `defer prop can be marked as fresh`() {
        val prop = DeferProp({ "value" }).fresh()
        assertTrue(prop.isFresh())
    }

    @Test
    fun `defer prop fluent API allows chaining`() {
        val prop = DeferProp({ "value" }, "stats")
            .once()
            .until(Duration.ofHours(1))
            .merge()
            .fresh()

        assertEquals("stats", prop.group())
        assertTrue(prop.isOnce())
        assertTrue(prop.isFresh())
        assertTrue(prop.isMerge())
    }
}
