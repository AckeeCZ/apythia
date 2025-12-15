package io.github.ackeecz.apythia.http.request

import com.eygraber.uri.decodeUri
import io.github.ackeecz.apythia.testing.http.request.createActualRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ActualRequestTest : FunSpec({

    test("parse url with encoded spaces as %20") {
        // Arrange
        val encodedSpace = "%20"
        val encodedHost = "examp${encodedSpace}le.com"
        val encodedPath = "/pa${encodedSpace}th"
        val encodedQuery = "pa${encodedSpace}ram=va${encodedSpace}lue"
        val encodedFragment = "fragme${encodedSpace}nt"
        val encodedUrl = "https://$encodedHost$encodedPath?$encodedQuery#$encodedFragment"

        val decodedHost = "examp le.com"
        val decodedPath = "/pa th"
        val decodedQuery = "pa ram=va lue"
        val decodedFragment = "fragme nt"
        val expected = "https://$decodedHost$decodedPath?$decodedQuery#$decodedFragment"

        // Act
        val actualUrl = createActualRequest(url = encodedUrl).url

        // Assert
        actualUrl.encodedAuthority shouldBe encodedHost
        actualUrl.encodedPath shouldBe encodedPath
        actualUrl.encodedQuery shouldBe encodedQuery
        actualUrl.encodedFragment shouldBe encodedFragment
        actualUrl.toString() shouldBe encodedUrl

        actualUrl.host shouldBe decodedHost
        actualUrl.path shouldBe decodedPath
        actualUrl.query shouldBe decodedQuery
        actualUrl.fragment shouldBe decodedFragment
        actualUrl.toString().decodeUri() shouldBe expected
    }

    test("parse url with all + encoded") {
        // Arrange
        val encodedPlus = "%2B"
        val encodedHost = "examp${encodedPlus}le.com"
        val encodedPath = "/pa${encodedPlus}th"
        val encodedQuery = "pa${encodedPlus}ram=va${encodedPlus}lue"
        val encodedFragment = "fragme${encodedPlus}nt"
        val encodedUrl = "https://$encodedHost$encodedPath?$encodedQuery#$encodedFragment"

        val decodedHost = "examp+le.com"
        val decodedPath = "/pa+th"
        val decodedQuery = "pa+ram=va+lue"
        val decodedFragment = "fragme+nt"
        val expected = "https://$decodedHost$decodedPath?$decodedQuery#$decodedFragment"

        // Act
        val actualUrl = createActualRequest(url = encodedUrl).url

        // Assert
        actualUrl.encodedAuthority shouldBe encodedHost
        actualUrl.encodedPath shouldBe encodedPath
        actualUrl.encodedQuery shouldBe encodedQuery
        actualUrl.encodedFragment shouldBe encodedFragment
        actualUrl.toString() shouldBe encodedUrl

        actualUrl.host shouldBe decodedHost
        actualUrl.path shouldBe decodedPath
        actualUrl.query shouldBe decodedQuery
        actualUrl.fragment shouldBe decodedFragment
        actualUrl.toString().decodeUri() shouldBe expected
    }

    test("parse url with all + literal except query where + represents encoded space") {
        // Arrange
        val expectedHost = "examp+le.com"
        val expectedPath = "/pa+th"
        val expectedFragment = "fragme+nt"

        val encodedQuery = "pa%2Bra+m=va%2Blu+e"
        val encodedUrl = "https://$expectedHost$expectedPath?$encodedQuery#$expectedFragment"

        val expectedQuery = "pa+ra m=va+lu e"
        val expectedUrl = "https://$expectedHost$expectedPath?$expectedQuery#$expectedFragment"

        // Act
        val actualUrl = createActualRequest(url = encodedUrl).url

        // Assert
        actualUrl.host shouldBe expectedHost
        actualUrl.path shouldBe expectedPath
        actualUrl.query shouldBe expectedQuery
        actualUrl.fragment shouldBe expectedFragment
        actualUrl.toString().decodeUri() shouldBe expectedUrl
    }
})
