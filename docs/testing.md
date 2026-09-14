# Testing HTML

Paperweb provides `io.estebangarcia21.paper.web.testing.HtmlSuite` in the `paperwebtestkit` artifact.
It keeps server-rendered page tests at the http4s route boundary while using jsoup to inspect the
returned document instead of matching serialized HTML strings. The sbt plugin adds the matching
testkit automatically; consumers do not need a separate test dependency.

Extend `HtmlSuite`, execute the route normally, and pass the response to `html`. This verifies the
HTML content type, consumes the response body once, and returns both the response and parsed jsoup
document. Use `required` when exactly one important element must exist; its failure includes the CSS
selector.

```scala
import io.estebangarcia21.paper.web.testing.HtmlSuite

class GoalsSuite extends HtmlSuite:
  test("GET /goals renders the goal form"):
    val response = routes.orNotFound.run(Request[IO](GET, uri"/goals")).unsafeRunSync()
    val page = html(response)

    assertEquals(page.response.status, Status.Ok)
    assertEquals(required(page.document, "h1").text(), "Financial Goals")

    val form = required(page.document, "form.goal-form")
    assertEquals(form.attr("method"), "post")
    assertEquals(form.attr("action"), "/goals")
```

Prefer selectors based on semantic elements, accessible attributes, and domain component classes.
Assert status codes, redirects, cookies, and other HTTP behavior directly on the response. Use DOM
assertions for document metadata, meaningful content, links, forms, and representative rendered
data. Avoid asserting the complete serialized document, attribute ordering, or incidental wrapper
structure.

These tests do not execute JavaScript. Add a small browser-level test only when behavior implemented
by HTMX, Alpine, or another browser API cannot be established at the route/document boundary.

The old `paperweb.testing.HtmlSuite` name remains as a compatibility facade.
