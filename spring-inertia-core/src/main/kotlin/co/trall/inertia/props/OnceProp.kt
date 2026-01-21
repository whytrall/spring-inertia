package co.trall.inertia.props

import java.time.Duration
import java.time.Instant

/**
 * A prop that is resolved once and cached client-side.
 *
 * Once props are useful for data that doesn't change often and should be
 * cached across navigations. The client will store the value and not request
 * it again until the cache expires or a fresh value is explicitly requested.
 *
 * Example usage:
 * ```kotlin
 * // Cache indefinitely until page reload
 * inertia.once { loadAppConfig() }
 *
 * // Cache for 1 hour
 * inertia.once { loadUserPreferences() }
 *     .until(Duration.ofHours(1))
 *
 * // Force refresh even if cached
 * inertia.once { loadNotifications() }
 *     .fresh()
 * ```
 *
 * @param callback The callback that produces the prop value when resolved.
 */
class OnceProp(
    private val callback: () -> Any?
) : InertiaProp, Onceable {

    private var expiration: Instant? = null
    private var ttlDuration: Duration? = null
    private var customKey: String? = null
    private var forceFresh: Boolean = false

    /**
     * Sets when this prop should expire and be re-fetched.
     */
    fun until(expiration: Instant): OnceProp {
        this.expiration = expiration
        this.ttlDuration = null
        return this
    }

    /**
     * Sets a TTL duration after which this prop should expire.
     * The expiration is calculated at resolution time, not at configuration time.
     */
    fun until(duration: Duration): OnceProp {
        this.ttlDuration = duration
        this.expiration = null
        return this
    }

    /**
     * Sets a custom key for this prop in the client cache.
     */
    fun `as`(key: String): OnceProp {
        this.customKey = key
        return this
    }

    /**
     * Forces this prop to be re-resolved even if cached.
     */
    fun fresh(): OnceProp {
        this.forceFresh = true
        return this
    }

    override fun resolve(): Any? = callback()

    override fun expiresAt(): Instant? = ttlDuration?.let { Instant.now().plus(it) } ?: expiration

    override fun key(): String? = customKey

    override fun isFresh(): Boolean = forceFresh
}
