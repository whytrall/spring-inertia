package co.trall.inertia

import co.trall.inertia.config.InertiaProperties
import tools.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.FlashMap
import org.springframework.web.servlet.support.RequestContextUtils
import java.util.concurrent.ConcurrentHashMap

/**
 * Factory for creating Inertia responses and managing shared state.
 *
 * This class manages:
 * - Shared props (available on every response)
 * - Flash data (one-time messages)
 * - Asset versioning
 * - Configuration
 *
 * Note: Shared props are application-scoped. For request-specific props,
 * use the `with()` method on individual responses or share callbacks
 * that resolve per-request data.
 */
class InertiaResponseFactory(
    private val properties: InertiaProperties,
    internal val objectMapper: ObjectMapper
) {
    // Thread-safe maps for concurrent access in web environment
    private val sharedProps = ConcurrentHashMap<String, Any?>()
    private val sharedCallbacks = ConcurrentHashMap<String, () -> Any?>()

    /**
     * The root view template name.
     */
    val rootView: String
        get() = properties.rootView

    /**
     * The current asset version.
     */
    val version: String?
        get() = properties.version

    /**
     * Whether history encryption is enabled globally.
     */
    val encryptHistory: Boolean
        get() = properties.history.encrypt

    /**
     * Creates a new Inertia response for the given component.
     *
     * @param component The component name (e.g., "Users/Index"). Must not be blank.
     * @param props The props to pass to the component.
     * @throws IllegalArgumentException if the component name is blank.
     */
    fun render(component: String, props: Map<String, Any?> = emptyMap()): InertiaResponse {
        validateComponentName(component)
        return InertiaResponse(component, props.toMutableMap(), this)
    }

    /**
     * Creates a new Inertia response for the given component with vararg props.
     *
     * @param component The component name (e.g., "Users/Index"). Must not be blank.
     * @param props The props to pass to the component as key-value pairs.
     * @throws IllegalArgumentException if the component name is blank.
     */
    fun render(component: String, vararg props: Pair<String, Any?>): InertiaResponse {
        validateComponentName(component)
        return InertiaResponse(component, props.toMap().toMutableMap(), this)
    }

    private fun validateComponentName(component: String) {
        require(component.isNotBlank()) { "Component name must not be blank" }
    }

    /**
     * Shares a static prop value that will be included in every response.
     */
    fun share(key: String, value: Any?) {
        sharedProps[key] = value
    }

    /**
     * Shares a callback that will be invoked on every response.
     */
    fun share(key: String, callback: () -> Any?) {
        sharedCallbacks[key] = callback
    }

    /**
     * Shares multiple props at once.
     */
    fun share(props: Map<String, Any?>) {
        sharedProps.putAll(props)
    }

    /**
     * Gets all shared props, resolving callbacks.
     *
     * Takes atomic snapshots of both static props and callbacks to ensure
     * consistent state even under concurrent modifications.
     */
    fun getShared(): Map<String, Any?> {
        // Take atomic snapshots of both maps
        val propsSnapshot = sharedProps.toMap()
        val callbacksSnapshot = sharedCallbacks.toMap()

        // Merge static props with resolved callbacks
        return propsSnapshot + callbacksSnapshot.mapValues { (_, callback) -> callback() }
    }

    /**
     * Clears all shared props.
     */
    fun clearShared() {
        sharedProps.clear()
        sharedCallbacks.clear()
    }

    /**
     * Sets flash data that will be included in the next response.
     * Flash data survives redirects using Spring's FlashMap mechanism.
     * Falls back to request attributes when FlashMap infrastructure is unavailable.
     */
    fun flash(key: String, value: Any?) {
        val request = currentRequest() ?: return
        val flashMap = getOutputFlashMapSafe(request)
        if (flashMap != null) {
            flashMap[FLASH_ATTRIBUTE_KEY] = getInertiaFlashMap(flashMap).apply {
                put(key, value)
            }
        } else {
            // Fallback: store in request attributes when FlashMap is not available
            @Suppress("UNCHECKED_CAST")
            val existing = request.getAttribute(FLASH_ATTRIBUTE_KEY) as? MutableMap<String, Any?>
                ?: mutableMapOf()
            existing[key] = value
            request.setAttribute(FLASH_ATTRIBUTE_KEY, existing)
        }
    }

    /**
     * Gets flash data for the current response.
     * Combines output flash (current request) and input flash (from redirect).
     */
    internal fun getFlash(): Map<String, Any?> {
        val request = currentRequest() ?: return emptyMap()
        val result = mutableMapOf<String, Any?>()

        // Get flash from previous request (survives redirect via FlashMap)
        val inputFlashMap = RequestContextUtils.getInputFlashMap(request)
        if (inputFlashMap != null) {
            @Suppress("UNCHECKED_CAST")
            val inertiaFlash = inputFlashMap[FLASH_ATTRIBUTE_KEY] as? Map<String, Any?>
            if (inertiaFlash != null) {
                result.putAll(inertiaFlash)
            }
        }

        // Get flash from current request (same request, no redirect)
        val outputFlashMap = getOutputFlashMapSafe(request)
        if (outputFlashMap != null) {
            @Suppress("UNCHECKED_CAST")
            val currentFlash = outputFlashMap[FLASH_ATTRIBUTE_KEY] as? Map<String, Any?>
            if (currentFlash != null) {
                result.putAll(currentFlash)
                // Clear current request flash after reading (one-time use)
                outputFlashMap.remove(FLASH_ATTRIBUTE_KEY)
            }
        } else {
            // Fallback: check request attributes when FlashMap is not available
            @Suppress("UNCHECKED_CAST")
            val attrFlash = request.getAttribute(FLASH_ATTRIBUTE_KEY) as? Map<String, Any?>
            if (attrFlash != null) {
                result.putAll(attrFlash)
                request.removeAttribute(FLASH_ATTRIBUTE_KEY)
            }
        }

        return result
    }

    /**
     * Safely gets the output FlashMap, returning null if FlashMap infrastructure is unavailable.
     * This handles test environments and edge cases where DispatcherServlet hasn't been set up.
     */
    private fun getOutputFlashMapSafe(request: HttpServletRequest): FlashMap? {
        return try {
            RequestContextUtils.getOutputFlashMap(request)
        } catch (e: IllegalStateException) {
            // FlashMapManager not available (e.g., in tests without full MVC setup)
            null
        }
    }

    private fun getInertiaFlashMap(flashMap: FlashMap): MutableMap<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        var inertiaFlash = flashMap[FLASH_ATTRIBUTE_KEY] as? MutableMap<String, Any?>
        if (inertiaFlash == null) {
            inertiaFlash = mutableMapOf()
        }
        return inertiaFlash
    }

    companion object {
        internal const val FLASH_ATTRIBUTE_KEY = "co.trall.inertia.flash"
    }

    private fun currentRequest(): HttpServletRequest? {
        val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        return attrs?.request
    }
}
