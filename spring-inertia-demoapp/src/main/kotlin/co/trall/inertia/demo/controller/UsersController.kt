package co.trall.inertia.demo.controller

import co.trall.inertia.Inertia
import co.trall.inertia.InertiaResponse
import co.trall.inertia.props.SimplePageData
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class UsersController(
    private val inertia: Inertia
) {
    // Simulated user data
    private val allUsers = (1..50).map { id ->
        mapOf(
            "id" to id,
            "name" to "User $id",
            "email" to "user$id@example.com",
            "role" to if (id % 3 == 0) "Admin" else "User",
            "createdAt" to "2026-01-${(id % 28 + 1).toString().padStart(2, '0')}"
        )
    }

    @GetMapping("/users")
    fun index(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "") search: String
    ): InertiaResponse {
        val filtered = if (search.isNotBlank()) {
            allUsers.filter {
                (it["name"] as String).contains(search, ignoreCase = true) ||
                (it["email"] as String).contains(search, ignoreCase = true)
            }
        } else {
            allUsers
        }

        val totalElements = filtered.size.toLong()
        val start = (page * size).coerceAtMost(filtered.size)
        val end = ((page + 1) * size).coerceAtMost(filtered.size)
        val pageContent = filtered.subList(start, end)

        val pageData = SimplePageData.of(
            content = pageContent,
            pageNumber = page,
            pageSize = size,
            totalElements = totalElements
        )

        return inertia.render("Users/Index",
            "users" to inertia.scroll(pageData),
            "filters" to mapOf(
                "search" to search,
                "page" to page,
                "size" to size
            ),
            "stats" to inertia.defer {
                // Simulated slow stats calculation
                Thread.sleep(100)
                mapOf(
                    "totalUsers" to allUsers.size,
                    "admins" to allUsers.count { it["role"] == "Admin" },
                    "regularUsers" to allUsers.count { it["role"] == "User" }
                )
            }
        )
    }

    @GetMapping("/users/{id}")
    fun show(@PathVariable id: Int): InertiaResponse {
        val user = allUsers.find { it["id"] == id }
            ?: return inertia.render("Users/NotFound", "id" to id)

        return inertia.render("Users/Show",
            "user" to user,
            "activity" to inertia.lazy {
                // Simulated activity log (only loaded when requested)
                (1..5).map { i ->
                    mapOf(
                        "action" to "Action $i",
                        "timestamp" to "2024-01-${(i * 5).toString().padStart(2, '0')} 10:${i}0:00"
                    )
                }
            }
        )
    }
}
