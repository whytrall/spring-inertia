package co.trall.inertia.config

import co.trall.inertia.http.InertiaInterceptor
import co.trall.inertia.http.InertiaResponseHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC configuration for Inertia.
 *
 * Registers the Inertia interceptor and return value handler.
 */
@Configuration
class InertiaWebMvcConfigurer(
    private val inertiaInterceptor: InertiaInterceptor,
    private val inertiaResponseHandler: InertiaResponseHandler
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(inertiaInterceptor)
    }

    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        // Add at the beginning to ensure it's checked before default handlers
        handlers.add(0, inertiaResponseHandler)
    }
}
