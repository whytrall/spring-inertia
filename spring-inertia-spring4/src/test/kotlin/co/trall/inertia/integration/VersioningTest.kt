package co.trall.inertia.integration

import co.trall.inertia.http.InertiaHeaders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.Test

@SpringBootTest(
    classes = [TestApplication::class],
    properties = ["inertia.version=v1.0.0"]
)
@AutoConfigureMockMvc
class VersioningTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `matching version returns normal response`() {
        mockMvc.get("/") {
            header(InertiaHeaders.INERTIA, "true")
            header(InertiaHeaders.VERSION, "v1.0.0")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `version mismatch triggers 409 with location header`() {
        mockMvc.get("/users") {
            header(InertiaHeaders.INERTIA, "true")
            header(InertiaHeaders.VERSION, "old-version")
        }.andExpect {
            status { isEqualTo(HttpStatus.CONFLICT.value()) }
            header { exists(InertiaHeaders.LOCATION) }
            header { string(InertiaHeaders.LOCATION, "/users") }
        }
    }

    @Test
    fun `version mismatch includes query string in location`() {
        mockMvc.get("/users?page=2&sort=name") {
            header(InertiaHeaders.INERTIA, "true")
            header(InertiaHeaders.VERSION, "old-version")
        }.andExpect {
            status { isEqualTo(HttpStatus.CONFLICT.value()) }
            header { string(InertiaHeaders.LOCATION, "/users?page=2&sort=name") }
        }
    }

    @Test
    fun `request without version header succeeds`() {
        mockMvc.get("/") {
            header(InertiaHeaders.INERTIA, "true")
        }.andExpect {
            status { isOk() }
        }
    }
}
