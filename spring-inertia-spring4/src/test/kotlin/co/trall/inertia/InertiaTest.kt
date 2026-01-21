package co.trall.inertia

import co.trall.inertia.config.InertiaProperties
import co.trall.inertia.http.InertiaHeaders
import co.trall.inertia.props.*
import tools.jackson.databind.json.JsonMapper
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import kotlin.test.*

class InertiaTest {

    private val objectMapper = JsonMapper.builder().findAndAddModules().build()
    private val properties = InertiaProperties(version = "test-version")
    private val factory = InertiaResponseFactory(properties, objectMapper)
    private val inertia = Inertia(factory)
    private lateinit var request: MockHttpServletRequest

    @BeforeTest
    fun setUp() {
        request = MockHttpServletRequest()
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    }

    @AfterTest
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
        factory.clearShared()
    }

    @Test
    fun `render creates response with component`() {
        val response = inertia.render("Dashboard")

        assertEquals("Dashboard", response.component)
    }

    @Test
    fun `render creates response with props map`() {
        val response = inertia.render("Dashboard", mapOf("title" to "Home"))

        assertEquals("Dashboard", response.component)
    }

    @Test
    fun `render creates response with vararg props`() {
        val response = inertia.render(
            "Dashboard",
            "title" to "Home",
            "count" to 42
        )

        assertEquals("Dashboard", response.component)
    }

    @Test
    fun `share adds prop to all responses`() {
        inertia.share("appName", "MyApp")

        val shared = inertia.getShared()

        assertEquals("MyApp", shared["appName"])
    }

    @Test
    fun `share adds callback prop`() {
        var counter = 0
        inertia.share("counter") { ++counter }

        assertEquals(1, inertia.getShared()["counter"])
        assertEquals(2, inertia.getShared()["counter"])
    }

    @Test
    fun `share adds multiple props`() {
        inertia.share(mapOf("a" to 1, "b" to 2))

        val shared = inertia.getShared()

        assertEquals(1, shared["a"])
        assertEquals(2, shared["b"])
    }

    @Test
    fun `flash adds flash data`() {
        inertia.flash("success", "Done!")

        val flash = factory.getFlash()

        assertEquals("Done!", flash["success"])
    }

    @Test
    fun `lazy creates LazyProp`() {
        val prop = inertia.lazy { "lazy value" }

        assertTrue(prop is LazyProp)
        assertEquals("lazy value", prop.resolve())
    }

    @Test
    fun `optional creates OptionalProp`() {
        val prop = inertia.optional { "optional value" }

        assertTrue(prop is OptionalProp)
        assertEquals("optional value", prop.resolve())
    }

    @Test
    fun `defer creates DeferProp with default group`() {
        val prop = inertia.defer { "deferred value" }

        assertTrue(prop is DeferProp)
        assertEquals("default", prop.group())
        assertEquals("deferred value", prop.resolve())
    }

    @Test
    fun `defer creates DeferProp with custom group`() {
        val prop = inertia.defer("stats") { "analytics" }

        assertEquals("stats", prop.group())
    }

    @Test
    fun `merge creates MergeProp`() {
        val prop = inertia.merge { listOf(1, 2, 3) }

        assertTrue(prop is MergeProp)
        assertEquals(MergeMode.APPEND, prop.mergeMode())
    }

    @Test
    fun `always creates AlwaysProp`() {
        val prop = inertia.always { "always included" }

        assertTrue(prop is AlwaysProp)
        assertEquals("always included", prop.resolve())
    }

    @Test
    fun `once creates OnceProp`() {
        val prop = inertia.once { "cached value" }

        assertTrue(prop is OnceProp)
        assertEquals("cached value", prop.resolve())
    }

    @Test
    fun `scroll creates ScrollProp from Page`() {
        val page = PageImpl(
            listOf("item1", "item2"),
            PageRequest.of(0, 10),
            2
        )

        val prop = inertia.scroll(page)

        assertTrue(prop is ScrollProp)
        val resolved = prop.resolve() as ScrollData<*>
        assertEquals(listOf("item1", "item2"), resolved.items)
        assertEquals(0, resolved.meta.currentPage)
        assertEquals(1, resolved.meta.totalPages)
    }

    @Test
    fun `location returns 409 with location header`() {
        val response = inertia.location("https://example.com")

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("https://example.com", response.headers[InertiaHeaders.LOCATION]?.first())
    }
}
