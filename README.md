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
- Kotlin Multiplatform support – works on Android and iOS projects.

With Apythia, your API logic is predictable, testable, and clearly defined.

## Architecture

Library consists of several modules:
- `http` contains HTTP-related code that is not tied to a specific library

### http
# TODO Add description

## Setup

Add the following dependencies to your `libs.versions.toml`, depending on what you need. You should
always use BOM to be sure to get binary compatible dependencies.

```toml
[versions]
apythia-bom = "SPECIFY_VERSION"

[libraries]
apythia-bom = { module = "io.github.ackeecz:apythia-bom", version.ref = "apythia-bom" }
apythia-http = { module = "io.github.ackeecz:apythia-http" }
```

Then specify dependencies in your `build.gradle.kts`:

```kotlin
dependencies {

    // Always use BOM
    implementation(platform(libs.apythia.bom))
    implementation(libs.apythia.http)
}
```

## Credits

Developed by [Ackee](https://www.ackee.cz) team with 💙.
