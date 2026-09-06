package paperweb.testing

import cats.effect.IO
import org.http4s.{MediaType, Response, Status}
import org.http4s.headers.`Content-Type`

class HtmlSuiteSuite extends HtmlSuite:
  test("html parses an HTML response and required selects an element"):
    val response = Response[IO](Status.Ok)
      .withEntity(
        "<!doctype html><html><body><main><h1>Paperweb</h1></main></body></html>"
      )
      .withContentType(`Content-Type`(MediaType.text.html))

    val page = html(response)

    assertEquals(page.response.status, Status.Ok)
    assertEquals(required(page.document, "main h1").text(), "Paperweb")

  test("required reports a missing selector"):
    val response = Response[IO](Status.Ok)
      .withEntity("<main></main>")
      .withContentType(`Content-Type`(MediaType.text.html))
    val page = html(response)

    val error = intercept[munit.FailException] {
      required(page.document, "form.import-form")
    }

    assert(error.getMessage.contains("form.import-form"))
