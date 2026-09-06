# Testing HTML

Paperweb provides `paperweb.testing.HtmlSuite` on its test classpath. It keeps server-rendered page
tests at the http4s route boundary while using jsoup to inspect the returned document instead of
matching serialized HTML strings. Consumer projects must depend on Paperweb's test configuration:

```scala
.dependsOn(paperweb % "compile->compile;test->test")
```

Extend `HtmlSuite`, execute the route normally, and pass the response to `html`. This verifies the
HTML content type, consumes the response body once, and returns both the response and parsed jsoup
document. Use `required` when exactly one important element must exist; its failure includes the CSS
selector.

```scala
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
