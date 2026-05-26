---
globs:
  - "**/src/**Test/**"
  - "http-testing/**"
---

# Testing Conventions

## Framework

- **Kotest `FunSpec` only** — no other spec styles (BehaviorSpec, StringSpec, etc.). Verified across all 27 test files in the repo.
- Test style: `test("description") { ... }` blocks inside the spec constructor.

## System Under Test

- Always name the instance under test `underTest` — never `sut`, `apythia`, `client`, etc.
- For most tests: declare `underTest` + dependencies as `private lateinit var` at **file level** (not inside `init` or `beforeEach`); recreate them in `beforeEach`.

```kotlin
private lateinit var httpApythia: HttpApythiaMock
private lateinit var underTest: HttpResponseMockBuilderImpl

internal class HttpResponseMockBuilderImplTest : FunSpec({

    beforeEach {
        httpApythia = HttpApythiaMock()
        underTest = HttpResponseMockBuilderImpl(
            dslExtensionConfigProvider = httpApythia,
        )
    }

    test("status code defaults to 200") {
        underTest.httpResponse.statusCode shouldBe 200
    }
})
```

## Reusable test suites

Cross-impl invariants (core, Ktor, OkHttp, future impls) are tested by **shared suites declared as extension functions on `FunSpecContainerScope`**:

```kotlin
internal suspend fun FunSpecContainerScope.bodyTestSuite(
    fixture: HttpApythiaTest.Fixture,
    arrangeHeaders: (Map<String, List<String>>) -> Unit,
    arrangeBody: (ByteArray) -> Unit,
    assertBody: suspend HttpRequestAssertion.(BodyAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    context("body") {
        actualBodyTests(fixture, arrangeHeaders, arrangeBody, assertBody)
        emptyBodyTests(fixture, arrangeBody, assertBody)
        // ...
    }
}
```

Suites are composed from test classes via `context { bodyTestSuite(fixture, ...) }`. When adding behaviour that must hold for every `HttpApythia` impl, write it as a suite under `:http/commonTest/...` (or `:http-testing`), not as a one-off test in a single impl.

## `BaseHttpApythiaImplTest` — the contract test

`public abstract class BaseHttpApythiaImplTest<Sut : HttpApythia> : FunSpec()` in `:http-testing` is the conformance test every `HttpApythia` impl must pass. It's **public** because external developers integrating their own HTTP client run it against their own impl.

Changes here are public API changes — regenerate the `:http-testing` `.api` dump and bump its version when releasing.

## Test doubles

- **Hand-written only** — no MockK, no Mockito.
- Suffix naming:
  - `*Mock` — controllable double with state (e.g. `HttpApythiaMock`, `DslExtensionConfigMock`).
  - `*Stub` — fixed-response stand-in.
- Shared doubles live in `:http-testing` (e.g. `HttpApythiaMock` is `public` and exposed to consumers).
- One-off doubles live next to the test that uses them.

## Test class visibility

- Test classes are `internal class FooTest` — keeps them out of the public API.
- Exception: `BaseHttpApythiaImplTest` and its enabler types (`RemoteDataSource`, factories) are `public` because they're consumer-facing.

## KMP test source sets

Tests are written per source set as appropriate:

- `commonTest` — KMP-shared tests; rely on Kotest only, no platform engines.
- `androidUnitTest`, `jvmTest`, `iosX64Test`, `iosArm64Test`, `iosSimulatorArm64Test` — platform-specific.

When a test in a KMP module requires a platform engine (e.g. an OkHttp `MockEngine` in `:http-okhttp`), keep it in the platform-specific source set instead of `commonTest`.
