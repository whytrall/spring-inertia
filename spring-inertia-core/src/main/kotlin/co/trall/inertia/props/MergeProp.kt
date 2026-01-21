package co.trall.inertia.props

import java.time.Duration
import java.time.Instant

/**
 * A prop that is merged with existing client-side data instead of replacing it.
 *
 * Useful for pagination, infinite scroll, and other scenarios where you want
 * to append/prepend data to existing lists.
 *
 * Example usage:
 * ```kotlin
 * // Append new items to existing list
 * inertia.merge { loadMoreItems(page) }
 *
 * // Prepend new items
 * inertia.merge { loadNewerItems() }.prepend()
 *
 * // Deep merge nested objects
 * inertia.merge { loadUpdatedSettings() }.deep()
 * ```
 *
 * @param callback The callback that produces the prop value when resolved.
 */
class MergeProp(
    private val callback: () -> Any?
) : InertiaProp, Mergeable, Onceable {

    private var mode: MergeMode = MergeMode.APPEND
    private var onceEnabled: Boolean = false
    private var expiration: Instant? = null
    private var ttlDuration: Duration? = null
    private var customKey: String? = null
    private var forceFresh: Boolean = false

    /**
     * Sets the merge mode to prepend (add items at the beginning).
     */
    fun prepend(): MergeProp {
        this.mode = MergeMode.PREPEND
        return this
    }

    /**
     * Sets the merge mode to deep merge (recursively merge nested objects).
     */
    fun deep(): MergeProp {
        this.mode = MergeMode.DEEP
        return this
    }

    /**
     * Marks this prop to be resolved once and cached client-side.
     */
    fun once(): MergeProp {
        this.onceEnabled = true
        return this
    }

    /**
     * Sets when this prop should expire and be re-fetched.
     */
    fun until(expiration: Instant): MergeProp {
        this.expiration = expiration
        this.ttlDuration = null
        return this
    }

    /**
     * Sets a TTL duration after which this prop should expire.
     * The expiration is calculated at resolution time, not at configuration time.
     */
    fun until(duration: Duration): MergeProp {
        this.ttlDuration = duration
        this.expiration = null
        return this
    }

    /**
     * Sets a custom key for this prop in the client cache.
     */
    fun `as`(key: String): MergeProp {
        this.customKey = key
        return this
    }

    /**
     * Forces this prop to be re-resolved even if cached.
     */
    fun fresh(): MergeProp {
        this.forceFresh = true
        return this
    }

    /**
     * Whether this prop has once behavior enabled.
     */
    fun isOnce(): Boolean = onceEnabled

    override fun resolve(): Any? = callback()

    override fun mergeMode(): MergeMode = mode

    override fun expiresAt(): Instant? = ttlDuration?.let { Instant.now().plus(it) } ?: expiration

    override fun key(): String? = customKey

    override fun isFresh(): Boolean = forceFresh
}
