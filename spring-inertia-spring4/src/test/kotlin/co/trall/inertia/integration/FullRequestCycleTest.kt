package co.trall.inertia.integration

import co.trall.inertia.http.InertiaHeaders
import co.trall.inertia.testing.andExpectInertia
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.Test

@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureMockMvc
class FullRequestCycleTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `inertia request returns JSON with correct content type`() {
        mockMvc.get("/") {
            header(InertiaHeaders.INERTIA, "true")
        }.andExpect {
            status { isOk() }
            content { contentType("application/json") }
            header { string(InertiaHeaders.INERTIA, "true") }
        }
    }

    @Test
    fun `inertia request returns correct component`() {
        mockMvc.get("/") {
            header(InertiaHeaders.INERTIA, "true")
        }.andExpectInertia {
            component("Home/Index")
        }
    }

    @Test
    fun `inertia request returns correct url`() {
        mockMvc.get("/users?page=1") {
            header(InertiaHeaders.INERTIA, "true")
        }.andExpectInertia {
            url("/users?page=1")
        }
    }

    @Test
    fun `inertia request returns resolved props`() {
        mockMvc.get("/") {
            header(InertiaHeaders.INERTIA, "true")
        }.andExpectInertia {
            has("title")
            has("count")
            whereEquals("title", "Welcome")
            whereEquals("count", 42)
        }
    }

    @Test
    fun `lazy props excluded on first load`() {
        mockMvc.get("/users") {
            header(InertiaHeaders.INERTIA, "true")
        }.andExpectInertia {
            has("users")
            has("filters")
            missing("lazy")
        }
    }

    @Test
    fun `deferred props excluded and metadata included`() {
        mockMvc.get("/users") {
            header(InertiaHeaders.INERTIA, "true")
        }.andExpectInertia {
            missing("deferred")
            hasDeferredProp("deferred", "stats")
        }
    }

    @Test
    fun `always props always included`() {
        mockMvc.get("/users") {
            header(InertiaHeaders.INERTIA, "true")
        }.andExpectInertia {
            has("always")
            whereEquals("always", "always value")
        }
    }

    @Test
    fun `flash data included in response`() {
        mockMvc.get("/users/flash") {
            header(InertiaHeaders.INERTIA, "true")
        }.andExpectInertia {
            hasFlash("success", "User created successfully")
        }
    }

    @Test
    fun `merge props include metadata`() {
        mockMvc.get("/merge") {
            header(InertiaHeaders.INERTIA, "true")
        }.andExpectInertia {
            has("items")
            hasMergeProp("items")
            hasPrependProp("prepended")
            hasDeepMergeProp("nested")
        }
    }

    @Test
    fun `once props include metadata`() {
        mockMvc.get("/once") {
            header(InertiaHeaders.INERTIA, "true")
        }.andExpectInertia {
            has("config")
            has("settings")
            hasOnceProp("config")
            hasOnceProp("appSettings")  // Custom key
        }
    }
}
