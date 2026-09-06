package paperweb

import org.http4s.{Charset, EntityEncoder, MediaType}
import org.http4s.headers.`Content-Type`
import scalatags.Text.Frag

/** Lets http4s return ScalaTags fragments as UTF-8 HTML. */
given htmlEncoder[F[_]]: EntityEncoder[F, Frag] =
  EntityEncoder
    .stringEncoder[F]
    .contramap[Frag](_.render)
    .withContentType(`Content-Type`(MediaType.text.html, Charset.`UTF-8`))
