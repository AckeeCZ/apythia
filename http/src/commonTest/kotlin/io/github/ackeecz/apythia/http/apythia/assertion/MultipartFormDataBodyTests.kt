package io.github.ackeecz.apythia.http.apythia.assertion

import io.github.ackeecz.apythia.http.apythia.HttpApythiaTest
import io.github.ackeecz.apythia.http.request.body.ActualPart
import io.github.ackeecz.apythia.http.request.body.createActualFormDataPart
import io.github.ackeecz.apythia.http.request.dsl.body.BodyAssertion
import io.github.ackeecz.apythia.http.request.dsl.body.MultipartFormDataAssertion
import io.github.ackeecz.apythia.testing.http.shouldFail
import io.github.ackeecz.apythia.testing.http.shouldNotFail
import io.kotest.core.spec.style.scopes.FunSpecContainerScope

internal suspend fun FunSpecContainerScope.multipartFormDataBodyTests(
    fixture: HttpApythiaTest.Fixture,
) = with(fixture) {
    context("multipart/form-data") {
        context("part") {
            commonFormDataPartTestSuite(fixture) { multipartFormData(it) }

            test("failure when one part is extra") {
                val expectedName = "name"
                underTest.actualParts = listOf(
                    createActualFormDataPart(name = expectedName),
                    createActualFormDataPart(name = "$expectedName extra"),
                )

                shouldFail {
                    underTest.assertNextRequest {
                        body {
                            multipartFormData {
                                part(expectedName) {}
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun FunSpecContainerScope.commonFormDataPartTestSuite(
    fixture: HttpApythiaTest.Fixture,
    callMultipart: suspend BodyAssertion.(suspend MultipartFormDataAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    test("failure when name is different") {
        val expectedName = "name"
        underTest.actualParts = listOf(createActualFormDataPart(name = "$expectedName failure"))

        shouldFail {
            underTest.assertNextRequest {
                body {
                    callMultipart {
                        part(name = expectedName) {}
                    }
                }
            }
        }
    }

    suspend fun filenameFailureTest(actual: String?, expected: String?) {
        underTest.actualParts = listOf(createActualFormDataPart(filename = actual))

        shouldFail {
            underTest.assertNextRequest {
                body {
                    callMultipart {
                        part(name = "name", filename = expected) {}
                    }
                }
            }
        }
    }

    test("failure when filename is different") {
        filenameFailureTest(actual = "actual_filename", expected = "expected_filename")
    }

    test("failure when actual filename is null but expected non-null") {
        filenameFailureTest(actual = null, expected = "expected_filename")
    }

    test("failure when actual filename is not null but expected null") {
        filenameFailureTest(actual = "actual_filename", expected = null)
    }

    test("failure when one part is missing") {
        val expectedName1 = "name1"
        val expectedName2 = "name2"
        underTest.actualParts = listOf(createActualFormDataPart(name = expectedName1))

        shouldFail {
            underTest.assertNextRequest {
                body {
                    callMultipart {
                        part(expectedName1) {}
                        part(expectedName2) {}
                    }
                }
            }
        }
    }

    test("failure when there are parts with the same name and one actual is not equal to any expected") {
        val expectedName = "name"
        val expectedFilename1 = "filename1"
        val expectedFilename2 = "filename2"
        underTest.actualParts = listOf(
            createActualFormDataPart(name = expectedName, filename = null),
            createActualFormDataPart(name = expectedName, filename = expectedFilename1),
            createActualFormDataPart(name = expectedName, filename = "bad filename"),
        )

        shouldFail {
            underTest.assertNextRequest {
                body {
                    callMultipart {
                        part(name = expectedName, filename = expectedFilename1) {}
                        part(name = expectedName, filename = null) {}
                        part(name = expectedName, filename = expectedFilename2) {}
                    }
                }
            }
        }
    }

    test("success when there are parts with different names") {
        val expectedName1 = "name1"
        val expectedName2 = "name2"
        val expectedFilename2 = "filename2"
        underTest.actualParts = listOf(
            createActualFormDataPart(name = expectedName1, filename = null),
            createActualFormDataPart(name = expectedName2, filename = expectedFilename2),
        )

        shouldNotFail {
            underTest.assertNextRequest {
                body {
                    callMultipart {
                        part(name = expectedName1) {}
                        part(name = expectedName2, filename = expectedFilename2) {}
                    }
                }
            }
        }
    }

    test("success when there are parts with the same name in the same expected order") {
        val expectedName = "name"
        val expectedFilename = "filename"
        underTest.actualParts = listOf(
            createActualFormDataPart(name = expectedName, filename = null),
            createActualFormDataPart(name = expectedName, filename = expectedFilename),
        )

        shouldNotFail {
            underTest.assertNextRequest {
                body {
                    callMultipart {
                        part(name = expectedName, filename = null) {}
                        part(name = expectedName, filename = expectedFilename) {}
                    }
                }
            }
        }
    }

    test("success when there are parts with the same name in the different order than expected") {
        val expectedName = "name"
        val expectedFilename = "filename"
        underTest.actualParts = listOf(
            createActualFormDataPart(name = expectedName, filename = null),
            createActualFormDataPart(name = expectedName, filename = expectedFilename),
        )

        shouldNotFail {
            underTest.assertNextRequest {
                body {
                    callMultipart {
                        part(name = expectedName, filename = expectedFilename) {}
                        part(name = expectedName, filename = null) {}
                    }
                }
            }
        }
    }

    test("success when there are parts with the same name but different headers and in the different order than expected") {
        val expectedName = "name"
        val firstExpectedHeader = "Content-Type" to listOf("text/plain")
        val secondExpectedHeader = "Content-Type" to listOf("application/octet-stream")
        underTest.actualParts = listOf(
            createActualFormDataPart(name = expectedName, headers = mapOf(firstExpectedHeader)),
            createActualFormDataPart(name = expectedName, headers = mapOf(secondExpectedHeader)),
        )

        shouldNotFail {
            underTest.assertNextRequest {
                body {
                    callMultipart {
                        part(name = expectedName) {
                            headers {
                                headers(secondExpectedHeader.first, secondExpectedHeader.second)
                            }
                        }
                        part(name = expectedName) {
                            headers {
                                headers(firstExpectedHeader.first, firstExpectedHeader.second)
                            }
                        }
                    }
                }
            }
        }
    }

    test("success when there are parts with the same name but different bodies and in the different order than expected") {
        val expectedName = "name"
        val firstExpectedBody = byteArrayOf(1, 2, 3)
        val secondExpectedBody = byteArrayOf(4, 5, 6)
        underTest.actualParts = listOf(
            createActualFormDataPart(name = expectedName, body = firstExpectedBody),
            createActualFormDataPart(name = expectedName, body = secondExpectedBody),
        )

        shouldNotFail {
            underTest.assertNextRequest {
                body {
                    callMultipart {
                        part(name = expectedName) {
                            body {
                                bytes(secondExpectedBody)
                            }
                        }
                        part(name = expectedName) {
                            body {
                                bytes(firstExpectedBody)
                            }
                        }
                    }
                }
            }
        }
    }

    formDataPartHeadersTests(fixture, callMultipart)
    formDataPartBodyTests(fixture, callMultipart)
}

private suspend fun FunSpecContainerScope.formDataPartHeadersTests(
    fixture: HttpApythiaTest.Fixture,
    callMultipart: suspend BodyAssertion.(suspend MultipartFormDataAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    val partName = "name"
    headersTestSuite(
        fixture = fixture,
        arrangeHeaders = {
            val part = createActualFormDataPart(name = partName, headers = it)
            underTest.actualParts = listOf(part)
        },
        assertHeaders = { headersAssertion ->
            body {
                callMultipart {
                    part(partName) {
                        headers(headersAssertion)
                    }
                }
            }
        },
    )
}

private suspend fun FunSpecContainerScope.formDataPartBodyTests(
    fixture: HttpApythiaTest.Fixture,
    callMultipart: suspend BodyAssertion.(suspend MultipartFormDataAssertion.() -> Unit) -> Unit,
) = with(fixture) {
    val partName = "name"

    fun createOrUpdatePart(update: (ActualPart) -> ActualPart) {
        val part = underTest.actualParts.firstOrNull() ?: createActualFormDataPart(name = partName)
        underTest.actualParts = listOf(update(part))
    }

    bodyTestSuite(
        fixture = fixture,
        arrangeHeaders = { headers ->
            createOrUpdatePart {
                println("current headers: ${it.headers}")
                println("new headers: $headers")
                it.copy(headers = it.headers + headers)
            }
        },
        arrangeBody = { body ->
            println("arrangeBody: ${body.toHexString()}")
            createOrUpdatePart { it.copy(body = body) }
            println("name: ${underTest.actualParts.firstOrNull()?.headers?.get("Content-Disposition")?.firstOrNull()}")
            println("actualParts: ${underTest.actualParts.first().body.toHexString()}")
        },
        assertBody = { bodyAssertion ->
            body {
                callMultipart {
                    part(partName) {
                        body(bodyAssertion)
                    }
                }
            }
        },
    )
}

internal suspend fun FunSpecContainerScope.partialMultipartFormDataBodyTests(
    fixture: HttpApythiaTest.Fixture
) = with(fixture) {
    context("partial multipart/form-data") {
        context("part") {
            commonFormDataPartTestSuite(fixture) { partialMultipartFormData(it) }
        }

        context("missing parts") {
            test("not asserted when no missing parts expected") {
                val partName = "name"
                underTest.actualParts = listOf(createActualFormDataPart(name = partName))

                shouldNotFail {
                    underTest.assertNextRequest {
                        body {
                            partialMultipartFormData {
                                part(partName) {}
                            }
                        }
                    }
                }
            }

            test("failure when part is not missing") {
                val missingName1 = "name1"
                val missingName2 = "name2"
                underTest.actualParts = listOf(
                    createActualFormDataPart(name = "$missingName1 other"),
                    createActualFormDataPart(name = missingName2),
                    createActualFormDataPart(name = "$missingName2 other"),
                )

                shouldFail {
                    underTest.assertNextRequest {
                        body {
                            partialMultipartFormData {
                                missingParts(missingName1, missingName2)
                            }
                        }
                    }
                }
            }

            test("success when all parts are missing") {
                val missingName1 = "name1"
                val missingName2 = "name2"
                underTest.actualParts = listOf(
                    createActualFormDataPart(name = "not missing 1"),
                    createActualFormDataPart(name = "not missing 2"),
                )

                shouldNotFail {
                    underTest.assertNextRequest {
                        body {
                            partialMultipartFormData {
                                missingParts(missingName1, missingName2)
                            }
                        }
                    }
                }
            }
        }
    }
}
