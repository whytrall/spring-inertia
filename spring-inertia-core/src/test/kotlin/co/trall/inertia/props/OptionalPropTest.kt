package co.trall.inertia.props

import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OptionalPropTest {

    @Test
    fun `optional prop implements IgnoreFirstLoad`() {
        val prop = OptionalProp { "value" }
        assertTrue(prop is IgnoreFirstLoad)
    }

    @Test
    fun `optional prop implements Onceable`() {
        val prop = OptionalProp { "value" }
        assertTrue(prop is Onceable)
    }

    @Test
    fun `optional prop resolves callback value`() {
        val prop = OptionalProp { "test value" }
        assertEquals("test value", prop.resolve())
    }

    @Test
    fun `optional prop is not once by default`() {
        val prop = OptionalProp { "value" }
        assertFalse(prop.isOnce())
    }

    @Test
    fun `optional prop can be marked as once`() {
        val prop = OptionalProp { "value" }.once()
        assertTrue(prop.isOnce())
    }

    @Test
    fun `optional prop has no expiration by default`() {
        val prop = OptionalProp { "value" }
        assertNull(prop.expiresAt())
    }

    @Test
    fun `optional prop can have expiration set with Instant`() {
        val expiration = Instant.now().plusSeconds(3600)
        val prop = OptionalProp { "value" }.until(expiration)
        assertEquals(expiration, prop.expiresAt())
    }

    @Test
    fun `optional prop can have expiration set with Duration`() {
        val before = Instant.now()
        val prop = OptionalProp { "value" }.until(Duration.ofHours(1))
        val after = Instant.now()

        val expiration = prop.expiresAt()
        assertNotNull(expiration)
        assertTrue(expiration.isAfter(before.plusSeconds(3599)))
        assertTrue(expiration.isBefore(after.plusSeconds(3601)))
    }

    @Test
    fun `optional prop has no custom key by default`() {
        val prop = OptionalProp { "value" }
        assertNull(prop.key())
    }

    @Test
    fun `optional prop can have custom key`() {
        val prop = OptionalProp { "value" }.`as`("customKey")
        assertEquals("customKey", prop.key())
    }

    @Test
    fun `optional prop is not fresh by default`() {
        val prop = OptionalProp { "value" }
        assertFalse(prop.isFresh())
    }

    @Test
    fun `optional prop can be marked as fresh`() {
        val prop = OptionalProp { "value" }.fresh()
        assertTrue(prop.isFresh())
    }

    @Test
    fun `optional prop fluent API allows chaining`() {
        val expiration = Instant.now().plusSeconds(3600)
        val prop = OptionalProp { "value" }
            .once()
            .until(expiration)
            .`as`("myKey")
            .fresh()

        assertTrue(prop.isOnce())
        assertEquals(expiration, prop.expiresAt())
        assertEquals("myKey", prop.key())
        assertTrue(prop.isFresh())
    }
}
