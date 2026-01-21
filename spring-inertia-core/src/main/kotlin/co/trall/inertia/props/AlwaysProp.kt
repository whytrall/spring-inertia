package co.trall.inertia.props

/**
 * A prop that is always included in responses, even during partial reloads.
 *
 * Use this for props that should never be skipped, regardless of what
 * partial data is requested.
 *
 * Example usage:
 * ```kotlin
 * inertia.render("Users/Index",
 *     "users" to users,
 *     "filters" to inertia.always { getCurrentFilters() },
 *     "auth" to inertia.always { getCurrentUser() }
 * )
 * ```
 *
 * @param callback The callback that produces the prop value when resolved.
 */
class AlwaysProp(
    private val callback: () -> Any?
) : InertiaProp, AlwaysInclude {

    override fun resolve(): Any? = callback()
}
