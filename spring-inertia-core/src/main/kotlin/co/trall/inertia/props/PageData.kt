package co.trall.inertia.props

/**
 * Interface representing paginated data.
 *
 * This abstraction allows ScrollProp to work with any pagination implementation,
 * not just Spring Data's Page. Spring Data Page can be converted to PageData
 * using the extension function in the spring4 module.
 *
 * @param T The type of elements in the page.
 */
interface PageData<out T> {
    /**
     * The content of this page as a list.
     */
    val content: List<T>

    /**
     * The number of the current page (zero-based).
     */
    val number: Int

    /**
     * The total number of pages.
     */
    val totalPages: Int

    /**
     * The total number of elements across all pages.
     */
    val totalElements: Long

    /**
     * Whether there is a next page.
     */
    val hasNext: Boolean

    /**
     * Whether there is a previous page.
     */
    val hasPrevious: Boolean

    /**
     * Whether this is the first page.
     */
    val isFirst: Boolean

    /**
     * Whether this is the last page.
     */
    val isLast: Boolean
}

/**
 * Simple implementation of PageData for manual construction.
 */
data class SimplePageData<out T>(
    override val content: List<T>,
    override val number: Int,
    override val totalPages: Int,
    override val totalElements: Long,
    override val hasNext: Boolean,
    override val hasPrevious: Boolean,
    override val isFirst: Boolean,
    override val isLast: Boolean
) : PageData<T> {

    companion object {
        /**
         * Creates a PageData from a list with pagination info.
         */
        fun <T> of(
            content: List<T>,
            pageNumber: Int,
            pageSize: Int,
            totalElements: Long
        ): PageData<T> {
            val totalPages = if (pageSize > 0) ((totalElements + pageSize - 1) / pageSize).toInt() else 0
            return SimplePageData(
                content = content,
                number = pageNumber,
                totalPages = totalPages,
                totalElements = totalElements,
                hasNext = pageNumber < totalPages - 1,
                hasPrevious = pageNumber > 0,
                isFirst = pageNumber == 0,
                isLast = pageNumber >= totalPages - 1
            )
        }

        /**
         * Creates a single-page PageData containing all items.
         */
        fun <T> single(content: List<T>): PageData<T> {
            return SimplePageData(
                content = content,
                number = 0,
                totalPages = 1,
                totalElements = content.size.toLong(),
                hasNext = false,
                hasPrevious = false,
                isFirst = true,
                isLast = true
            )
        }
    }
}
