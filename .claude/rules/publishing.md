---
globs:
  - "lib.properties"
  - "CHANGELOG.md"
  - "RELEASING.md"
  - "bom/**"
  - "build-logic/logic/src/main/kotlin/io/github/ackeecz/apythia/verification/**"
  - "build-logic/logic/src/main/kotlin/io/github/ackeecz/apythia/plugin/PublishingPlugin.kt"
  - ".github/workflows/deploy.yml"
---

# Publishing

Source of truth for the release procedure: `RELEASING.md`. This file is the mental model Claude should use when reasoning about a change that touches publishing.

## Versioning model

- Every artifact has an **independent** version in `lib.properties` (`HTTP_VERSION`, `HTTP_KTOR_VERSION`, `BOM_VERSION`, …).
- The BOM pins a compatible set of artifact versions.
- Artifacts can be released individually, but `verifyPublishing` forces co-release whenever an internal-only module had breaking changes — because internal-API breaks can corrupt binary compat between artifacts that link against the same internal.

## Release Gradle tasks

| Task | Purpose |
|---|---|
| `checkIfUpdateNeededSinceCurrentTag` | List artifacts whose code changed since the last tag |
| `verifyPublishing` | Fail if internal-module breakage forces a co-release |
| `verifyBomVersion` | Fail if BOM version in `lib.properties` doesn't match the pushed git tag |
| `prePublishCheck` | Aggregated check — runs everything CI does on tag push. Run locally before pushing the tag |
| `updateLegacyAbi` | Regenerate `.api` dumps. Required whenever public API changes (see [CLAUDE.md → ABI Validation](../CLAUDE.md)) |

## Release flow

See `RELEASING.md` and its `TLDR version` release flow steps.

## ABI dumps before release

A public API change without a regenerated `.api` dump fails CI. See CLAUDE.md → ABI Validation for the broader workflow.

## Publishing skipping

`PublishingPlugin` probes Maven Central before publishing:

- **404** → publish.
- **2xx** → skip (artifact at this version already exists).
- **Anything else** → fail.

Consequence: re-pushing the same tag is safe (publishes nothing new). Don't try to "force re-publish" by hand — bump the version instead.

## Maven coordinates

`io.github.ackeecz:apythia-*` — artifact IDs come from `lib.properties` (`HTTP_ARTIFACT_ID`, etc.). Group ID is `io.github.ackeecz`.

## Dokka + signing

- `com.vanniktech.maven.publish` plugin handles publishing; Dokka generates Javadoc.
- `signAllPublications()` requires GPG secrets — provided by CI only. Local publishing without those secrets will fail signing; that's expected.
