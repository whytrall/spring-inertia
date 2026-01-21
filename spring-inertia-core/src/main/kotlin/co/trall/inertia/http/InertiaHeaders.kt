package co.trall.inertia.http

/**
 * Constants for Inertia HTTP headers used in request/response communication.
 */
object InertiaHeaders {
    // Request headers
    const val INERTIA = "X-Inertia"
    const val VERSION = "X-Inertia-Version"
    const val PARTIAL_COMPONENT = "X-Inertia-Partial-Component"
    const val PARTIAL_DATA = "X-Inertia-Partial-Data"
    const val PARTIAL_EXCEPT = "X-Inertia-Partial-Except"
    const val RESET = "X-Inertia-Reset"
    const val INFINITE_SCROLL_MERGE_INTENT = "X-Inertia-Infinite-Scroll-Merge-Intent"
    const val EXCEPT_ONCE_PROPS = "X-Inertia-Except-Once-Props"
    const val ERROR_BAG = "X-Inertia-Error-Bag"

    // Response headers
    const val LOCATION = "X-Inertia-Location"
    const val VARY = "Vary"
}

/**
 * Merge intent for infinite scroll requests.
 */
enum class MergeIntent {
    APPEND,
    PREPEND
}
