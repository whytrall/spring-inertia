package co.trall.inertia

import co.trall.inertia.config.InertiaProperties
import co.trall.inertia.http.InertiaHeaders
import co.trall.inertia.http.InertiaRequestContext
import co.trall.inertia.props.*
import tools.jackson.databind.json.JsonMapper
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Duration
import kotlin.test.*

class InertiaResponseTest {

    private val objectMapper = JsonMapper.builder().findAndAddModules().build()
    private val properties = InertiaProperties(version = "test-version")
    private val factory = InertiaResponseFactory(properties, objectMapper)
    private lateinit var request: MockHttpServletRequest

    @BeforeTest
    fun setUp() {
        request = MockHttpServletRequest().apply {
            requestURI = "/users"
        }
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    }

    @AfterTest
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
        factory.clearShared()
    }

    @Test
    fun `builds page with component name`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertEquals("Users/Index", page.component)
    }

    @Test
    fun `builds page with url`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertEquals("/users", page.url)
    }

    @Test
    fun `builds page with version`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertEquals("test-version", page.version)
    }

    @Test
    fun `resolves simple props`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(
            "name" to "John",
            "age" to 30
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertEquals("John", page.props["name"])
        assertEquals(30, page.props["age"])
    }

    @Test
    fun `resolves closure props`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(
            "computed" to { 1 + 2 }
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertEquals(3, page.props["computed"])
    }

    @Test
    fun `excludes lazy props on first load`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(
            "users" to listOf("John"),
            "stats" to LazyProp { mapOf("count" to 100) }
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertTrue(page.props.containsKey("users"))
        assertFalse(page.props.containsKey("stats"))
    }

    @Test
    fun `includes lazy props when explicitly requested in partial reload`() {
        request.addHeader(InertiaHeaders.INERTIA, "true")
        request.addHeader(InertiaHeaders.PARTIAL_DATA, "stats")
        request.addHeader(InertiaHeaders.PARTIAL_COMPONENT, "Users/Index")

        val response = InertiaResponse("Users/Index", mutableMapOf(
            "users" to listOf("John"),
            "stats" to LazyProp { mapOf("count" to 100) }
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertTrue(page.props.containsKey("stats"))
        assertEquals(mapOf("count" to 100), page.props["stats"])
    }

    @Test
    fun `excludes defer props on first load and adds metadata`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(
            "users" to listOf("John"),
            "analytics" to DeferProp({ mapOf("views" to 500) }, "stats")
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertFalse(page.props.containsKey("analytics"))
        assertNotNull(page.deferredProps)
        assertTrue(page.deferredProps!!["stats"]!!.contains("analytics"))
    }

    @Test
    fun `includes always props in partial reload`() {
        request.addHeader(InertiaHeaders.INERTIA, "true")
        request.addHeader(InertiaHeaders.PARTIAL_DATA, "users")
        request.addHeader(InertiaHeaders.PARTIAL_COMPONENT, "Users/Index")

        val response = InertiaResponse("Users/Index", mutableMapOf(
            "users" to listOf("John"),
            "auth" to AlwaysProp { mapOf("user" to "Admin") }
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertTrue(page.props.containsKey("users"))
        assertTrue(page.props.containsKey("auth"))
    }

    @Test
    fun `filters props by partial-data`() {
        request.addHeader(InertiaHeaders.INERTIA, "true")
        request.addHeader(InertiaHeaders.PARTIAL_DATA, "users")
        request.addHeader(InertiaHeaders.PARTIAL_COMPONENT, "Users/Index")

        val response = InertiaResponse("Users/Index", mutableMapOf(
            "users" to listOf("John"),
            "filters" to mapOf("status" to "active")
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertTrue(page.props.containsKey("users"))
        assertFalse(page.props.containsKey("filters"))
    }

    @Test
    fun `filters props by partial-except`() {
        request.addHeader(InertiaHeaders.INERTIA, "true")
        request.addHeader(InertiaHeaders.PARTIAL_EXCEPT, "users")
        request.addHeader(InertiaHeaders.PARTIAL_COMPONENT, "Users/Index")

        val response = InertiaResponse("Users/Index", mutableMapOf(
            "users" to listOf("John"),
            "filters" to mapOf("status" to "active")
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertFalse(page.props.containsKey("users"))
        assertTrue(page.props.containsKey("filters"))
    }

    @Test
    fun `includes merge props metadata`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(
            "items" to MergeProp { listOf(1, 2, 3) }
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertNotNull(page.mergeProps)
        assertTrue(page.mergeProps!!.contains("items"))
    }

    @Test
    fun `includes prepend props metadata`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(
            "items" to MergeProp { listOf(1, 2, 3) }.prepend()
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertNotNull(page.prependProps)
        assertTrue(page.prependProps!!.contains("items"))
    }

    @Test
    fun `includes deep merge props metadata`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(
            "settings" to MergeProp { mapOf("theme" to "dark") }.deep()
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertNotNull(page.deepMergeProps)
        assertTrue(page.deepMergeProps!!.contains("settings"))
    }

    @Test
    fun `includes once props metadata`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(
            "config" to OnceProp { mapOf("key" to "value") }.until(Duration.ofHours(1))
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertNotNull(page.onceProps)
        assertTrue(page.onceProps!!.containsKey("config"))
        assertNotNull(page.onceProps!!["config"]!!.expiresAt)
    }

    @Test
    fun `sets clearHistory flag`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(), factory)
            .clearHistory()
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertTrue(page.clearHistory)
    }

    @Test
    fun `sets encryptHistory flag`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(), factory)
            .encryptHistory()
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertTrue(page.encryptHistory)
    }

    @Test
    fun `with adds single prop`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(), factory)
            .with("name", "John")
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertEquals("John", page.props["name"])
    }

    @Test
    fun `with adds multiple props from map`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(), factory)
            .with(mapOf("name" to "John", "age" to 30))
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertEquals("John", page.props["name"])
        assertEquals(30, page.props["age"])
    }

    @Test
    fun `resolves nested map props`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(
            "data" to mapOf(
                "nested" to { "computed" }
            )
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        @Suppress("UNCHECKED_CAST")
        val data = page.props["data"] as Map<String, Any?>
        assertEquals("computed", data["nested"])
    }

    @Test
    fun `resolves list props`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(
            "items" to listOf({ 1 }, { 2 }, { 3 })
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertEquals(listOf(1, 2, 3), page.props["items"])
    }

    @Test
    fun `handles null props`() {
        val response = InertiaResponse("Users/Index", mutableMapOf(
            "nullable" to null
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertTrue(page.props.containsKey("nullable"))
        assertNull(page.props["nullable"])
    }

    @Test
    fun `includes shared props`() {
        factory.share("appName", "MyApp")

        val response = InertiaResponse("Users/Index", mutableMapOf(
            "users" to listOf("John")
        ), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertEquals("MyApp", page.props["appName"])
        assertEquals(listOf("John"), page.props["users"])
    }

    @Test
    fun `includes flash data`() {
        factory.flash("success", "User created")

        val response = InertiaResponse("Users/Index", mutableMapOf(), factory)
        val context = InertiaRequestContext.from(request)

        val page = response.buildPage(context)

        assertNotNull(page.flash)
        assertEquals("User created", page.flash!!["success"])
    }
}
