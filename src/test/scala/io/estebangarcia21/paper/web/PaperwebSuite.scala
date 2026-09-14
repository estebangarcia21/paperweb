package io.estebangarcia21.paper.web

import org.jsoup.Jsoup
import scalatags.Text.all._
import scalatags.Text.tags2.{main => mainTag}

class PaperwebSuite extends munit.FunSuite {

  test("asset version is added as a query parameter") {
    assertEquals(
      Paperweb.versionAssetUrl("/assets/app.css", "1234"),
      "/assets/app.css?v=1234"
    )
  }

  test("asset version preserves an existing query and fragment") {
    assertEquals(
      Paperweb.versionAssetUrl("/assets/app.css?theme=dark#colors", "1234"),
      "/assets/app.css?theme=dark&v=1234#colors"
    )
  }

  test("configured documents render global assets before page assets") {
    val renderer = PageRenderer(
      DocumentConfig(
        language = "es",
        globalAssets = Assets(
          css = List("/assets/global.css"),
          js = List("/assets/global.js")
        ),
        favicon = Some(Favicon("/assets/favicon.svg", "image/svg+xml"))
      )
    )
    val view = renderer.page[String](_ =>
      PageMeta(
        title = "Example",
        assets = Assets(
          css = List("/assets/page.css"),
          js = List("/assets/page.js")
        ),
        robots = Some("noindex"),
        description = Some("Description"),
        canonicalUrl = Some("https://example.test/page"),
        openGraph = Some(
          OpenGraph(
            title = "Card",
            description = "Description",
            url = "https://example.test/page",
            image = Some("https://example.test/card.png")
          )
        )
      )
    )(value => mainTag(h1(value)))

    val document = Jsoup.parse(view("Hello").render)

    assertEquals(document.selectFirst("html").attr("lang"), "es")
    assertEquals(
      document.select("link[rel=stylesheet]").eachAttr("href").toArray.toList,
      List("/assets/global.css", "/assets/page.css")
    )
    assertEquals(
      document.select("script[type=module]").eachAttr("src").toArray.toList,
      List("/assets/global.js", "/assets/page.js")
    )
    assertEquals(document.selectFirst("link[rel=icon]").attr("type"), "image/svg+xml")
    assertEquals(
      document.selectFirst("meta[property=og:image]").attr("content"),
      "https://example.test/card.png"
    )
  }

  test("the default renderer does not assume application assets") {
    val document = Jsoup.parse(
      page[Unit](_ => PageMeta("Minimal"))(_ => mainTag(h1("Minimal")))(()).render
    )

    assertEquals(document.select("link[rel=stylesheet]").size(), 0)
    assertEquals(document.select("link[rel=icon]").size(), 0)
  }

  test("the original paperweb namespace retains its stylesheet and favicon defaults") {
    val document = Jsoup.parse(
      _root_.paperweb.Layout
        .render(_root_.paperweb.PageMeta("Compatibility"), mainTag(h1("Compatibility")))
        .render
    )

    assertEquals(document.selectFirst("link[rel=stylesheet]").attr("href"), "/assets/app.css")
    assertEquals(document.selectFirst("link[rel=icon]").attr("href"), "/assets/images/favicon.png")
  }

}
