package co.trall.inertia.demo.controller

import co.trall.inertia.Inertia
import co.trall.inertia.InertiaResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController(
    private val inertia: Inertia
) {
    @GetMapping("/")
    fun index(): InertiaResponse {
        return inertia.render("Home",
            "title" to "Welcome to Inertia.js Spring",
            "description" to "This demo showcases the Spring Boot adapter for Inertia.js",
            "features" to listOf(
                mapOf(
                    "title" to "Server-Side Rendering Ready",
                    "description" to "Full SSR support with Node.js integration"
                ),
                mapOf(
                    "title" to "Lazy & Deferred Props",
                    "description" to "Load data on-demand for better performance"
                ),
                mapOf(
                    "title" to "Flash Messages",
                    "description" to "Session flash data that survives redirects"
                ),
                mapOf(
                    "title" to "Partial Reloads",
                    "description" to "Only reload the data you need"
                )
            )
        )
    }
}
