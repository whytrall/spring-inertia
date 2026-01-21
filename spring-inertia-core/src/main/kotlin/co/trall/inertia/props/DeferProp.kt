package co.trall.inertia.props

import java.time.Duration
import java.time.Instant

/**
 * A deferred prop that is loaded automatically by the frontend after page render.
 *
 * Unlike lazy props which must be explicitly requested, deferred props are
 * automatically fetched by the Inertia.js client after the initial page loads.
 * This improves Time-to-Interactive (TTI) for pages with expensive data.
 *
 * Deferred props can be grouped together so they're loaded in a single request.
 *
 * Example usage:
 * ```kotlin
 * // Basic deferred prop
 * inertia.defer { calculateStats() }
 *
 * // Deferred prop with custom group
 * inertia.defer("analytics") { loadAnalytics() }
 *
 * // Deferred prop with once behavior
 * inertia.defer { loadConfig() }
 *     .once()
 *     .until(Duration.ofMinutes(30))
 * ```
 *
 * @param callback The callback that produces the prop value when resolved.
 * @param groupName The group name for this deferred prop.
 */
class DeferProp(
    private val callback: () -> Any?,
    private val groupName: String = "default"
) : InertiaProp, IgnoreFirstLoad, Deferrable, Onceable, Mergeable {

    private var onceEnabled: Boolean = false
    private var expiration: Instant? = null
    private var ttlDuration: Duration? = null
    private var customKey: String? = null
    private var forceFresh: Boolean = false
    private var mergeEnabled: Boolean = false
    private var mode: MergeMode = MergeMode.APPEND

    /**
     * Marks this prop to be resolved once and cached client-side.
     */
    fun once(): DeferProp {
        this.onceEnabled = true
        return this
    }

    /**
     * Sets when this prop should expire and be re-fetched.
     */
    fun until(expiration: Instant): DeferProp {
        this.expiration = expiration
        this.ttlDuration = null
        return this
    }

    /**
     * Sets a TTL duration after which this prop should expire.
     * The expiration is calculated at resolution time, not at configuration time.
     */
    fun until(duration: Duration): DeferProp {
        this.ttlDuration = duration
        this.expiration = null
        return this
    }

    /**
     * Sets a custom key for this prop in the client cache.
     */
    fun `as`(key: String): DeferProp {
        this.customKey = key
        return this
    }

    /**
     * Forces this prop to be re-resolved even if cached.
     */
    fun fresh(): DeferProp {
        this.forceFresh = true
        return this
    }

    /**
     * Enables merging (append) for this prop.
     */
    fun merge(): DeferProp {
        this.mergeEnabled = true
        this.mode = MergeMode.APPEND
        return this
    }

    /**
     * Enables prepending for this prop.
     */
    fun prepend(): DeferProp {
        this.mergeEnabled = true
        this.mode = MergeMode.PREPEND
        return this
    }

    /**
     * Enables deep merging for this prop.
     */
    fun deepMerge(): DeferProp {
        this.mergeEnabled = true
        this.mode = MergeMode.DEEP
        return this
    }

    /**
     * Whether this prop has once behavior enabled.
     */
    fun isOnce(): Boolean = onceEnabled

    /**
     * Whether this prop has merge behavior enabled.
     */
    fun isMerge(): Boolean = mergeEnabled

    override fun resolve(): Any? = callback()

    override fun group(): String = groupName

    override fun expiresAt(): Instant? = ttlDuration?.let { Instant.now().plus(it) } ?: expiration

    override fun key(): String? = customKey

    override fun isFresh(): Boolean = forceFresh

    override fun mergeMode(): MergeMode = mode
}
