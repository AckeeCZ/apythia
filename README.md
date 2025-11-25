[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ackeecz/apythia-bom)](https://central.sonatype.com/artifact/io.github.ackeecz/apythia-bom)

# Apythia

In ancient times, the Oracle of Delphi—Pythia—revealed truths hidden to mortal eyes. Today, your 
API calls are the hidden realm, filled with subtle mistakes, unmocked responses, and unexpected behaviors.

Enter Apythia – your magical oracle for API communication, revealing hidden truths in your 
Kotlin Multiplatform projects through mocking and assertions — all while **hiding the underlying 
API implementation**.

## 🧙 What Apythia Does

Apythia helps you test your API/network logic confidently with a clear, expressive DSL:
- Mock API responses easily – define exactly how your API client should respond.
- Assert requests elegantly – verify that the requests your code sent match your expectations.
- DSL-first approach – both mocking and assertions are intuitive, readable, and powerful.
- Single unified API – interact with one DSL, regardless of whether your underlying implementation is Ktor, OkHttp, any other API library or custom implementation.
- Kotlin Multiplatform support – works for Android, iOS, and JVM targets.

With Apythia, your API logic is predictable, testable, and clearly defined.

## Architecture

Library consists of several modules:
- `http` contains HTTP-related code that is not tied to a specific library
- `http-ktor` contains `http` implementation backed by Ktor
- `http-okhttp` contains `http` implementation backed by OkHttp

### http
# TODO Add description

### http-ktor
# TODO Add description

### http-okhttp
# TODO Add description

## Setup

Add the following dependencies to your `libs.versions.toml`, depending on what you need. You should
always use BOM to be sure to get binary compatible dependencies.

```toml
[versions]
apythia-bom = "SPECIFY_VERSION"

[libraries]
apythia-bom = { module = "io.github.ackeecz:apythia-bom", version.ref = "apythia-bom" }
# For Ktor
apythia-http-ktor = { module = "io.github.ackeecz:apythia-http-ktor" }
# For OkHttp
apythia-http-okhttp = { module = "io.github.ackeecz:apythia-http-okhttp" }
```

Then specify dependencies in your `build.gradle.kts`:

Android and JVM modules:

```kotlin
dependencies {

    // Always use BOM
    testImplementation(platform(libs.apythia.bom))
    // For Ktor
    testImplementation(libs.apythia.http.ktor)
    // For OkHttp
    testImplementation(libs.apythia.http.okhttp)
}
```

KMP modules:

```kotlin
commonTest {
    dependencies {
        // Always use BOM
        implementation(dependencies.platform(libs.apythia.bom))
        // For Ktor
        implementation(libs.apythia.http.ktor)
    }
}
```

## Credits

Developed by [Ackee](https://www.ackee.cz) team with 💙.
