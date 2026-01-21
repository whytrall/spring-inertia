package co.trall.inertia.ssr

import co.trall.inertia.InertiaPage
import co.trall.inertia.config.InertiaProperties
import org.springframework.web.client.RestClient
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull

class HttpSsrGatewayTest {

    private val properties = InertiaProperties(
        ssr = InertiaProperties.SsrProperties(
            enabled = true,
            url = "http://localhost:13714"
        )
    )

    @Test
    fun `render returns null when SSR service unavailable`() {
        // Using a port that's not listening
        val gateway = HttpSsrGateway(
            InertiaProperties(
                ssr = InertiaProperties.SsrProperties(
                    enabled = true,
                    url = "http://localhost:59999"
                )
            ),
            RestClient.builder()
        )

        val page = InertiaPage(
            component = "Test",
            props = mapOf("key" to "value"),
            url = "/test",
            version = "1.0"
        )

        val result = gateway.render(page)

        assertNull(result)
    }

    @Test
    fun `isAvailable returns false when SSR service unavailable`() {
        val gateway = HttpSsrGateway(
            InertiaProperties(
                ssr = InertiaProperties.SsrProperties(
                    enabled = true,
                    url = "http://localhost:59999"
                )
            ),
            RestClient.builder()
        )

        val result = gateway.isAvailable()

        assertFalse(result)
    }
}
