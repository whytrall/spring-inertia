package co.trall.inertia.ssr

/**
 * Response from the SSR service containing pre-rendered HTML.
 *
 * @property head List of HTML strings to include in the document head
 *                (typically meta tags, title, link tags, etc.)
 * @property body The pre-rendered HTML body to include in the root element.
 */
data class SsrResponse(
    /**
     * HTML strings to include in the document head.
     * Each string is typically a complete HTML tag like:
     * - `<title>Page Title</title>`
     * - `<meta name="description" content="...">`
     * - `<link rel="canonical" href="...">`
     */
    val head: List<String>,

    /**
     * Pre-rendered HTML body content.
     * This is inserted into the root element (typically `<div id="app">`).
     */
    val body: String
)
