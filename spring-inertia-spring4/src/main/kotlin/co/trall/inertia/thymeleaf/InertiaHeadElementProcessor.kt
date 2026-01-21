package co.trall.inertia.thymeleaf

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractElementTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

/**
 * Processes the `<inertia:head />` element.
 *
 * This processor renders the head content from SSR (if available).
 * This typically includes meta tags, title, and other head elements
 * that were rendered on the server.
 *
 * Output (without SSR):
 * ```html
 * <!-- empty - no SSR head content -->
 * ```
 *
 * Output (with SSR):
 * ```html
 * <title>Page Title</title>
 * <meta name="description" content="...">
 * <!-- other head tags from SSR -->
 * ```
 *
 * ## Security Note
 *
 * SSR head content is inserted as raw HTML without escaping. This is intentional
 * since SSR produces pre-rendered HTML that must be inserted verbatim. However,
 * this means you must ensure your SSR service is trusted and not compromised.
 *
 * The SSR service should:
 * - Be running in a trusted environment (localhost or internal network)
 * - Not accept user input that could lead to HTML injection
 * - Properly escape any dynamic content in head tags
 *
 * If the SSR service is compromised, malicious HTML/JavaScript could be injected
 * into the page. Only use SSR with trusted services.
 */
class InertiaHeadElementProcessor(
    dialectPrefix: String
) : AbstractElementTagProcessor(
    TemplateMode.HTML,
    dialectPrefix,
    "head",
    true,
    null,
    false,
    1000
) {
    override fun doProcess(
        context: ITemplateContext,
        tag: IProcessableElementTag,
        structureHandler: IElementTagStructureHandler
    ) {
        // Get SSR head content if available
        val ssrHead = context.getVariable("ssrHead") as? List<*>

        val html = if (ssrHead != null && ssrHead.isNotEmpty()) {
            ssrHead.joinToString("\n") { it.toString() }
        } else {
            "" // No SSR content available
        }

        structureHandler.replaceWith(html, false)
    }
}
