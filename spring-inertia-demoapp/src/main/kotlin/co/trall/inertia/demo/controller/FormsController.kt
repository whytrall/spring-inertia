package co.trall.inertia.demo.controller

import co.trall.inertia.Inertia
import co.trall.inertia.InertiaResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.view.RedirectView

@Controller
class FormsController(
    private val inertia: Inertia
) {
    // In-memory storage for demo
    private val contacts = mutableListOf<MutableMap<String, Any>>(
        mutableMapOf("id" to 1, "name" to "John Doe", "email" to "john@example.com", "message" to "Hello!"),
        mutableMapOf("id" to 2, "name" to "Jane Smith", "email" to "jane@example.com", "message" to "Hi there!")
    )
    private var nextId = 3

    @GetMapping("/forms")
    fun index(): InertiaResponse {
        return inertia.render("Forms/Index",
            "contacts" to contacts.toList()
        )
    }

    @GetMapping("/forms/create")
    fun create(): InertiaResponse {
        return inertia.render("Forms/Create")
    }

    @PostMapping("/forms")
    fun store(@RequestBody form: ContactForm): RedirectView {
        // Validate
        val errors = mutableMapOf<String, String>()

        if (form.name.isBlank()) {
            errors["name"] = "Name is required"
        }
        if (form.email.isBlank()) {
            errors["email"] = "Email is required"
        } else if (!form.email.contains("@")) {
            errors["email"] = "Please enter a valid email"
        }
        if (form.message.isBlank()) {
            errors["message"] = "Message is required"
        }

        if (errors.isNotEmpty()) {
            // Flash errors back
            inertia.flash("errors", errors)
            inertia.flash("old", mapOf(
                "name" to form.name,
                "email" to form.email,
                "message" to form.message
            ))
            return RedirectView("/forms/create")
        }

        // Save
        contacts.add(mutableMapOf<String, Any>(
            "id" to nextId++,
            "name" to form.name,
            "email" to form.email,
            "message" to form.message
        ))

        inertia.flash("success", "Contact created successfully!")
        return RedirectView("/forms")
    }

    @PostMapping("/forms/delete")
    fun delete(@RequestBody request: DeleteRequest): RedirectView {
        contacts.removeIf { it["id"] == request.id }
        inertia.flash("success", "Contact deleted successfully!")
        return RedirectView("/forms")
    }

    data class ContactForm(
        val name: String = "",
        val email: String = "",
        val message: String = ""
    )

    data class DeleteRequest(
        val id: Int
    )
}
