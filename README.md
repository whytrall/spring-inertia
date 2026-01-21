# spring-inertia

A Spring Boot 4.x adapter for [Inertia.js](https://inertiajs.com/) — build modern single-page applications with server-side routing, without building a separate API.

## Features

- Full Spring Boot 4.x integration written in Kotlin
- Server-Side Rendering (SSR) support via Node.js
- Multiple prop types for different loading strategies (lazy, deferred, merge, etc.)
- Thymeleaf dialect for seamless template integration
- Spring Data `Page` support with pagination helpers
- Built-in testing utilities
- Flash messages and shared props

## Requirements

- Java 21+
- Spring Boot 4.0.1+
- Kotlin 2.3.0+

## Installation

### 1. Add the GitHub Packages repository

In your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/whytrall/spring-inertia")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

### 2. Add credentials

Create or edit `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

Generate a token at https://github.com/settings/tokens with `read:packages` scope.

### 3. Add the dependency

```kotlin
dependencies {
    implementation("co.trall:spring-inertia-spring4:1.0.0")
}
```

## Quick Start

### 1. Create the root template

Create a Thymeleaf template (e.g., `templates/app.html`):

```html
<!DOCTYPE html>
<html xmlns:inertia="http://trall.co/inertia">
<head>
    <meta charset="UTF-8">
    <title>My App</title>
    <inertia:head />
</head>
<body>
    <inertia:page />
    <script type="module" src="/assets/app.js"></script>
</body>
</html>
```

### 2. Configure application properties

```yaml
inertia:
  root-view: app
  version: "1.0.0"
```

### 3. Create a controller

```kotlin
@Controller
class HomeController(private val inertia: Inertia) {

    @GetMapping("/")
    fun index(): InertiaResponse {
        return inertia.render("Home",
            "title" to "Welcome",
            "message" to "Never gonna give you up"
        )
    }
}
```

## Configuration

All configuration options available in `application.yml`:

```yaml
inertia:
  # Root Thymeleaf template name (required)
  root-view: app

  # Asset version for cache busting
  version: "1.0.0"

  # Server-Side Rendering
  ssr:
    enabled: false
    url: http://localhost:13714
    ensureBundleExists: true
    bundle: null

  # Browser history
  history:
    encrypt: false

  # Page validation
  pages:
    ensureExist: false
    paths:
      - classpath:/templates/pages/
    extensions:
      - .vue
      - .tsx
      - .jsx
      - .svelte
```

## Prop Types

spring-inertia supports various prop types for different data loading scenarios:

| Prop Type | Description                                  | Usage                             |
|-----------|----------------------------------------------|-----------------------------------|
| Regular   | Always included in response                  | `"key" to value`                  |
| Lazy      | Only loaded on explicit partial reload       | `"key" to inertia.lazy { ... }`   |
| Deferred  | Auto-loaded by frontend after initial render | `"key" to inertia.defer { ... }`  |
| Always    | Included even during partial reloads         | `"key" to inertia.always { ... }` |
| Merge     | Merged with existing client data             | `"key" to inertia.merge { ... }`  |
| Once      | Cached client-side with optional expiration  | `"key" to inertia.once { ... }`   |
| Scroll    | Pagination with auto-extracted metadata      | `"key" to inertia.scroll(page)`   |

### Examples

```kotlin
@GetMapping("/users")
fun users(pageable: Pageable): InertiaResponse {
    val page = userRepository.findAll(pageable)

    return inertia.render("Users/Index",
        // Regular prop - always included
        "filters" to filters,

        // Pagination with infinite scroll support
        "users" to inertia.scroll(page),

        // Loaded after initial render
        "stats" to inertia.defer {
            calculateStats()
        },

        // Only on explicit request
        "activity" to inertia.lazy {
            loadActivityLog()
        }
    )
}
```

## Shared Props

Use an interceptor to share data across all responses:

```kotlin
@Component
class AppInterceptor : InertiaInterceptor {
    override fun share(request: HttpServletRequest): Map<String, Any?> {
        return mapOf(
            "auth" to getCurrentUser(),
            "navigation" to getNavigation(),
            "appName" to "My App"
        )
    }
}
```

Or share directly via the `Inertia` instance:

```kotlin
inertia.share("appVersion", "1.0.0")
```

## Flash Messages

Flash messages persist through redirects:

```kotlin
@PostMapping("/users")
fun store(@Valid user: User): InertiaResponse {
    userRepository.save(user)
    inertia.flash("success", "User created successfully")
    return inertia.location("/users")
}
```

## Testing

spring-inertia provides testing utilities for verifying responses:

```kotlin
@Test
fun `renders home page with correct props`() {
    mockMvc.get("/") {
        header(InertiaHeaders.INERTIA, "true")
    }.andExpectInertia {
        component("Home")
        has("title")
        whereEquals("title", "Welcome")
    }
}
```

### Mock Inertia Requests

```kotlin
val request = MockInertiaRequest.builder()
    .partialData("users", "filters")
    .build()
```

## Module Structure

The project consists of three modules:

| Module                   | Description                                      |
|--------------------------|--------------------------------------------------|
| `spring-inertia-core`    | Core functionality with zero Spring dependencies |
| `spring-inertia-spring4` | Spring Boot 4.x integration                      |
| `spring-inertia-demoapp` | Reference implementation                         |

## Running the Demo

```bash
# Build the project
./gradlew build

# Run the demo app
./gradlew :spring-inertia-demoapp:bootRun

# Access at http://localhost:8083
```

For frontend development:

```bash
cd spring-inertia-demoapp/frontend
npm install
npm run dev
```

## Frontend Setup

spring-inertia works with any Inertia.js-compatible frontend framework (React, Vue, Svelte). See the [Inertia.js documentation](https://inertiajs.com/) for client-side setup.

Example with React:

```jsx
import { createInertiaApp } from '@inertiajs/react'
import { createRoot } from 'react-dom/client'

createInertiaApp({
    resolve: name => import(`./Pages/${name}.jsx`),
    setup({ el, App, props }) {
        createRoot(el).render(<App {...props} />)
    },
})
```

## License

Apache License, Version 2.0