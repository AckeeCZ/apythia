# Apythia

In ancient times, the Oracle of Delphi—Pythia—revealed truths hidden to mortal eyes. Today, your
API calls are the hidden realm, filled with subtle mistakes, unmocked responses, and unexpected behaviors.

Enter Apythia – your magical oracle for API communication, revealing hidden truths in your
Kotlin Multiplatform projects through mocking and assertions — all while hiding the underlying
API implementation.

## 🔮 What Apythia Does

Apythia helps you test your API/network logic confidently with a clear, expressive DSL:
- Mock API responses easily – define exactly how your API client should respond.
- Assert requests elegantly – verify that the requests your code sent match your expectations.
- DSL-first approach – both mocking and assertions are intuitive, readable, and powerful.
- Single unified API – interact with one DSL, regardless of whether your underlying implementation is Ktor, OkHttp, any other API library or custom implementation.
- **Serialization-agnostic core** – the main `http` artifact and the `HttpApythia` interface are completely independent from any serialization library.
- **Kotlin Multiplatform support** – works for Android, iOS, and JVM targets.

With Apythia, your API logic is predictable, testable, and clearly defined.

## Architecture

Apythia is fully modular and extensible. You pick only what you need.

### `http` (core module)

This is the heart of Apythia:
- Contains all HTTP-related abstractions and the DSL for mocking responses and asserting requests.
- Defines the `HttpApythia` abstract class, which serves as the main entry point for mocking and assertions.
- **No serialization dependency** — everything works with raw strings or bytes by default.
- Provides a **DSL extension configuration mechanism**:
  - When constructing any `HttpApythia`, you can provide configuration objects specific to each extension.
    For example, the JSON extension can accept a global `Json` instance or custom settings for all its operations, while other extensions can have their own separate configurations.
- Fully extensible — you can implement your own `HttpApythia` to connect Apythia to the mocking solution of your production HTTP client (e.g., Ktor’s `MockEngine`).

### Serialization Extensions

Apythia’s core is independent of serialization. Optional extensions allow seamless integration:

#### `http-ext-json-kotlinx-serialization`

Adds support for JSON bodies and JSON assertions using **Kotlinx Serialization**:
- Functions like `jsonObjectBody { ... }` and `jsonArrayBody { ... }` allow building response JSON bodies using Kotlinx Serialization DSL builders (`JsonObjectBuilder`, `JsonArrayBuilder`), providing type-safe, natural syntax.
- Provides similar DSLs for asserting JSON data in requests.
- **Optional** — include it only if your project uses Kotlinx Serialization.
- Can be used as a **reference** to implement your own DSL extensions for other formats or serialization libraries.

### Assertion DSL Data Access

Almost every main block in the assertion DSL (e.g., `body`, `headers`) gives you access 
to the **actual request data**, allowing you to write custom DSL extensions or implement additional checks on top of the provided DSL.

### HTTP Client Implementations

Apythia includes ready-made implementations of `HttpApythia` for popular clients:

#### `http-ktor`

Backed by Ktor client.
Ideal for projects using Ktor networking.
Includes the core `http` module transitively.

#### `http-okhttp`

Backed by OkHttp.
Ideal for Android/JVM projects.
Includes the core `http` module transitively.

### Implementing Your Own `HttpApythia`

Since `HttpApythia` is an abstract class, you can implement it to connect Apythia to the mocking solution 
of your production HTTP client (for example, Ktor’s `MockEngine` or your own client).
This allows you to use all of Apythia’s platform-independent DSL, mocking, and assertion features, 
while plugging in your own HTTP client for request execution.

## Setup

Use the BOM to ensure consistent and binary-compatible versions.

