package co.trall.inertia

import co.trall.inertia.config.InertiaProperties
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class InertiaResponseFactoryTest {

    private val objectMapper: ObjectMapper = JsonMapper.builder().findAndAddModules().build()
    private val properties = InertiaProperties(
        rootView = "app",
        version = "v1.0.0"
    )
    private val factory = InertiaResponseFactory(properties, objectMapper)
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
    fun `creates response with component and empty props`() {
        val response = factory.render("Users/Index")

        assertEquals("Users/Index", response.component)
    }

    @Test
    fun `creates response with component and props map`() {
        val props = mapOf("users" to listOf("John", "Jane"))
        val response = factory.render("Users/Index", props)

        assertEquals("Users/Index", response.component)
    }

    @Test
    fun `creates response with component and vararg props`() {
        val response = factory.render(
            "Users/Index",
            "users" to listOf("John"),
            "count" to 10
        )

        assertEquals("Users/Index", response.component)
    }

    @Test
    fun `shares static prop value`() {
        factory.share("appName", "MyApp")

        val shared = factory.getShared()

        assertEquals("MyApp", shared["appName"])
    }

    @Test
    fun `shares multiple props at once`() {
        factory.share(mapOf(
            "appName" to "MyApp",
            "version" to "1.0"
        ))

        val shared = factory.getShared()

        assertEquals("MyApp", shared["appName"])
        assertEquals("1.0", shared["version"])
    }

    @Test
    fun `shares callback that is evaluated on getShared`() {
        var counter = 0
        factory.share("counter") { ++counter }

        assertEquals(1, factory.getShared()["counter"])
        assertEquals(2, factory.getShared()["counter"])
    }

    @Test
    fun `clearShared removes all shared props`() {
        factory.share("key", "value")
        factory.clearShared()

        assertTrue(factory.getShared().isEmpty())
    }

    @Test
    fun `flash stores data for next response`() {
        factory.flash("success", "User created")

        val flash = factory.getFlash()

        assertEquals("User created", flash["success"])
    }

    @Test
    fun `getFlash clears flash data`() {
        factory.flash("success", "User created")
        factory.getFlash()

        val flash = factory.getFlash()

        assertTrue(flash.isEmpty())
    }

    @Test
    fun `flash stores multiple values`() {
        factory.flash("success", "User created")
        factory.flash("info", "Check your email")

        val flash = factory.getFlash()

        assertEquals("User created", flash["success"])
        assertEquals("Check your email", flash["info"])
    }

    @Test
    fun `rootView returns configured value`() {
        assertEquals("app", factory.rootView)
    }

    @Test
    fun `version returns configured value`() {
        assertEquals("v1.0.0", factory.version)
    }

    @Test
    fun `encryptHistory returns configured value`() {
        val encryptedFactory = InertiaResponseFactory(
            InertiaProperties(history = InertiaProperties.HistoryProperties(encrypt = true)),
            objectMapper
        )

        assertTrue(encryptedFactory.encryptHistory)
    }

    @Test
    fun `render throws exception for blank component name`() {
        assertFailsWith<IllegalArgumentException> {
            factory.render("   ")
        }
    }

    @Test
    fun `render throws exception for empty component name`() {
        assertFailsWith<IllegalArgumentException> {
            factory.render("")
        }
    }

    @Test
    fun `render with vararg props throws exception for blank component name`() {
        assertFailsWith<IllegalArgumentException> {
            factory.render("", "key" to "value")
        }
    }
}
