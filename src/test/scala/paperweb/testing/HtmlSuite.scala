package paperweb.testing

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import org.http4s.{MediaType, Response}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

final case class RenderedHtml(response: Response[IO], document: Document)

abstract class HtmlSuite extends FunSuite:
  protected def html(response: Response[IO]): RenderedHtml =
    assertEquals(
      response.contentType.map(_.mediaType),
      Some(MediaType.text.html),
      "expected an HTML response"
    )
    RenderedHtml(response, Jsoup.parse(response.as[String].unsafeRunSync()))

  protected def required(
      document: Document | Element,
      selector: String
  ): Element =
    Option(document.selectFirst(selector)).getOrElse {
      fail(s"expected HTML element matching selector: $selector")
    }
