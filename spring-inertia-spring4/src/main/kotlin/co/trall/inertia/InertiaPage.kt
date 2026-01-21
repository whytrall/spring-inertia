package co.trall.inertia

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Represents the page data structure sent to the Inertia.js client.
 *
 * This is the JSON payload that gets embedded in the HTML page (for initial loads)
 * or returned directly (for subsequent Inertia requests).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class InertiaPage(
    /**
     * The name of the frontend component to render.
     * Example: "Users/Index", "Dashboard", "Auth/Login"
     */
    val component: String,

    /**
     * The resolved props to pass to the component.
     */
    val props: Map<String, Any?>,

    /**
     * The current page URL (including query string).
     */
    val url: String,

    /**
     * The asset version for cache invalidation.
     */
    val version: String?,

    /**
     * Whether to clear the browser history state.
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val clearHistory: Boolean = false,

    /**
     * Whether to encrypt the browser history state.
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val encryptHistory: Boolean = false,

    /**
     * Metadata about deferred props grouped by group name.
     * Maps group name to list of prop names in that group.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val deferredProps: Map<String, List<String>>? = null,

    /**
     * List of prop names that should be merged (appended) client-side.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val mergeProps: List<String>? = null,

    /**
     * List of prop names that should be prepended client-side.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val prependProps: List<String>? = null,

    /**
     * List of prop names that should be deep merged client-side.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val deepMergeProps: List<String>? = null,

    /**
     * Metadata about once props with their expiration times.
     * Maps prop name to expiration info.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val onceProps: Map<String, OncePropsMetadata>? = null,

    /**
     * Flash data (one-time messages).
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val flash: Map<String, Any?>? = null
)

/**
 * Metadata for a once prop including its expiration time.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OncePropsMetadata(
    /**
     * Unix timestamp (milliseconds) when this prop expires.
     * Null means the prop never expires (cached indefinitely until page reload).
     */
    val expiresAt: Long? = null
)
