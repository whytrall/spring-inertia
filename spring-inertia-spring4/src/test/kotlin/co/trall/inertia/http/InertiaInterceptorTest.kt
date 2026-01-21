package co.trall.inertia.http

import co.trall.inertia.config.InertiaProperties
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InertiaInterceptorTest {

    private val properties = InertiaProperties(
        rootView = "app",
        version = "test-version"
    )

    private val interceptor = InertiaInterceptor(properties)

    @Test
    fun `preHandle returns true for non-inertia requests`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        val result = interceptor.preHandle(request, response, Any())

        assertTrue(result)
    }

    @Test
    fun `preHandle returns true when versions match`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.INERTIA, "true")
            addHeader(InertiaHeaders.VERSION, "test-version")
        }
        val response = MockHttpServletResponse()

        val result = interceptor.preHandle(request, response, Any())

        assertTrue(result)
    }

    @Test
    fun `preHandle returns false and sets 409 on version mismatch`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.INERTIA, "true")
            addHeader(InertiaHeaders.VERSION, "old-version")
            requestURI = "/users"
        }
        val response = MockHttpServletResponse()

        val result = interceptor.preHandle(request, response, Any())

        assertFalse(result)
        assertEquals(HttpStatus.CONFLICT.value(), response.status)
        assertEquals("/users", response.getHeader(InertiaHeaders.LOCATION))
    }

    @Test
    fun `preHandle sets location header with query string on version mismatch`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.INERTIA, "true")
            addHeader(InertiaHeaders.VERSION, "old-version")
            requestURI = "/users"
            queryString = "page=2"
        }
        val response = MockHttpServletResponse()

        interceptor.preHandle(request, response, Any())

        assertEquals("/users?page=2", response.getHeader(InertiaHeaders.LOCATION))
    }

    @Test
    fun `preHandle returns true when client has no version`() {
        val request = MockHttpServletRequest().apply {
            addHeader(InertiaHeaders.INERTIA, "true")
        }
        val response = MockHttpServletResponse()

        val result = interceptor.preHandle(request, response, Any())

        assertTrue(result)
    }

    @Test
    fun `postHandle adds Vary header`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        interceptor.postHandle(request, response, Any(), null)

        assertEquals(InertiaHeaders.INERTIA, response.getHeader(InertiaHeaders.VARY))
    }

    @Test
    fun `postHandle converts 302 to 303 for PUT requests`() {
        val request = MockHttpServletRequest("PUT", "/users/1").apply {
            addHeader(InertiaHeaders.INERTIA, "true")
        }
        val response = MockHttpServletResponse().apply {
            status = HttpStatus.FOUND.value()
        }

        interceptor.postHandle(request, response, Any(), null)

        assertEquals(HttpStatus.SEE_OTHER.value(), response.status)
    }

    @Test
    fun `postHandle converts 302 to 303 for PATCH requests`() {
        val request = MockHttpServletRequest("PATCH", "/users/1").apply {
            addHeader(InertiaHeaders.INERTIA, "true")
        }
        val response = MockHttpServletResponse().apply {
            status = HttpStatus.FOUND.value()
        }

        interceptor.postHandle(request, response, Any(), null)

        assertEquals(HttpStatus.SEE_OTHER.value(), response.status)
    }

    @Test
    fun `postHandle converts 302 to 303 for DELETE requests`() {
        val request = MockHttpServletRequest("DELETE", "/users/1").apply {
            addHeader(InertiaHeaders.INERTIA, "true")
        }
        val response = MockHttpServletResponse().apply {
            status = HttpStatus.FOUND.value()
        }

        interceptor.postHandle(request, response, Any(), null)

        assertEquals(HttpStatus.SEE_OTHER.value(), response.status)
    }

    @Test
    fun `postHandle keeps 302 for GET requests`() {
        val request = MockHttpServletRequest("GET", "/users").apply {
            addHeader(InertiaHeaders.INERTIA, "true")
        }
        val response = MockHttpServletResponse().apply {
            status = HttpStatus.FOUND.value()
        }

        interceptor.postHandle(request, response, Any(), null)

        assertEquals(HttpStatus.FOUND.value(), response.status)
    }

    @Test
    fun `postHandle keeps 302 for POST requests`() {
        val request = MockHttpServletRequest("POST", "/users").apply {
            addHeader(InertiaHeaders.INERTIA, "true")
        }
        val response = MockHttpServletResponse().apply {
            status = HttpStatus.FOUND.value()
        }

        interceptor.postHandle(request, response, Any(), null)

        assertEquals(HttpStatus.FOUND.value(), response.status)
    }

    @Test
    fun `postHandle does not convert 302 for non-inertia requests`() {
        val request = MockHttpServletRequest("PUT", "/users/1")
        val response = MockHttpServletResponse().apply {
            status = HttpStatus.FOUND.value()
        }

        interceptor.postHandle(request, response, Any(), null)

        assertEquals(HttpStatus.FOUND.value(), response.status)
    }

    @Test
    fun `version returns configured version`() {
        val request = MockHttpServletRequest()

        val version = interceptor.version(request)

        assertEquals("test-version", version)
    }

    @Test
    fun `rootView returns configured root view`() {
        val request = MockHttpServletRequest()

        val rootView = interceptor.rootView(request)

        assertEquals("app", rootView)
    }

    @Test
    fun `share returns empty map by default`() {
        val request = MockHttpServletRequest()

        val shared = interceptor.share(request)

        assertTrue(shared.isEmpty())
    }
}
