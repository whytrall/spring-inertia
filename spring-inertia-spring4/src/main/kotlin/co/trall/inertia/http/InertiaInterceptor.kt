package co.trall.inertia.http

import co.trall.inertia.config.InertiaProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

/**
 * HTTP interceptor that handles Inertia-specific request/response processing.
 *
 * This interceptor:
 * - Detects version mismatches and triggers full page reloads
 * - Converts 302 redirects to 303 for PUT/PATCH/DELETE requests
 * - Adds the Vary header for proper caching
 * - Sets up the request context for Inertia handling
 *
 * Extend this class to customize shared props and versioning:
 * ```kotlin
 * @Component
 * class AppInertiaInterceptor(
 *     properties: InertiaProperties
 * ) : InertiaInterceptor(properties) {
 *
 *     override fun share(request: HttpServletRequest): Map<String, Any?> {
 *         return mapOf(
 *             "auth" to mapOf("user" to getCurrentUser()),
 *             "errors" to getValidationErrors(request)
 *         )
 *     }
 *
 *     override fun version(request: HttpServletRequest): String {
 *         return manifestHash ?: ""
 *     }
 * }
 * ```
 */
open class InertiaInterceptor(
    private val properties: InertiaProperties
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val context = InertiaRequestContext.from(request)

        // Store request-scoped shared props from interceptor
        val sharedProps = share(request)
        if (sharedProps.isNotEmpty()) {
            request.setAttribute(SHARED_PROPS_ATTRIBUTE, sharedProps)
        }

        // Check version mismatch for Inertia requests
        if (context.isInertiaRequest) {
            val clientVersion = context.version
            val serverVersion = version(request)

            if (clientVersion != null && serverVersion != null && clientVersion != serverVersion) {
                // Version mismatch - force full page reload.
                // Per Inertia.js protocol, we return HTTP 409 Conflict with X-Inertia-Location header.
                // The client will perform a full page visit to the specified URL.
                // See: https://inertiajs.com/the-protocol#asset-versioning
                response.status = HttpStatus.CONFLICT.value()
                response.setHeader(InertiaHeaders.LOCATION, context.url)
                return false
            }
        }

        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        val context = InertiaRequestContext.from(request)

        // Add Vary header for proper caching
        response.addHeader(InertiaHeaders.VARY, InertiaHeaders.INERTIA)

        // Convert 302 to 303 for PUT/PATCH/DELETE to ensure browser uses GET
        if (context.isInertiaRequest && response.status == HttpStatus.FOUND.value()) {
            val method = context.method.uppercase()
            if (method in listOf("PUT", "PATCH", "DELETE")) {
                response.status = HttpStatus.SEE_OTHER.value()
            }
        }
    }

    /**
     * Override this method to provide shared props for every Inertia response.
     * These props will be merged with the props from individual responses.
     *
     * @param request The current HTTP request.
     * @return A map of shared props.
     */
    open fun share(request: HttpServletRequest): Map<String, Any?> {
        return emptyMap()
    }

    /**
     * Override this method to provide the asset version.
     * When the version changes, Inertia clients will do a full page reload.
     *
     * @param request The current HTTP request.
     * @return The asset version string, or null if versioning is disabled.
     */
    open fun version(request: HttpServletRequest): String? {
        return properties.version
    }

    /**
     * Override this method to provide the root view template name.
     *
     * @param request The current HTTP request.
     * @return The root view template name.
     */
    open fun rootView(request: HttpServletRequest): String {
        return properties.rootView
    }

    companion object {
        /**
         * Request attribute key for storing interceptor-provided shared props.
         */
        const val SHARED_PROPS_ATTRIBUTE = "co.trall.inertia.sharedProps"

        /**
         * Retrieves the shared props stored by the interceptor for the given request.
         */
        @Suppress("UNCHECKED_CAST")
        fun getSharedProps(request: HttpServletRequest): Map<String, Any?> {
            return request.getAttribute(SHARED_PROPS_ATTRIBUTE) as? Map<String, Any?> ?: emptyMap()
        }
    }
}
