package co.trall.inertia.demo.controller

import co.trall.inertia.Inertia
import co.trall.inertia.InertiaResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.view.RedirectView

@Controller
class FlashController(
    private val inertia: Inertia
) {
    @GetMapping("/flash")
    fun index(): InertiaResponse {
        return inertia.render("Flash/Index",
            "description" to "Flash messages are one-time messages that survive redirects."
        )
    }

    @PostMapping("/flash/success")
    fun success(): RedirectView {
        inertia.flash("success", "Operation completed successfully!")
        return RedirectView("/flash")
    }

    @PostMapping("/flash/error")
    fun error(): RedirectView {
        inertia.flash("error", "Something went wrong. Please try again.")
        return RedirectView("/flash")
    }

    @PostMapping("/flash/info")
    fun info(): RedirectView {
        inertia.flash("info", "Here's some useful information for you.")
        return RedirectView("/flash")
    }

    @PostMapping("/flash/warning")
    fun warning(): RedirectView {
        inertia.flash("warning", "Please be careful with this action.")
        return RedirectView("/flash")
    }

    @PostMapping("/flash/multiple")
    fun multiple(): RedirectView {
        inertia.flash("success", "User created successfully!")
        inertia.flash("info", "An email has been sent to the user.")
        return RedirectView("/flash")
    }

    @PostMapping("/flash/custom")
    fun custom(@RequestParam type: String, @RequestParam message: String): RedirectView {
        inertia.flash(type, message)
        return RedirectView("/flash")
    }
}
