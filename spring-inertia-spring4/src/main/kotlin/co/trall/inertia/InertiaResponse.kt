package co.trall.inertia

import co.trall.inertia.http.InertiaHeaders
import co.trall.inertia.http.InertiaRequestContext
import co.trall.inertia.props.*
import tools.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.ModelAndView

/**
 * Represents an Inertia response that can be rendered as either HTML (initial load)
 * or JSON (subsequent Inertia requests).
 *
 * Example usage:
 * ```kotlin
 * @GetMapping("/users")
 * fun index(): InertiaResponse {
 *     return inertia.render("Users/Index",
 *         "users" to userRepository.findAll(),
 *         "filters" to filters
 *     )
 * }
 * ```
 */
class InertiaResponse(
    val component: String,
    private val props: MutableMap<String, Any?> = mutableMapOf(),
    private val factory: InertiaResponseFactory
) {
    private var clearHistory: Boolean = false
    private var encryptHistory: Boolean = false

    /**
     * Adds a prop to the response.
     */
    fun with(key: String, value: Any?): InertiaResponse {
        props[key] = value
        return this
    }

    /**
     * Adds multiple props to the response.
     */
    fun with(additionalProps: Map<String, Any?>): InertiaResponse {
        props.putAll(additionalProps)
        return this
    }

    /**
     * Marks that the browser history should be cleared.
     */
    fun clearHistory(): InertiaResponse {
        this.clearHistory = true
        return this
    }

    /**
     * Marks that the browser history should be encrypted.
     */
    fun encryptHistory(): InertiaResponse {
        this.encryptHistory = true
        return this
    }

    /**
     * Converts this response to the appropriate format based on the request type.
     * Returns HTML with embedded JSON for initial loads, or JSON for Inertia requests.
     */
    fun toView(request: HttpServletRequest): Any {
        val context = InertiaRequestContext.from(request)
        val page = buildPage(context)

        return if (context.isInertiaRequest) {
            // Return JSON for Inertia requests
            ResponseEntity.ok()
                .header(InertiaHeaders.INERTIA, "true")
                .header(InertiaHeaders.VARY, InertiaHeaders.INERTIA)
                .body(page)
        } else {
            // Return HTML view for initial loads
            val model = mutableMapOf<String, Any?>(
                "page" to page,
                "pageJson" to factory.objectMapper.writeValueAsString(page)
            )
            ModelAndView(factory.rootView, model)
        }
    }

    /**
     * Builds the InertiaPage object with resolved props and metadata.
     */
    internal fun buildPage(context: InertiaRequestContext): InertiaPage {
        // Merge props: factory shared (global) + interceptor shared (request-scoped) + response props
        val interceptorProps = context.request?.let {
            co.trall.inertia.http.InertiaInterceptor.getSharedProps(it)
        } ?: emptyMap()
        val allProps = factory.getShared() + interceptorProps + props
        val resolvedProps = mutableMapOf<String, Any?>()
        val deferredProps = mutableMapOf<String, MutableList<String>>()
        val mergeProps = mutableListOf<String>()
        val prependProps = mutableListOf<String>()
        val deepMergeProps = mutableListOf<String>()
        val oncePropsMetadata = mutableMapOf<String, OncePropsMetadata>()

        val isFirstLoad = !context.isInertiaRequest
        val isPartialReload = context.isPartialReload

        for ((key, value) in allProps) {
            // Determine if this prop should be included
            val shouldInclude = shouldIncludeProp(key, value, context, isFirstLoad, isPartialReload)

            if (!shouldInclude) {
                // Track deferred props metadata even if not included
                // (so frontend knows what to auto-fetch)
                if (value is Deferrable) {
                    val group = value.group()
                    deferredProps.getOrPut(group) { mutableListOf() }.add(key)
                }
                continue
            }

            // Resolve the prop value
            val resolved = resolveProp(value)
            resolvedProps[key] = resolved

            // Collect metadata for special prop types
            collectPropMetadata(
                key, value, mergeProps, prependProps, deepMergeProps, oncePropsMetadata
            )
        }

        // Add flash data
        val flash = factory.getFlash()

        return InertiaPage(
            component = component,
            props = resolvedProps,
            url = context.url,
            version = factory.version,
            clearHistory = clearHistory,
            encryptHistory = encryptHistory || factory.encryptHistory,
            deferredProps = deferredProps.takeIf { it.isNotEmpty() },
            mergeProps = mergeProps.takeIf { it.isNotEmpty() },
            prependProps = prependProps.takeIf { it.isNotEmpty() },
            deepMergeProps = deepMergeProps.takeIf { it.isNotEmpty() },
            onceProps = oncePropsMetadata.takeIf { it.isNotEmpty() },
            flash = flash.takeIf { it.isNotEmpty() }
        )
    }

    private fun shouldIncludeProp(
        key: String,
        value: Any?,
        context: InertiaRequestContext,
        isFirstLoad: Boolean,
        isPartialReload: Boolean
    ): Boolean {
        // Always include AlwaysInclude props
        if (value is AlwaysInclude) {
            return true
        }

        // Handle partial reload filtering
        if (isPartialReload) {
            // Check if explicitly requested
            if (context.partialData.isNotEmpty() && key !in context.partialData) {
                return false
            }
            // Check if explicitly excluded
            if (key in context.partialExcept) {
                return false
            }
        }

        // Exclude IgnoreFirstLoad (lazy) props unless explicitly requested via partial reload
        if (value is IgnoreFirstLoad) {
            // Only include if this is a partial reload that explicitly requests this prop
            if (!isPartialReload || context.partialData.isEmpty() || key !in context.partialData) {
                return false
            }
        }

        // Exclude deferred props unless explicitly requested via partial reload
        // (frontend auto-fetches them after initial render)
        if (value is Deferrable) {
            if (!isPartialReload || context.partialData.isEmpty() || key !in context.partialData) {
                return false
            }
        }

        // Handle once props
        if (value is Onceable && !value.isFresh()) {
            val onceKey = value.key() ?: key
            if (onceKey in context.exceptOnceProps) {
                return false
            }
        }

        return true
    }

    private fun resolveProp(value: Any?): Any? {
        return when (value) {
            null -> null
            is InertiaProp -> value.resolve()
            is Function0<*> -> value.invoke()
            is Map<*, *> -> value.mapValues { resolveProp(it.value) }
            is List<*> -> value.map { resolveProp(it) }
            else -> value
        }
    }

    private fun collectPropMetadata(
        key: String,
        value: Any?,
        mergeProps: MutableList<String>,
        prependProps: MutableList<String>,
        deepMergeProps: MutableList<String>,
        oncePropsMetadata: MutableMap<String, OncePropsMetadata>
    ) {
        // Collect merge metadata
        if (value is Mergeable) {
            when (value.mergeMode()) {
                MergeMode.APPEND -> mergeProps.add(key)
                MergeMode.PREPEND -> prependProps.add(key)
                MergeMode.DEEP -> deepMergeProps.add(key)
            }
        }

        // Collect once metadata
        if (value is Onceable) {
            val onceKey = value.key() ?: key
            oncePropsMetadata[onceKey] = OncePropsMetadata(
                expiresAt = value.expiresAt()?.toEpochMilli()
            )
        }
    }
}
