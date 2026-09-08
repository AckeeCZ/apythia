# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### http
#### Added
- `HttpApythia.mockNextDynamicResponse` for mocking a response that is built at the request time from
  the arriving request, which makes mocking of concurrent requests order-independent.
- `HttpApythia.mockNextDynamicResponses` for mocking the same dynamic response block multiple times.
- `HttpApythia.mockNextResponseProvider` implementation hook, which has to be overridden by
  `HttpApythia` implementations supporting dynamic responses. Its default implementation throws
  `IllegalStateException`.
- `ActualRequest.path`, `ActualRequest.queryParameters` and `ActualRequest.queryParameter` accessors
  of the decoded request URL data.

### http-ktor
#### Added
- Support for dynamic responses via `mockNextDynamicResponse`.

### http-okhttp
#### Added
- Support for dynamic responses via `mockNextDynamicResponse`.

#### Changed
- Responses are served by a custom mock web server dispatcher instead of the default queue one.
  Behaviour when no response is mocked is unchanged.

## BOM [1.0.2] - 2026-05-29
### http
#### Changed
- Updated build to Kotlin 2.3 and AGP 9.

### http-ext-json-kotlinx-serialization
#### Changed
- Updated `kotlinx.serialization` to 1.11.0.
- Updated dependency on `http` artifact.

### http-ktor
#### Changed
- Updated `ktor` to 3.5.0.
- Updated dependency on `http` artifact.

### http-okhttp
#### Changed
- Updated `okhttp` to 5.3.2.
- Updated dependency on `http` artifact.

## BOM [1.0.1] - 2025-12-15
### http
#### Fixed
- Encoded URL processing. `HttpApythia` now correctly handles encoded URLs and all URL assertions
now assert correctly only against decoded URLs.

### http-ext-json-kotlinx-serialization
#### Changed
- Updated dependency on `http` artifact.

### http-ktor
#### Changed
- Updated dependency on `http` artifact.

### http-okhttp
#### Changed
- Updated dependency on `http` artifact.

## BOM [1.0.0] - 2025-12-04
### http
#### Added
- First version of the artifact 🎉

### http-ext-json-kotlinx-serialization
#### Added
- First version of the artifact 🎉

### http-ktor
#### Added
- First version of the artifact 🎉

### http-okhttp
#### Added
- First version of the artifact 🎉
