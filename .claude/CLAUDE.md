# CLAUDE.md — apythia

## Project Overview

Published Kotlin Multiplatform testing library for HTTP API mocking and assertions.
The `HttpApythia` DSL lets consumers mock HTTP responses and assert outgoing requests
in a serialization-agnostic, client-agnostic way (with first-class Ktor + OkHttp adapters).

- Root package: `io.github.ackeecz.apythia`
- Maven coordinates: `io.github.ackeecz:apythia-*` (see `lib.properties`)
- Build tooling: convention plugins in `build-logic/`, Gradle Version Catalogs
- **Public API is the deliverable** — every `.api` dump under `<module>/api/` is part of the contract

## Module Structure

```
:bom                                    — BOM artifact (apythia-bom); pins compatible versions
:http                                   — Core: HttpApythia abstract class + DSL; serialization-agnostic
:http-ext-json-kotlinx-serialization    — Optional JSON DSL extensions backed by Kotlinx Serialization
:http-ktor                              — Ktor-backed HttpApythia impl
:http-okhttp                            — OkHttp-backed HttpApythia impl
:http-testing                           — Shared test infra: BaseHttpApythiaImplTest, HttpApythiaMock, factories
:sample-app                             — Internal sample showing library usage; not published
```

## KMP Targets & Compiler Configuration

Targets (per `KmpLibraryPlugin`): Android, JVM, iosX64, iosArm64, iosSimulatorArm64.

Compiler flags applied project-wide:

- `allWarningsAsErrors = true` — even a deprecation warning fails the build.
- `explicitApi()` — every public declaration needs an explicit visibility modifier.
- `-Xconsistent-data-class-copy-visibility` — `internal` data classes get `internal` `copy()`.
- Auto opt-in: `io.github.ackeecz.apythia.http.ExperimentalHttpApi` is added to `optIn`, so apythia's own code never needs `@OptIn(ExperimentalHttpApi::class)`. External users still must opt in.

## ABI Validation

Built-in Kotlin ABI validation (not the BCV plugin) is enabled by default on every KMP library module.
Public API dumps live at `<module>/api/` and are committed. Any signature change to a `public` declaration must be reflected there.

Workflow when public API changes:

1. `./gradlew updateLegacyAbi` to regenerate dumps.
2. Commit the dumps with the code change.

If the intent is a public API change, treat the `.api` diff as part of the review. If the dump didn't move, the change wasn't public.

## Convention Plugins (`build-logic/`)

| Alias | Purpose |
|---|---|
| `apythia.kotlin.multiplatform.library` | KMP library (Android + iOS + JVM), explicit API, ABI validation, Detekt |
| `apythia.kotlin.multiplatform.library-with-testing` | Above + Kotest test deps |
| `apythia.kotlin.jvm.library` | JVM-only library |
| `apythia.kotlin.jvm.library-with-testing` | JVM-only library + Kotest |
| `apythia.android.application` | Android app (sample-app only) |
| `apythia.publishing` | Maven Central publishing + `verifyPublishing` / `checkIfUpdateNeededSinceCurrentTag` |
| `apythia.preflightchecks` | Registers `prePublishCheck` aggregating release-time checks |

## Code Style

- Always put a blank line after a type body opening brace and before the first member. Applies to `class`, `interface`, `sealed class`, `object`, `enum class` — any construct that declares a type with a `{ }` body block.
- **Does NOT apply** to constructor parameter lists `(...)` or lambda/DSL bodies (e.g. `module { }`, `launch { }`, `test { }`).

## Plans

At the end of each plan, give me a list of unresolved questions to answer, if any. Make the questions extremely concise. Sacrifice grammar for the sake of concision. Use AskUserQuestionTool.
