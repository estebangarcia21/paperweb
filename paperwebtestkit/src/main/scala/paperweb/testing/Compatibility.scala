package paperweb.testing

/** Compatibility name for Paperweb's original parsed-response value. */
type RenderedHtml = io.estebangarcia21.paper.web.testing.RenderedHtml

val RenderedHtml: io.estebangarcia21.paper.web.testing.RenderedHtml.type =
  io.estebangarcia21.paper.web.testing.RenderedHtml

/** Compatibility base class for existing Paperweb HTML suites. */
abstract class HtmlSuite extends io.estebangarcia21.paper.web.testing.HtmlSuite
