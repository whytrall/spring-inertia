package co.trall.inertia.props

/**
 * A specialized merge prop for infinite scroll / pagination scenarios.
 *
 * Automatically extracts pagination metadata from PageData objects
 * and configures appropriate merge behavior based on the scroll direction.
 *
 * Example usage with Spring Data Page (in spring4 module):
 * ```kotlin
 * @GetMapping("/users")
 * fun index(pageable: Pageable): InertiaResponse {
 *     return inertia.render("Users/Index",
 *         "users" to inertia.scroll(userRepository.findAll(pageable))
 *     )
 * }
 * ```
 *
 * Example usage with PageData directly:
 * ```kotlin
 * val pageData = SimplePageData.of(items, pageNumber = 0, pageSize = 10, totalElements = 100)
 * inertia.render("Items/Index", "items" to ScrollProp(pageData))
 * ```
 *
 * @param page The PageData containing the data and pagination info.
 */
class ScrollProp<T : Any>(
    private val page: PageData<T>
) : InertiaProp, Mergeable, Deferrable {

    private var mode: MergeMode = MergeMode.APPEND
    private var groupName: String = "default"
    private var deferEnabled: Boolean = false

    /**
     * Sets the merge mode to prepend (for "load newer" scenarios).
     */
    fun prepend(): ScrollProp<T> {
        this.mode = MergeMode.PREPEND
        return this
    }

    /**
     * Makes this a deferred prop (loaded after initial page render).
     */
    fun defer(group: String = "default"): ScrollProp<T> {
        this.deferEnabled = true
        this.groupName = group
        return this
    }

    /**
     * Whether this prop is deferred.
     */
    fun isDeferred(): Boolean = deferEnabled

    override fun resolve(): Any? {
        return ScrollData(
            items = page.content,
            meta = ScrollMetadata(
                currentPage = page.number,
                totalPages = page.totalPages,
                totalItems = page.totalElements,
                hasNextPage = page.hasNext,
                hasPreviousPage = page.hasPrevious,
                isFirstPage = page.isFirst,
                isLastPage = page.isLast
            )
        )
    }

    override fun mergeMode(): MergeMode = mode

    override fun group(): String = groupName
}

/**
 * Wrapper for scroll data containing items and pagination metadata.
 */
data class ScrollData<T>(
    val items: List<T>,
    val meta: ScrollMetadata
)

/**
 * Pagination metadata for scroll props.
 */
data class ScrollMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Long,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean,
    val isFirstPage: Boolean,
    val isLastPage: Boolean
)
