package co.trall.inertia

import co.trall.inertia.http.InertiaHeaders
import co.trall.inertia.props.*
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * Main facade for the Inertia library.
 *
 * This is the primary entry point for creating Inertia responses and managing
 * shared props. Inject this into your controllers to render Inertia pages.
 *
 * This bean is automatically configured by [co.trall.inertia.config.InertiaAutoConfiguration].
 *
 * Example usage:
 * ```kotlin
 * @Controller
 * class UsersController(private val inertia: Inertia) {
 *
 *     @GetMapping("/users")
 *     fun index(): InertiaResponse {
 *         return inertia.render("Users/Index",
 *             "users" to userRepository.findAll(),
 *             "filters" to inertia.always { getCurrentFilters() }
 *         )
 *     }
 * }
 * ```
 */
class Inertia(
    private val factory: InertiaResponseFactory
) {
    // ==================== Rendering ====================

    /**
     * Creates an Inertia response for the given component.
     *
     * @param component The name of the frontend component to render.
     * @param props The props to pass to the component.
     */
    fun render(component: String, props: Map<String, Any?> = emptyMap()): InertiaResponse {
        return factory.render(component, props)
    }

    /**
     * Creates an Inertia response for the given component with vararg props.
     *
     * @param component The name of the frontend component to render.
     * @param props The props to pass to the component as key-value pairs.
     */
    fun render(component: String, vararg props: Pair<String, Any?>): InertiaResponse {
        return factory.render(component, *props)
    }

    // ==================== Shared Props ====================

    /**
     * Shares a prop that will be included in every Inertia response.
     *
     * @param key The prop name.
     * @param value The prop value (can be a callback for lazy evaluation).
     */
    fun share(key: String, value: Any?) {
        factory.share(key, value)
    }

    /**
     * Shares a callback that will be evaluated on every request.
     *
     * @param key The prop name.
     * @param callback The callback that produces the prop value.
     */
    fun share(key: String, callback: () -> Any?) {
        factory.share(key, callback)
    }

    /**
     * Shares multiple props at once.
     *
     * @param props The props to share.
     */
    fun share(props: Map<String, Any?>) {
        factory.share(props)
    }

    /**
     * Gets all shared props.
     */
    fun getShared(): Map<String, Any?> {
        return factory.getShared()
    }

    // ==================== Flash Data ====================

    /**
     * Sets flash data that will be included in the next response.
     * Flash data is automatically cleared after being sent.
     *
     * @param key The flash key.
     * @param value The flash value.
     */
    fun flash(key: String, value: Any?) {
        factory.flash(key, value)
    }

    // ==================== Prop Helpers ====================

    /**
     * Creates a lazy prop that is excluded from the initial page load.
     * Lazy props are only resolved when explicitly requested via partial reloads.
     *
     * @param callback The callback that produces the prop value.
     */
    fun lazy(callback: () -> Any?): LazyProp {
        return LazyProp(callback)
    }

    /**
     * Creates an optional prop (modern replacement for lazy).
     * Optional props support additional behaviors like once (caching).
     *
     * @param callback The callback that produces the prop value.
     */
    fun optional(callback: () -> Any?): OptionalProp {
        return OptionalProp(callback)
    }

    /**
     * Creates a deferred prop that is automatically loaded by the frontend
     * after the initial page render.
     *
     * @param group The group name for batching deferred prop requests.
     * @param callback The callback that produces the prop value.
     */
    fun defer(group: String = "default", callback: () -> Any?): DeferProp {
        return DeferProp(callback, group)
    }

    /**
     * Creates a merge prop that is merged with existing client-side data
     * instead of replacing it.
     *
     * @param callback The callback that produces the prop value.
     */
    fun merge(callback: () -> Any?): MergeProp {
        return MergeProp(callback)
    }

    /**
     * Creates an always prop that is always included in responses,
     * even during partial reloads.
     *
     * @param callback The callback that produces the prop value.
     */
    fun always(callback: () -> Any?): AlwaysProp {
        return AlwaysProp(callback)
    }

    /**
     * Creates a once prop that is resolved once and cached client-side.
     *
     * @param callback The callback that produces the prop value.
     */
    fun once(callback: () -> Any?): OnceProp {
        return OnceProp(callback)
    }

    /**
     * Creates a scroll prop for infinite scroll / pagination scenarios.
     * Automatically extracts pagination metadata from Spring Data Page.
     *
     * @param page The Spring Data Page containing the data.
     */
    fun <T : Any> scroll(page: Page<T>): ScrollProp<T> {
        return ScrollProp(page.toPageData())
    }

    /**
     * Creates a scroll prop for infinite scroll / pagination scenarios.
     * Works with the core PageData interface directly.
     *
     * @param pageData The PageData containing the data and pagination info.
     */
    fun <T : Any> scroll(pageData: PageData<T>): ScrollProp<T> {
        return ScrollProp(pageData)
    }

    // ==================== Special Responses ====================

    /**
     * Creates an external location response that instructs the Inertia client
     * to perform a full page visit to the given URL.
     *
     * This is useful for redirecting to external URLs or non-Inertia pages.
     *
     * @param url The URL to redirect to.
     */
    fun location(url: String): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT) // 409
            .header(InertiaHeaders.LOCATION, url)
            .build()
    }
}
