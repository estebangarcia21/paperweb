package paperweb

import cats.effect.Resource
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

import scala.concurrent.duration.Duration

object Paperweb:
  private lazy val developmentAssetVersion =
    Option.when(isDevelopment)(System.currentTimeMillis().toString)

  private def isDevelopment: Boolean =
    sys.env.get("PAPERWEB_DEVELOPMENT").contains("true")

  private[paperweb] def versionAssetUrl(path: String, version: String): String =
    val fragmentStart = path.indexOf('#')
    val (url, fragment) =
      if fragmentStart >= 0 then path.splitAt(fragmentStart)
      else (path, "")
    val separator = if url.contains('?') then "&" else "?"
    s"$url${separator}v=$version$fragment"

  private[paperweb] def assetUrl(path: String): String =
    developmentAssetVersion.fold(path)(versionAssetUrl(path, _))

  /** Builds an Ember server with fast shutdown when launched by
    * startDevelopment.sc.
    */
  def buildServer[F[_]](server: EmberServerBuilder[F]): Resource[F, Server] =
    if isDevelopment then server.withShutdownTimeout(Duration.Zero).build
    else server.build