```toml
[versions]
apythia-bom = "SPECIFY_VERSION"

[libraries]
apythia-bom = { module = "io.github.ackeecz:apythia-bom", version.ref = "apythia-bom" }

# Core-only module — use ONLY if you implement your own HttpApythia
apythia-http = { module = "io.github.ackeecz:apythia-http" }

# For Ktor
apythia-http-ktor = { module = "io.github.ackeecz:apythia-http-ktor" }

# For OkHttp
apythia-http-okhttp = { module = "io.github.ackeecz:apythia-http-okhttp" }

# Optional JSON + Kotlinx Serialization support
apythia-http-ext-json-kotlinx-serialization = { module = "io.github.ackeecz:apythia-http-ext-json-kotlinx-serialization" }
```

### build.gradle.kts (JVM / Android)

```kotlin
dependencies {
    // Always use BOM
    testImplementation(platform(libs.apythia.bom))

    // Choose your HTTP implementation
    testImplementation(libs.apythia.http.ktor)
    // OR
    testImplementation(libs.apythia.http.okhttp)

    // Optional: JSON DSL extension
    testImplementation(libs.apythia.http.ext.json.kotlinx.serialization)

    // Only needed if writing your own HttpApythia
    // testImplementation(libs.apythia.http)
}

```

### build.gradle.kts (KMP)

```kotlin
commonTest {
    dependencies {
        implementation(platform(libs.apythia.bom))

        // Choose your HTTP implementation
        implementation(libs.apythia.http.ktor)

        // Optional JSON support
        implementation(libs.apythia.http.ext.json.kotlinx.serialization)

        // Core-only for custom implementation
        // implementation(libs.apythia.http)
    }
}
```

## Sample Usage

### Instantiating `HttpApythia`

```kotlin
private lateinit var httpApythia: HttpApythia
private lateinit var underTest: RemoteDataSource

class RemoteDataSourceImplTest : FunSpec({

    val ktorHttpApythia = KtorHttpApythia().also { httpApythia = it }

    beforeEach {
        ktorHttpApythia.beforeEachTest()
        val httpClient = HttpClient(ktorHttpApythia.mockEngine)
        underTest = RemoteDataSourceImpl(httpClient)
    }

    afterEach {
        ktorHttpApythia.afterEachTest()
    }

    // Then use HttpApythia interface in your tests. This will make them decoupled from the underlying HTTP client.
})
```
**Tip**: It’s recommended to create a JUnit rule or Kotest extension (depending on your testing framework) 
to automatically handle setup and teardown of `HttpApythia` instances, avoiding repetitive boilerplate in each test.

### Mocking DSL

```kotlin
httpApythia.mockNextResponse {
    statusCode(204)
    headers {
        header("X-Custom-Header", "customValue")
        header("Accept", "application/json")
    }
    jsonObjectBody {
        put("customKey", "customValue")
    }
}
```

### Assertion DSL

```kotlin
httpApythia.assertNextRequest {
    method(HttpMethod.GET)
    url {
        path("/api/v1/sample.php")
        query {
            parameter("param", 1)
            parameters("param2", listOf("value1", "value2"))
        }
    }
    body {
        jsonObject {
            put("key", "value")
        }
    }
}
```

### Configuring Kotlinx Serialization Json

```kotlin
KtorHttpApythia {
    kotlinxSerializationJsonConfig {
        allowTrailingComma = true
        // Add other settings as needed
    }
}
```

### Custom DSL Extension Configuration

```kotlin
KtorHttpApythia {
    // Provide configuration for third-party or custom extensions. For more info see
    // DslExtensionConfig documentation.
    dslExtensionConfig(...)
}
```

## Experimental Status

Some of Apythia’s APIs are marked as experimental.

This follows the standard Kotlin experimental / `@OptIn` conventions — meaning:
- These APIs are stable in behavior and safe to use in production.
- Their public API surface may evolve in future releases based on:
  - Real-world feedback
  - DSL improvements
  - New features or extension modules
  - Refinement of cross-platform abstractions

When using experimental APIs, you may need to update your code when upgrading to new versions, 
especially where the API is annotated with Kotlin’s experimental markers.

We welcome feedback from users — it helps guide the evolution of Apythia’s experimental features.

## Credits

Developed by [Ackee](https://www.ackee.cz) team with 💙.
