package paperweb

import scalatags.Text.Frag
import scalatags.Text.all.*
import scalatags.Text.tags2.title

final case class Assets(css: List[String] = Nil, js: List[String] = Nil)

final case class PageMeta(
    title: String,
    assets: Assets = Assets(),
    robots: Option[String] = None
)

/** Anything that can render typed data to HTML. */
type View[-A] = A => Frag

def page[A](meta: A => PageMeta)(bodyView: View[A]): View[A] =
  data => Layout.render(meta(data), bodyView(data))

object Layout:
  def render(metaData: PageMeta, content: Frag): Frag =
    frag(
      raw("<!doctype html>"),
      html(
        lang := "en",
        head(
          meta(attr("charset") := "utf-8"),
          meta(
            attr("name") := "viewport",
            attr("content") := "width=device-width, initial-scale=1"
          ),
          metaData.robots.map(value =>
            meta(attr("name") := "robots", attr("content") := value)
          ),
          title(metaData.title),
          link(
            rel := "icon",
            attr("type") := "image/png",
            href := "/assets/images/favicon.png"
          ),
          link(
            rel := "stylesheet",
            href := Paperweb.assetUrl("/assets/app.css")
          ),
          metaData.assets.css.map(path =>
            link(rel := "stylesheet", href := Paperweb.assetUrl(path))
          ),
          metaData.assets.js.map(path =>
            script(src := Paperweb.assetUrl(path), attr("type") := "module")
          )
        ),
        body(content)
      )
    )
