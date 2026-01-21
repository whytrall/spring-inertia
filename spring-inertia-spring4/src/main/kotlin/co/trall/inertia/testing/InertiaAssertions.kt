package co.trall.inertia.testing

import co.trall.inertia.InertiaPage
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.readValue
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultActionsDsl
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * DSL for asserting Inertia responses in tests.
 *
 * Example usage:
 * ```kotlin
 * @Test
 * fun `shows users page`() {
 *     mockMvc.get("/users")
 *         .andExpect { status { isOk() } }
 *         .andExpectInertia {
 *             component("Users/Index")
 *             has("users")
 *             where("users") { users ->
 *                 (users as List<*>).isNotEmpty()
 *             }
 *         }
 * }
 * ```
 */
class InertiaAssertions(
    private val page: Map<String, Any?>,
    private val objectMapper: ObjectMapper
) {
    /**
     * Asserts that the component name matches.
     */
    fun component(expected: String): InertiaAssertions {
        assertEquals(expected, page["component"], "Component name mismatch")
        return this
    }

    /**
     * Asserts that the URL matches.
     */
    fun url(expected: String): InertiaAssertions {
        assertEquals(expected, page["url"], "URL mismatch")
        return this
    }

    /**
     * Asserts that the version matches.
     */
    fun version(expected: String): InertiaAssertions {
        assertEquals(expected, page["version"], "Version mismatch")
        return this
    }

    /**
     * Asserts that a prop exists.
     */
    fun has(prop: String): InertiaAssertions {
        val props = getProps()
        assertTrue(props.containsKey(prop), "Expected prop '$prop' to exist")
        return this
    }

    /**
     * Asserts that a prop does not exist.
     */
    fun missing(prop: String): InertiaAssertions {
        val props = getProps()
        assertTrue(!props.containsKey(prop), "Expected prop '$prop' to be missing")
        return this
    }

    /**
     * Asserts a prop value using a predicate.
     */
    fun where(prop: String, predicate: (Any?) -> Boolean): InertiaAssertions {
        val props = getProps()
        val value = props[prop]
        assertTrue(predicate(value), "Predicate failed for prop '$prop' with value: $value")
        return this
    }

    /**
     * Asserts that a prop equals an expected value.
     */
    fun whereEquals(prop: String, expected: Any?): InertiaAssertions {
        val props = getProps()
        val value = props[prop]
        assertEquals(expected, value, "Value mismatch for prop '$prop'")
        return this
    }

    /**
     * Asserts that flash data exists with the given key.
     */
    fun hasFlash(key: String): InertiaAssertions {
        val flash = getFlash()
        assertNotNull(flash, "Expected flash data to exist")
        assertTrue(flash.containsKey(key), "Expected flash key '$key' to exist")
        return this
    }

    /**
     * Asserts that flash data equals an expected value.
     */
    fun hasFlash(key: String, expected: Any?): InertiaAssertions {
        val flash = getFlash()
        assertNotNull(flash, "Expected flash data to exist")
        assertEquals(expected, flash[key], "Flash value mismatch for key '$key'")
        return this
    }

    /**
     * Asserts that flash data does not contain the given key.
     */
    fun missingFlash(key: String): InertiaAssertions {
        val flash = getFlash()
        if (flash != null) {
            assertTrue(!flash.containsKey(key), "Expected flash key '$key' to be missing")
        }
        return this
    }

    /**
     * Asserts that a deferred prop exists in the given group.
     */
    fun hasDeferredProp(prop: String, group: String = "default"): InertiaAssertions {
        val deferredProps = getDeferredProps()
        assertNotNull(deferredProps, "Expected deferred props to exist")
        val groupProps = deferredProps[group]
        assertNotNull(groupProps, "Expected deferred props group '$group' to exist")
        assertTrue(prop in groupProps, "Expected prop '$prop' in deferred group '$group'")
        return this
    }

    /**
     * Asserts that a prop is marked as a merge prop.
     */
    fun hasMergeProp(prop: String): InertiaAssertions {
        val mergeProps = getMergeProps()
        assertNotNull(mergeProps, "Expected merge props to exist")
        assertTrue(prop in mergeProps, "Expected prop '$prop' to be a merge prop")
        return this
    }

    /**
     * Asserts that a prop is marked as a prepend prop.
     */
    fun hasPrependProp(prop: String): InertiaAssertions {
        val prependProps = getPrependProps()
        assertNotNull(prependProps, "Expected prepend props to exist")
        assertTrue(prop in prependProps, "Expected prop '$prop' to be a prepend prop")
        return this
    }

    /**
     * Asserts that a prop is marked as a deep merge prop.
     */
    fun hasDeepMergeProp(prop: String): InertiaAssertions {
        val deepMergeProps = getDeepMergeProps()
        assertNotNull(deepMergeProps, "Expected deep merge props to exist")
        assertTrue(prop in deepMergeProps, "Expected prop '$prop' to be a deep merge prop")
        return this
    }

    /**
     * Asserts that a once prop exists.
     */
    fun hasOnceProp(prop: String): InertiaAssertions {
        val onceProps = getOnceProps()
        assertNotNull(onceProps, "Expected once props to exist")
        assertTrue(onceProps.containsKey(prop), "Expected once prop '$prop' to exist")
        return this
    }

    /**
     * Gets the props map from the page.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getProps(): Map<String, Any?> {
        return page["props"] as? Map<String, Any?> ?: emptyMap()
    }

    /**
     * Gets the flash data from the page.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getFlash(): Map<String, Any?>? {
        return page["flash"] as? Map<String, Any?>
    }

    /**
     * Gets the deferred props metadata.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getDeferredProps(): Map<String, List<String>>? {
        return page["deferredProps"] as? Map<String, List<String>>
    }

    /**
     * Gets the merge props list.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getMergeProps(): List<String>? {
        return page["mergeProps"] as? List<String>
    }

    /**
     * Gets the prepend props list.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getPrependProps(): List<String>? {
        return page["prependProps"] as? List<String>
    }

    /**
     * Gets the deep merge props list.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getDeepMergeProps(): List<String>? {
        return page["deepMergeProps"] as? List<String>
    }

    /**
     * Gets the once props metadata.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getOnceProps(): Map<String, Any?>? {
        return page["onceProps"] as? Map<String, Any?>
    }

    companion object {
        private val defaultObjectMapper: ObjectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build()

        /**
         * Creates InertiaAssertions from a MockMvc result.
         */
        fun fromResult(result: MvcResult, objectMapper: ObjectMapper = defaultObjectMapper): InertiaAssertions {
            val content = result.response.contentAsString
            val page: Map<String, Any?> = objectMapper.readValue(content)
            return InertiaAssertions(page, objectMapper)
        }
    }
}

/**
 * Extension function for ResultActions to add Inertia assertions.
 */
fun ResultActions.andExpectInertia(
    objectMapper: ObjectMapper = JsonMapper.builder().findAndAddModules().build(),
    block: InertiaAssertions.() -> Unit
): ResultActions {
    val result = this.andReturn()
    val assertions = InertiaAssertions.fromResult(result, objectMapper)
    assertions.block()
    return this
}

/**
 * Extension function for ResultActionsDsl (Kotlin DSL) to add Inertia assertions.
 */
fun ResultActionsDsl.andExpectInertia(
    objectMapper: ObjectMapper = JsonMapper.builder().findAndAddModules().build(),
    block: InertiaAssertions.() -> Unit
): ResultActionsDsl {
    val result = this.andReturn()
    val assertions = InertiaAssertions.fromResult(result, objectMapper)
    assertions.block()
    return this
}
