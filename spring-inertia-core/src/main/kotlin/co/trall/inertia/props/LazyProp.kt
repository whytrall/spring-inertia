package co.trall.inertia.props

/**
 * A lazy prop that is excluded from the initial page load.
 *
 * Lazy props are only resolved when explicitly requested via partial reloads
 * using the X-Inertia-Partial-Data header.
 *
 * Example usage:
 * ```kotlin
 * inertia.render("Users/Show",
 *     "user" to user,
 *     "posts" to inertia.lazy { postRepository.findByUserId(user.id) }
 * )
 * ```
 *
 * @param callback The callback that produces the prop value when resolved.
 */
class LazyProp(
    private val callback: () -> Any?
) : InertiaProp, IgnoreFirstLoad {

    override fun resolve(): Any? = callback()
}
