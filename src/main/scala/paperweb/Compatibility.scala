package paperweb

import org.http4s.EntityEncoder
import scalatags.Text.Frag

import io.estebangarcia21.paper.web as core

type Assets = core.Assets
val Assets: core.Assets.type = core.Assets

type DocumentConfig = core.DocumentConfig
val DocumentConfig: core.DocumentConfig.type = core.DocumentConfig

type Favicon = core.Favicon
val Favicon: core.Favicon.type = core.Favicon

type OpenGraph = core.OpenGraph
val OpenGraph: core.OpenGraph.type = core.OpenGraph

type PageMeta = core.PageMeta
val PageMeta: core.PageMeta.type = core.PageMeta

type PageRenderer = core.PageRenderer
val PageRenderer: core.PageRenderer.type = core.PageRenderer

type View[-A] = core.View[A]

private val legacyConfig = core.DocumentConfig(
  globalAssets = core.Assets(css = List("/assets/app.css")),
  favicon = Some(core.Favicon("/assets/images/favicon.png"))
)

private val legacyRenderer = core.PageRenderer(legacyConfig)

/** Compatibility page constructor for applications using Paperweb's original default assets. */
def page[A](meta: A => PageMeta)(bodyView: View[A]): View[A] =
  legacyRenderer.page(meta)(bodyView)

/** Compatibility encoder for the original `paperweb` import path. */
given htmlEncoder[F[_]]: EntityEncoder[F, Frag] = core.htmlEncoder[F]

/** Compatibility access to the original runtime helper name. */
val Paperweb: core.Paperweb.type = core.Paperweb

/** Compatibility renderer using Paperweb's original stylesheet and favicon defaults. */
object Layout:

  def render(metaData: PageMeta, content: Frag): Frag =
    core.Layout.render(legacyConfig, metaData, content)
