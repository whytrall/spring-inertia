package co.trall.inertia.http

import jakarta.servlet.http.HttpServletRequest

/**
 * Holds the Inertia-specific context for the current request.
 *
 * This class parses and provides access to all Inertia request headers
 * and determines the type of request (initial load, partial reload, etc.).
 */
class InertiaRequestContext internal constructor(
    internal val request: HttpServletRequest
) {
    /**
     * Whether this is an Inertia request (has X-Inertia header).
     */
    val isInertiaRequest: Boolean
        get() = request.getHeader(InertiaHeaders.INERTIA) == "true"

    /**
     * The asset version sent by the client.
     */
    val version: String?
        get() = request.getHeader(InertiaHeaders.VERSION)

    /**
     * The component name for partial reload requests.
     */
    val partialComponent: String?
        get() = request.getHeader(InertiaHeaders.PARTIAL_COMPONENT)

    /**
     * Props to include in partial reload (only these props).
     * Parsed from comma-separated header value.
     */
    val partialData: Set<String>
        get() = parseHeaderList(InertiaHeaders.PARTIAL_DATA)

    /**
     * Props to exclude from partial reload.
     * Parsed from comma-separated header value.
     */
    val partialExcept: Set<String>
        get() = parseHeaderList(InertiaHeaders.PARTIAL_EXCEPT)

    /**
     * Props to reset (re-resolve even if cached).
     * Parsed from comma-separated header value.
     */
    val reset: Set<String>
        get() = parseHeaderList(InertiaHeaders.RESET)

    /**
     * Once props to exclude (already cached on client).
     * Parsed from comma-separated header value.
     */
    val exceptOnceProps: Set<String>
        get() = parseHeaderList(InertiaHeaders.EXCEPT_ONCE_PROPS)

    /**
     * The merge intent for infinite scroll (append or prepend).
     */
    val mergeIntent: MergeIntent?
        get() = when (request.getHeader(InertiaHeaders.INFINITE_SCROLL_MERGE_INTENT)?.lowercase()) {
            "append" -> MergeIntent.APPEND
            "prepend" -> MergeIntent.PREPEND
            else -> null
        }

    /**
     * The error bag name for validation errors.
     */
    val errorBag: String?
        get() = request.getHeader(InertiaHeaders.ERROR_BAG)

    /**
     * Whether this is a partial reload request.
     */
    val isPartialReload: Boolean
        get() = isInertiaRequest && (partialData.isNotEmpty() || partialExcept.isNotEmpty())

    /**
     * The full URL of the current request including query string.
     */
    val url: String
        get() {
            val uri = request.requestURI
            val query = request.queryString
            return if (query.isNullOrEmpty()) uri else "$uri?$query"
        }

    /**
     * The HTTP method of the request.
     */
    val method: String
        get() = request.method

    private fun parseHeaderList(headerName: String): Set<String> {
        val value = request.getHeader(headerName) ?: return emptySet()
        return value.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    companion object {
        private val REQUEST_ATTRIBUTE = InertiaRequestContext::class.java.name

        /**
         * Gets or creates the InertiaRequestContext for the current request.
         */
        fun from(request: HttpServletRequest): InertiaRequestContext {
            val existing = request.getAttribute(REQUEST_ATTRIBUTE) as? InertiaRequestContext
            if (existing != null) return existing

            val context = InertiaRequestContext(request)
            request.setAttribute(REQUEST_ATTRIBUTE, context)
            return context
        }
    }
}
