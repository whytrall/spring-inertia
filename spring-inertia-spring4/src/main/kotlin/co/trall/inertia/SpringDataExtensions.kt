package co.trall.inertia

import co.trall.inertia.props.PageData
import org.springframework.data.domain.Page

/**
 * Extension function to convert Spring Data Page to PageData.
 *
 * This allows seamless use of Spring Data's Page with the core ScrollProp.
 *
 * Example:
 * ```kotlin
 * val page: Page<User> = userRepository.findAll(pageable)
 * val pageData: PageData<User> = page.toPageData()
 * ```
 */
fun <T : Any> Page<T>.toPageData(): PageData<T> = SpringDataPageAdapter(this)

/**
 * Adapter that wraps Spring Data Page to implement PageData.
 */
private class SpringDataPageAdapter<T : Any>(
    private val page: Page<T>
) : PageData<T> {
    override val content: List<T> get() = page.content
    override val number: Int get() = page.number
    override val totalPages: Int get() = page.totalPages
    override val totalElements: Long get() = page.totalElements
    override val hasNext: Boolean get() = page.hasNext()
    override val hasPrevious: Boolean get() = page.hasPrevious()
    override val isFirst: Boolean get() = page.isFirst
    override val isLast: Boolean get() = page.isLast
}
