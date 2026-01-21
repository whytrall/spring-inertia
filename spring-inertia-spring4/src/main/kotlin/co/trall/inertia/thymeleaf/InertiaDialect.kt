package co.trall.inertia.thymeleaf

import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.processor.IProcessor

/**
 * Thymeleaf dialect for Inertia.js integration.
 *
 * Provides two custom elements:
 * - `<inertia:page />` - Renders the root div with page data
 * - `<inertia:head />` - Renders SSR head content (if available)
 *
 * Example usage in templates:
 * ```html
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org"
 *       xmlns:inertia="http://trall.co/inertia">
 * <head>
 *     <inertia:head />
 * </head>
 * <body>
 *     <inertia:page />
 *     <script src="/js/app.js"></script>
 * </body>
 * </html>
 * ```
 */
class InertiaDialect : AbstractProcessorDialect(
    "Inertia",
    "inertia",
    1000
) {
    override fun getProcessors(dialectPrefix: String): Set<IProcessor> {
        return setOf(
            InertiaPageElementProcessor(dialectPrefix),
            InertiaHeadElementProcessor(dialectPrefix)
        )
    }
}
