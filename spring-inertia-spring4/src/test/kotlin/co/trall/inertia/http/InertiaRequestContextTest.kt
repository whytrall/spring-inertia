package co.trall.inertia.http

import org.springframework.mock.web.MockHttpServletRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InertiaRequestContextTest {

    @Test
    fun `detects inertia request from X-Inertia header`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.INERTIA, "true")
        }
        val context = InertiaRequestContext.from(request)

        assertTrue(context.isInertiaRequest)
    }

    @Test
    fun `detects non-inertia request`() {
        val request = MockHttpServletRequest()
        val context = InertiaRequestContext.from(request)

        assertFalse(context.isInertiaRequest)
    }

    @Test
    fun `extracts version from header`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.VERSION, "abc123")
        }
        val context = InertiaRequestContext.from(request)

        assertEquals("abc123", context.version)
    }

    @Test
    fun `version is null when header not present`() {
        val request = MockHttpServletRequest()
        val context = InertiaRequestContext.from(request)

        assertNull(context.version)
    }

    @Test
    fun `extracts partial component from header`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.PARTIAL_COMPONENT, "Users/Index")
        }
        val context = InertiaRequestContext.from(request)

        assertEquals("Users/Index", context.partialComponent)
    }

    @Test
    fun `parses partial-data header correctly`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.PARTIAL_DATA, "users, filters, stats")
        }
        val context = InertiaRequestContext.from(request)

        assertEquals(setOf("users", "filters", "stats"), context.partialData)
    }

    @Test
    fun `partial-data is empty set when header not present`() {
        val request = MockHttpServletRequest()
        val context = InertiaRequestContext.from(request)

        assertTrue(context.partialData.isEmpty())
    }

    @Test
    fun `parses partial-except header correctly`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.PARTIAL_EXCEPT, "users,filters")
        }
        val context = InertiaRequestContext.from(request)

        assertEquals(setOf("users", "filters"), context.partialExcept)
    }

    @Test
    fun `parses reset header correctly`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.RESET, "config")
        }
        val context = InertiaRequestContext.from(request)

        assertEquals(setOf("config"), context.reset)
    }

    @Test
    fun `parses except-once-props header correctly`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.EXCEPT_ONCE_PROPS, "config,settings")
        }
        val context = InertiaRequestContext.from(request)

        assertEquals(setOf("config", "settings"), context.exceptOnceProps)
    }

    @Test
    fun `parses merge intent append`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.INFINITE_SCROLL_MERGE_INTENT, "append")
        }
        val context = InertiaRequestContext.from(request)

        assertEquals(MergeIntent.APPEND, context.mergeIntent)
    }

    @Test
    fun `parses merge intent prepend`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.INFINITE_SCROLL_MERGE_INTENT, "prepend")
        }
        val context = InertiaRequestContext.from(request)

        assertEquals(MergeIntent.PREPEND, context.mergeIntent)
    }

    @Test
    fun `parses merge intent case insensitive`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.INFINITE_SCROLL_MERGE_INTENT, "APPEND")
        }
        val context = InertiaRequestContext.from(request)

        assertEquals(MergeIntent.APPEND, context.mergeIntent)
    }

    @Test
    fun `merge intent is null for invalid value`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.INFINITE_SCROLL_MERGE_INTENT, "invalid")
        }
        val context = InertiaRequestContext.from(request)

        assertNull(context.mergeIntent)
    }

    @Test
    fun `extracts error bag from header`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.ERROR_BAG, "createUser")
        }
        val context = InertiaRequestContext.from(request)

        assertEquals("createUser", context.errorBag)
    }

    @Test
    fun `identifies partial reload request with partial-data`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.INERTIA, "true")
            addHeader(InertiaHeaders.PARTIAL_DATA, "users")
        }
        val context = InertiaRequestContext.from(request)

        assertTrue(context.isPartialReload)
    }

    @Test
    fun `identifies partial reload request with partial-except`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.INERTIA, "true")
            addHeader(InertiaHeaders.PARTIAL_EXCEPT, "users")
        }
        val context = InertiaRequestContext.from(request)

        assertTrue(context.isPartialReload)
    }

    @Test
    fun `non-inertia request is not partial reload`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.PARTIAL_DATA, "users")
        }
        val context = InertiaRequestContext.from(request)

        assertFalse(context.isPartialReload)
    }

    @Test
    fun `builds url without query string`() {
        val request = MockHttpServletRequest().apply {
            requestURI = "/users"
        }
        val context = InertiaRequestContext.from(request)

        assertEquals("/users", context.url)
    }

    @Test
    fun `builds url with query string`() {
        val request = MockHttpServletRequest().apply {
            requestURI = "/users"
            queryString = "page=1&sort=name"
        }
        val context = InertiaRequestContext.from(request)

        assertEquals("/users?page=1&sort=name", context.url)
    }

    @Test
    fun `extracts http method`() {
        val request = MockHttpServletRequest("POST", "/users")
        val context = InertiaRequestContext.from(request)

        assertEquals("POST", context.method)
    }

    @Test
    fun `caches context in request attribute`() {
        val request = MockHttpServletRequest()
        val context1 = InertiaRequestContext.from(request)
        val context2 = InertiaRequestContext.from(request)

        assertTrue(context1 === context2)
    }
}
