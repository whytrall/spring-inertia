package co.trall.inertia.integration

import co.trall.inertia.Inertia
import co.trall.inertia.InertiaResponse
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@SpringBootApplication
class TestApplication

@Controller
class TestController(private val inertia: Inertia) {

    @GetMapping("/")
    fun index(): InertiaResponse {
        return inertia.render("Home/Index",
            "title" to "Welcome",
            "count" to 42
        )
    }

    @GetMapping("/users")
    fun users(): InertiaResponse {
        return inertia.render("Users/Index",
            "users" to listOf(
                mapOf("id" to 1, "name" to "John"),
                mapOf("id" to 2, "name" to "Jane")
            ),
            "filters" to mapOf("status" to "active"),
            "lazy" to inertia.lazy { "lazy value" },
            "always" to inertia.always { "always value" },
            "deferred" to inertia.defer("stats") { "deferred value" }
        )
    }

    @GetMapping("/users/flash")
    fun usersWithFlash(): InertiaResponse {
        inertia.flash("success", "User created successfully")
        return inertia.render("Users/Index",
            "users" to emptyList<Any>()
        )
    }

    @GetMapping("/merge")
    fun mergeProps(): InertiaResponse {
        return inertia.render("Items/Index",
            "items" to inertia.merge { listOf(1, 2, 3) },
            "prepended" to inertia.merge { listOf("a", "b") }.prepend(),
            "nested" to inertia.merge { mapOf("key" to "value") }.deep()
        )
    }

    @GetMapping("/once")
    fun onceProps(): InertiaResponse {
        return inertia.render("Config/Index",
            "config" to inertia.once { mapOf("theme" to "dark") },
            "settings" to inertia.once { mapOf("lang" to "en") }.`as`("appSettings")
        )
    }
}
