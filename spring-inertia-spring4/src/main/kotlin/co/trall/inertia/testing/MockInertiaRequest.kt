package co.trall.inertia.testing

import co.trall.inertia.http.InertiaHeaders
import co.trall.inertia.http.MergeIntent
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

/**
 * Builder for creating mock Inertia requests in tests.
 *
 * Example usage:
 * ```kotlin
 * @Test
 * fun `partial reload returns only requested props`() {
 *     mockMvc.perform(
 *         get("/users")
 *             .asInertiaRequest()
 *             .withPartialData("users", "filters")
 *             .forComponent("Users/Index")
 *     ).andExpect { status { isOk() } }
 * }
 * ```
 */
class MockInertiaRequest(
    private val builder: MockHttpServletRequestBuilder
) {
    /**
     * Marks this as an Inertia request.
     */
    fun asInertia(): MockInertiaRequest {
        builder.header(InertiaHeaders.INERTIA, "true")
        return this
    }

    /**
     * Sets the version header.
     */
    fun withVersion(version: String): MockInertiaRequest {
        builder.header(InertiaHeaders.VERSION, version)
        return this
    }

    /**
     * Sets the partial data header (props to include).
     */
    fun withPartialData(vararg props: String): MockInertiaRequest {
        builder.header(InertiaHeaders.PARTIAL_DATA, props.joinToString(","))
        return this
    }

    /**
     * Sets the partial except header (props to exclude).
     */
    fun withPartialExcept(vararg props: String): MockInertiaRequest {
        builder.header(InertiaHeaders.PARTIAL_EXCEPT, props.joinToString(","))
        return this
    }

    /**
     * Sets the partial component header.
     */
    fun forComponent(component: String): MockInertiaRequest {
        builder.header(InertiaHeaders.PARTIAL_COMPONENT, component)
        return this
    }

    /**
     * Sets the merge intent header.
     */
    fun withMergeIntent(intent: MergeIntent): MockInertiaRequest {
        builder.header(InertiaHeaders.INFINITE_SCROLL_MERGE_INTENT, intent.name.lowercase())
        return this
    }

    /**
     * Sets the except once props header.
     */
    fun withExceptOnceProps(vararg props: String): MockInertiaRequest {
        builder.header(InertiaHeaders.EXCEPT_ONCE_PROPS, props.joinToString(","))
        return this
    }

    /**
     * Sets the reset header (props to re-resolve).
     */
    fun withReset(vararg props: String): MockInertiaRequest {
        builder.header(InertiaHeaders.RESET, props.joinToString(","))
        return this
    }

    /**
     * Sets the error bag header.
     */
    fun withErrorBag(bag: String): MockInertiaRequest {
        builder.header(InertiaHeaders.ERROR_BAG, bag)
        return this
    }

    /**
     * Returns the underlying request builder.
     */
    fun build(): MockHttpServletRequestBuilder = builder
}

/**
 * Extension function to start building an Inertia request.
 */
fun MockHttpServletRequestBuilder.asInertiaRequest(): MockInertiaRequest {
    return MockInertiaRequest(this).asInertia()
}

/**
 * Extension function for partial reload requests.
 */
fun MockHttpServletRequestBuilder.inertiaPartialReload(
    component: String,
    vararg props: String
): MockHttpServletRequestBuilder {
    return MockInertiaRequest(this)
        .asInertia()
        .forComponent(component)
        .withPartialData(*props)
        .build()
}
