package co.trall.inertia.ssr

import co.trall.inertia.InertiaPage

/**
 * Gateway interface for Server-Side Rendering (SSR).
 *
 * Implementations of this interface communicate with an SSR service
 * (typically a Node.js process running the frontend framework) to
 * pre-render Inertia pages on the server.
 *
 * To provide a custom SSR gateway, implement this interface and register
 * it as a Spring bean:
 *
 * ```kotlin
 * @Bean
 * fun customSsrGateway(): SsrGateway {
 *     return MyCustomSsrGateway()
 * }
 * ```
 */
interface SsrGateway {

    /**
     * Renders the given page using SSR.
     *
     * @param page The Inertia page to render.
     * @return The SSR response containing head and body HTML, or null if SSR failed.
     */
    fun render(page: InertiaPage): SsrResponse?

    /**
     * Checks if the SSR service is available and healthy.
     *
     * @return true if SSR is available, false otherwise.
     */
    fun isAvailable(): Boolean
}
