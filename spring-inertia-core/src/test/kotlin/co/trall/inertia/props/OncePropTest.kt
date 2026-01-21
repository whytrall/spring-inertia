package co.trall.inertia.props

import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OncePropTest {

    @Test
    fun `once prop implements Onceable`() {
        val prop = OnceProp { "value" }
        assertTrue(prop is Onceable)
    }

    @Test
    fun `once prop implements InertiaProp`() {
        val prop = OnceProp { "value" }
        assertTrue(prop is InertiaProp)
    }

    @Test
    fun `once prop does not implement IgnoreFirstLoad`() {
        val prop = OnceProp { "value" }
        assertFalse(prop is IgnoreFirstLoad)
    }

    @Test
    fun `once prop resolves callback value`() {
        val prop = OnceProp { "cached value" }
        assertEquals("cached value", prop.resolve())
    }

    @Test
    fun `once prop has no expiration by default`() {
        val prop = OnceProp { "value" }
        assertNull(prop.expiresAt())
    }

    @Test
    fun `once prop can have expiration set with Instant`() {
        val expiration = Instant.now().plusSeconds(3600)
        val prop = OnceProp { "value" }.until(expiration)
        assertEquals(expiration, prop.expiresAt())
    }

    @Test
    fun `once prop can have expiration set with Duration`() {
        val before = Instant.now()
        val prop = OnceProp { "value" }.until(Duration.ofHours(2))
        val after = Instant.now()

        val expiration = prop.expiresAt()
        assertNotNull(expiration)
        assertTrue(expiration.isAfter(before.plusSeconds(7199)))
        assertTrue(expiration.isBefore(after.plusSeconds(7201)))
    }

    @Test
    fun `once prop has no custom key by default`() {
        val prop = OnceProp { "value" }
        assertNull(prop.key())
    }

    @Test
    fun `once prop can have custom key`() {
        val prop = OnceProp { "value" }.`as`("config")
        assertEquals("config", prop.key())
    }

    @Test
    fun `once prop is not fresh by default`() {
        val prop = OnceProp { "value" }
        assertFalse(prop.isFresh())
    }

    @Test
    fun `once prop can be marked as fresh`() {
        val prop = OnceProp { "value" }.fresh()
        assertTrue(prop.isFresh())
    }

    @Test
    fun `once prop fluent API allows chaining`() {
        val expiration = Instant.now().plusSeconds(1800)
        val prop = OnceProp { mapOf("setting" to "value") }
            .until(expiration)
            .`as`("appConfig")
            .fresh()

        assertEquals(expiration, prop.expiresAt())
        assertEquals("appConfig", prop.key())
        assertTrue(prop.isFresh())
    }

    @Test
    fun `once prop callback is invoked on each resolve`() {
        var counter = 0
        val prop = OnceProp { ++counter }

        // Note: caching is handled by the client, not the prop itself
        assertEquals(1, prop.resolve())
        assertEquals(2, prop.resolve())
    }

    @Test
    fun `duration-based expiration is calculated at resolution time not configuration time`() {
        val prop = OnceProp { "value" }.until(Duration.ofHours(1))

        // Get expiration time twice with a small delay
        val expiration1 = prop.expiresAt()
        Thread.sleep(50)
        val expiration2 = prop.expiresAt()

        // Expirations should be different because they're calculated at call time
        assertNotNull(expiration1)
        assertNotNull(expiration2)
        assertTrue(expiration2!!.isAfter(expiration1),
            "Duration-based expiration should be calculated at resolution time")
    }

    @Test
    fun `instant-based expiration returns same value each time`() {
        val fixedExpiration = Instant.now().plusSeconds(3600)
        val prop = OnceProp { "value" }.until(fixedExpiration)

        val expiration1 = prop.expiresAt()
        Thread.sleep(50)
        val expiration2 = prop.expiresAt()

        // Instant-based expiration should return the same value
        assertEquals(expiration1, expiration2)
        assertEquals(fixedExpiration, expiration1)
    }

    @Test
    fun `switching from duration to instant clears duration`() {
        val fixedExpiration = Instant.now().plusSeconds(3600)
        val prop = OnceProp { "value" }
            .until(Duration.ofHours(2))  // Set duration first
            .until(fixedExpiration)       // Then override with instant

        assertEquals(fixedExpiration, prop.expiresAt())
    }

    @Test
    fun `switching from instant to duration clears instant`() {
        val prop = OnceProp { "value" }
            .until(Instant.now().plusSeconds(100))  // Set instant first
            .until(Duration.ofHours(1))              // Then override with duration

        val before = Instant.now()
        val expiration = prop.expiresAt()

        assertNotNull(expiration)
        // Should be approximately 1 hour from now, not 100 seconds
        assertTrue(expiration!!.isAfter(before.plusSeconds(3500)))
    }
}
