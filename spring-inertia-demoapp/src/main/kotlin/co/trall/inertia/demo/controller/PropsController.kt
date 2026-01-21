package co.trall.inertia.demo.controller

import co.trall.inertia.Inertia
import co.trall.inertia.InertiaResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.time.Duration
import java.time.Instant

@Controller
class PropsController(
    private val inertia: Inertia
) {
    @GetMapping("/props")
    fun index(): InertiaResponse {
        return inertia.render("Props/Index",
            // Regular prop - always included
            "timestamp" to Instant.now().toString(),

            // Always prop - included even in partial reloads
            "serverTime" to inertia.always { Instant.now().toString() },

            // Lazy prop - excluded from initial load, must be explicitly requested
            "lazyData" to inertia.lazy {
                Thread.sleep(200) // Simulate slow operation
                mapOf(
                    "message" to "This was loaded lazily!",
                    "loadedAt" to Instant.now().toString()
                )
            },

            // Deferred prop - auto-fetched by frontend after page loads
            "deferredStats" to inertia.defer {
                Thread.sleep(300) // Simulate slow operation
                mapOf(
                    "cpuUsage" to "${(Math.random() * 100).toInt()}%",
                    "memoryUsage" to "${(Math.random() * 100).toInt()}%",
                    "loadedAt" to Instant.now().toString()
                )
            },

            // Deferred prop in custom group
            "deferredAnalytics" to inertia.defer("analytics") {
                Thread.sleep(400) // Simulate slow operation
                mapOf(
                    "pageViews" to (Math.random() * 10000).toInt(),
                    "uniqueVisitors" to (Math.random() * 5000).toInt(),
                    "loadedAt" to Instant.now().toString()
                )
            },

            // Once prop - cached on client with TTL
            "config" to inertia.once {
                mapOf(
                    "theme" to "dark",
                    "locale" to "en",
                    "cachedAt" to Instant.now().toString()
                )
            }.until(Duration.ofMinutes(5)),

            // Merge prop - data is merged with existing client data
            "notifications" to inertia.merge {
                listOf(
                    mapOf("id" to 1, "message" to "Notification at ${Instant.now()}")
                )
            }
        )
    }

    @GetMapping("/props/partial")
    fun partial(): InertiaResponse {
        return inertia.render("Props/Partial",
            "items" to (1..5).map { mapOf("id" to it, "name" to "Item $it") },
            "meta" to mapOf(
                "total" to 100,
                "page" to 1
            ),
            "expensive" to inertia.lazy {
                Thread.sleep(500)
                "This took 500ms to load"
            }
        )
    }
}
