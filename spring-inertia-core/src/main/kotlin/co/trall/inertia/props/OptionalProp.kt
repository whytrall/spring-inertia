package co.trall.inertia.props

import java.time.Duration
import java.time.Instant

/**
 * An optional prop that is excluded from the initial page load.
 *
 * This is the modern replacement for LazyProp with additional support for
 * "once" behavior (caching on the client with optional TTL).
 *
 * Example usage:
 * ```kotlin
 * // Basic optional prop
 * inertia.optional { expensiveCalculation() }
 *
 * // Optional prop with once behavior
 * inertia.optional { config }
 *     .once()
 *     .until(Duration.ofHours(1))
 * ```
 *
 * @param callback The callback that produces the prop value when resolved.
 */
class OptionalProp(
    private val callback: () -> Any?
) : InertiaProp, IgnoreFirstLoad, Onceable {

    private var onceEnabled: Boolean = false
    private var expiration: Instant? = null
    private var ttlDuration: Duration? = null
    private var customKey: String? = null
    private var forceFresh: Boolean = false

    /**
     * Marks this prop to be resolved once and cached client-side.
     */
    fun once(): OptionalProp {
        this.onceEnabled = true
        return this
    }

    /**
     * Sets when this prop should expire and be re-fetched.
     */
    fun until(expiration: Instant): OptionalProp {
        this.expiration = expiration
        this.ttlDuration = null
        return this
    }

    /**
     * Sets a TTL duration after which this prop should expire.
     * The expiration is calculated at resolution time, not at configuration time.
     */
    fun until(duration: Duration): OptionalProp {
        this.ttlDuration = duration
        this.expiration = null
        return this
    }

    /**
     * Sets a custom key for this prop in the client cache.
     */
    fun `as`(key: String): OptionalProp {
        this.customKey = key
        return this
    }

    /**
     * Forces this prop to be re-resolved even if cached.
     */
    fun fresh(): OptionalProp {
        this.forceFresh = true
        return this
    }

    /**
     * Whether this prop has once behavior enabled.
     */
    fun isOnce(): Boolean = onceEnabled

    override fun resolve(): Any? = callback()

    override fun expiresAt(): Instant? = ttlDuration?.let { Instant.now().plus(it) } ?: expiration

    override fun key(): String? = customKey

    override fun isFresh(): Boolean = forceFresh
}
