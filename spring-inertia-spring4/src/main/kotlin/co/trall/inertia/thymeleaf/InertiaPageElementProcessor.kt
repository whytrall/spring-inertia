package co.trall.inertia.thymeleaf

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractElementTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode
import org.unbescape.html.HtmlEscape

/**
 * Processes the `<inertia:page />` element.
 *
 * This processor renders the root element for the Inertia.js application.
 * It outputs a `<div>` with:
 * - `id="app"` - The mount point for the frontend framework
 * - `data-page="..."` - The JSON-encoded page data
 *
 * If SSR is enabled and available, the div will also contain the pre-rendered HTML.
 *
 * Output (without SSR):
 * ```html
 * <div id="app" data-page='{"component":"...","props":{...}}'></div>
 * ```
 *
 * Output (with SSR):
 * ```html
 * <div id="app" data-page='{"component":"...","props":{...}}'>
 *     [Pre-rendered HTML from SSR]
 * </div>
 * ```
 */
class InertiaPageElementProcessor(
    dialectPrefix: String
) : AbstractElementTagProcessor(
    TemplateMode.HTML,
    dialectPrefix,
    "page",
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
        // Get the page JSON from the model
        val pageJson = context.getVariable("pageJson") as? String ?: "{}"

        // Get SSR body if available
        val ssrBody = context.getVariable("ssrBody") as? String

        // Build the output
        val escapedJson = HtmlEscape.escapeHtml4Xml(pageJson)

        val html = if (ssrBody != null) {
            """<div id="app" data-page="$escapedJson">$ssrBody</div>"""
        } else {
            """<div id="app" data-page="$escapedJson"></div>"""
        }

        structureHandler.replaceWith(html, false)
    }
}
