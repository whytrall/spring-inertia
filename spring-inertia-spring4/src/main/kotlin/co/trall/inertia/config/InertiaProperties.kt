package co.trall.inertia.config

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Configuration properties for the Inertia library.
 *
 * Example configuration in application.yml:
 * ```yaml
 * inertia:
 *   root-view: app
 *   version: abc123
 *   ssr:
 *     enabled: true
 *     url: http://localhost:13714
 *   history:
 *     encrypt: false
 *   pages:
 *     ensure-exist: true
 *     paths:
 *       - classpath:/templates/pages/
 *     extensions:
 *       - .vue
 *       - .tsx
 * ```
 */
@Validated
@ConfigurationProperties(prefix = "inertia")
data class InertiaProperties(
    /**
     * The root Thymeleaf template name (without extension).
     */
    @field:NotBlank(message = "Root view template name must not be blank")
    val rootView: String = "app",

    /**
     * The asset version for cache busting.
     * When this changes, clients will do a full page reload.
     */
    val version: String? = null,

    /**
     * SSR (Server-Side Rendering) configuration.
     */
    @field:Valid
    val ssr: SsrProperties = SsrProperties(),

    /**
     * Browser history configuration.
     */
    @field:Valid
    val history: HistoryProperties = HistoryProperties(),

    /**
     * Page component validation configuration.
     */
    @field:Valid
    val pages: PagesProperties = PagesProperties()
) {
    /**
     * SSR configuration properties.
     */
    data class SsrProperties(
        /**
         * Whether SSR is enabled.
         */
        val enabled: Boolean = false,

        /**
         * URL of the SSR server (typically a Node.js process).
         */
        @field:NotBlank(message = "SSR URL must not be blank when SSR is enabled")
        @field:Pattern(
            regexp = "^https?://.*",
            message = "SSR URL must be a valid HTTP(S) URL"
        )
        val url: String = "http://localhost:13714",

        /**
         * Whether to check that the SSR bundle exists before attempting to render.
         */
        val ensureBundleExists: Boolean = true,

        /**
         * Path to the SSR bundle file (optional, auto-detected if not specified).
         */
        val bundle: String? = null
    )

    /**
     * History configuration properties.
     */
    data class HistoryProperties(
        /**
         * Whether to encrypt the history state in the browser.
         */
        val encrypt: Boolean = false
    )

    /**
     * Page validation configuration properties.
     */
    data class PagesProperties(
        /**
         * Whether to validate that page components exist at render time.
         */
        val ensureExist: Boolean = false,

        /**
         * Paths to search for page components.
         */
        val paths: List<String> = listOf("classpath:/templates/pages/"),

        /**
         * File extensions to recognize as page components.
         */
        val extensions: List<String> = listOf(".vue", ".tsx", ".jsx", ".svelte")
    )
}
