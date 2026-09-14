package io.estebangarcia21.paper.web.testing

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import org.http4s.{MediaType, Response}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

/** One consumed HTTP response together with its parsed HTML document. */
final case class RenderedHtml(response: Response[IO], document: Document)

/** MUnit support for asserting complete server-rendered http4s responses with jsoup. */
abstract class HtmlSuite extends FunSuite:

  /** Requires an HTML response and parses its body once. */
  protected def html(response: Response[IO]): RenderedHtml =
    assertEquals(
      response.contentType.map(_.mediaType),
      Some(MediaType.text.html),
      "expected an HTML response"
    )
    RenderedHtml(response, Jsoup.parse(response.as[String].unsafeRunSync()))

  /** Returns the single required element or fails with its selector. */
  protected def required(
      document: Document | Element,
      selector: String
  ): Element =
    Option(document.selectFirst(selector)).getOrElse {
      fail(s"expected HTML element matching selector: $selector")
    }
