package co.trall.inertia.props

import java.time.Instant

/**
 * Base interface for all special Inertia prop types.
 * Props implementing this interface have special resolution behavior.
 */
interface InertiaProp {
    /**
     * Resolves the prop value.
     * Called when the prop needs to be included in the response.
     */
    fun resolve(): Any?
}

/**
 * Marker interface for props that should be excluded from the first page load.
 * These props will only be resolved when explicitly requested via partial reloads.
 */
interface IgnoreFirstLoad

/**
 * Marker interface for props that should always be included in responses,
 * even during partial reloads when they weren't explicitly requested.
 */
interface AlwaysInclude

/**
 * Interface for props that can be deferred.
 * Deferred props are excluded from the initial response and loaded by the
 * frontend after the page renders.
 */
interface Deferrable {
    /**
     * The group name for this deferred prop.
     * Props with the same group are loaded together.
     */
    fun group(): String
}

/**
 * Merge mode for props that can be merged client-side.
 */
enum class MergeMode {
    /**
     * Append to the existing array/list.
     */
    APPEND,

    /**
     * Prepend to the existing array/list.
     */
    PREPEND,

    /**
     * Deep merge nested objects.
     */
    DEEP
}

/**
 * Interface for props that can be merged with existing client-side data.
 */
interface Mergeable {
    /**
     * The merge mode for this prop.
     */
    fun mergeMode(): MergeMode
}

/**
 * Interface for props that should only be resolved once and cached client-side.
 */
interface Onceable {
    /**
     * When this prop expires and should be re-fetched.
     * Null means the prop never expires (cached until page reload).
     */
    fun expiresAt(): Instant?

    /**
     * Custom key for this prop in the cache.
     * Null means use the prop name as the key.
     */
    fun key(): String?

    /**
     * Whether to force re-resolution even if cached.
     */
    fun isFresh(): Boolean
}
