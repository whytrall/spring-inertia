package co.trall.inertia.demo.config

import co.trall.inertia.Inertia
import co.trall.inertia.config.InertiaProperties
import co.trall.inertia.http.InertiaInterceptor
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class InertiaConfig(
    private val inertia: Inertia
) : WebMvcConfigurer {

    /**
     * Custom interceptor that shares common props on every request.
     */
    @Bean
    fun demoInertiaInterceptor(properties: InertiaProperties): DemoInertiaInterceptor {
        return DemoInertiaInterceptor(properties)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        // Interceptor is auto-registered by InertiaAutoConfiguration
    }
}

/**
 * Custom Inertia interceptor for the demo app.
 * Shares navigation and app info on every request.
 */
class DemoInertiaInterceptor(
    properties: InertiaProperties
) : InertiaInterceptor(properties) {

    override fun share(request: HttpServletRequest): Map<String, Any?> {
        return mapOf(
            "app" to mapOf(
                "name" to "Inertia.js Spring Demo",
                "version" to "1.0.0"
            ),
            "navigation" to listOf(
                mapOf("name" to "Home", "href" to "/"),
                mapOf("name" to "Users", "href" to "/users"),
                mapOf("name" to "Props Demo", "href" to "/props"),
                mapOf("name" to "Flash Demo", "href" to "/flash"),
                mapOf("name" to "Forms", "href" to "/forms")
            ),
            "currentPath" to request.requestURI
        )
    }
}
