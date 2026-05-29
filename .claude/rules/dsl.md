---
globs:
  - "http/**"
  - "http-ext-json-kotlinx-serialization/**"
  - "http-ktor/**"
  - "http-okhttp/**"
  - "http-testing/**"
---

# DSL Conventions

apythia is, mechanically, a builder DSL. Every public DSL surface follows these patterns.

## DslMarker per scope

Scope-isolating `@DslMarker` annotation per logical scope. `public`, `BINARY` retention.

```kotlin
@DslMarker
@Retention(AnnotationRetention.BINARY)
public annotation class HttpResponseDslMarker
```

Existing markers: `HttpResponseDslMarker`, `HttpRequestDslMarker`, `ConfigsDslMarker`.
Apply on the **interface**, not the impl.

## Public interface + internal impl split

Every DSL surface has a `public interface` and an `internal class *Impl`. Consumers only see the interface.

```kotlin
@HttpResponseDslMarker
public interface HttpResponseMockBuilder : DslExtensionConfigProvider {

    public fun statusCode(code: Int)
    public fun headers(mockHeaders: HeadersMockBuilder.() -> Unit)
}

internal class HttpResponseMockBuilderImpl(
    private val dslExtensionConfigProvider: DslExtensionConfigProvider,
) : HttpResponseMockBuilder, DslExtensionConfigProvider by dslExtensionConfigProvider {
    // ...
}
```

## DSL constraint enforcement

Constraints on builder usage are enforced at runtime via dedicated checkers, not by clever types.

- `CallCountChecker` — caps how many times a builder method may be called (e.g. `statusCode` once, `body` once).
- `MutualExclusivityChecker` — ensures only one of a set of mutually exclusive options is picked.

```kotlin
private val statusCodeCallCountChecker = CallCountChecker("statusCode", maxCallCount = 1)
```

When adding a new builder method, decide explicitly: is it one-shot, repeatable, or mutually exclusive with siblings? Wire the appropriate checker.

## `@ExperimentalHttpApi` gating

Mark evolving / not-fully-settled public API with `@ExperimentalHttpApi`. The annotation is **auto-opted-in** for apythia source via `compilerOptions.optIn`, so internal code uses it freely. External users must opt in themselves.

```kotlin
@RequiresOptIn(
    message = "This API is experimental and may change in future releases.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalHttpApi
```

Use it on:

- DSL members whose shape may still change (e.g. `statusCode`, `bytesBody`, `plainTextBody`).
- Extension points (`DslExtensionConfig`).

## DSL extension config mechanism

Optional extensions (like `http-ext-json-kotlinx-serialization`) plug in via `DslExtensionConfig`:

```kotlin
@ExperimentalHttpApi
public interface DslExtensionConfig

public abstract class HttpApythia(
    dslExtensionConfigs: DslExtensionConfigs.() -> Unit,
)
```

The config is then surfaced on scopes like `BodyAssertion` / `HttpResponseMockBuilder` via `DslExtensionConfigProvider`, so extension methods read their configuration without forcing callers to thread it through every call site. New DSL extensions in separate modules should follow `http-ext-json-kotlinx-serialization` as the reference.

## Builders vs Assertions

Response side is **mocking** → `*MockBuilder` types (`HttpResponseMockBuilder`, `HeadersMockBuilder`).
Request side is **verification** → `*Assertion` types (`HttpRequestAssertion`, `BodyAssertion`, `HeadersAssertion`, `UrlAssertion`).

Don't mix the verbs — a builder *configures*, an assertion *checks*.
