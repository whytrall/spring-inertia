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
class PartialReloadTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `partial reload with only returns subset of props`() {
        mockMvc.get("/users") {
            header(InertiaHeaders.INERTIA, "true")
            header(InertiaHeaders.PARTIAL_DATA, "users")
            header(InertiaHeaders.PARTIAL_COMPONENT, "Users/Index")
        }.andExpectInertia {
            has("users")
            missing("filters")
        }
    }

    @Test
    fun `partial reload with except excludes specified props`() {
        mockMvc.get("/users") {
            header(InertiaHeaders.INERTIA, "true")
            header(InertiaHeaders.PARTIAL_EXCEPT, "users")
            header(InertiaHeaders.PARTIAL_COMPONENT, "Users/Index")
        }.andExpectInertia {
            missing("users")
            has("filters")
        }
    }

    @Test
    fun `partial reload includes always props even when not requested`() {
        mockMvc.get("/users") {
            header(InertiaHeaders.INERTIA, "true")
            header(InertiaHeaders.PARTIAL_DATA, "users")
            header(InertiaHeaders.PARTIAL_COMPONENT, "Users/Index")
        }.andExpectInertia {
            has("users")
            has("always")  // AlwaysProp should be included
        }
    }

    @Test
    fun `partial reload can request lazy props`() {
        mockMvc.get("/users") {
            header(InertiaHeaders.INERTIA, "true")
            header(InertiaHeaders.PARTIAL_DATA, "lazy")
            header(InertiaHeaders.PARTIAL_COMPONENT, "Users/Index")
        }.andExpectInertia {
            has("lazy")
            whereEquals("lazy", "lazy value")
        }
    }

    @Test
    fun `partial reload can request deferred props`() {
        mockMvc.get("/users") {
            header(InertiaHeaders.INERTIA, "true")
            header(InertiaHeaders.PARTIAL_DATA, "deferred")
            header(InertiaHeaders.PARTIAL_COMPONENT, "Users/Index")
        }.andExpectInertia {
            has("deferred")
            whereEquals("deferred", "deferred value")
        }
    }

    @Test
    fun `partial reload with multiple props`() {
        mockMvc.get("/users") {
            header(InertiaHeaders.INERTIA, "true")
            header(InertiaHeaders.PARTIAL_DATA, "users,filters")
            header(InertiaHeaders.PARTIAL_COMPONENT, "Users/Index")
        }.andExpectInertia {
            has("users")
            has("filters")
            missing("lazy")
        }
    }
}
