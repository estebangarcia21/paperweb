package io.estebangarcia21.paper.web

import cats.effect.Resource
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

import java.nio.file.{Path, Paths}
import scala.concurrent.duration.Duration

/** Runtime helpers shared by Paperweb applications.
  *
  * Development mode versions asset URLs once per server start and disables Ember's shutdown delay;
  * production behavior remains cache-friendly and graceful. The sbt plugin supplies development
  * settings through system properties and environment variables.
  */
object Paperweb:

  private lazy val developmentAssetVersion =
    Option.when(isDevelopment)(System.currentTimeMillis().toString)

  private def isDevelopment: Boolean =
    setting("paperweb.development", "PAPERWEB_DEVELOPMENT").contains("true")

  private[web] def versionAssetUrl(path: String, version: String): String =
    val fragmentStart = path.indexOf('#')
    val (url, fragment) =
      if fragmentStart >= 0 then path.splitAt(fragmentStart)
      else (path, "")
    val separator = if url.contains('?') then "&" else "?"
    s"$url${separator}v=$version$fragment"

  private[web] def assetUrl(path: String): String =
    developmentAssetVersion.fold(path)(versionAssetUrl(path, _))

  /** Returns the application asset source directory supplied by the development tooling. */
  def developmentAssetDirectory: Option[Path] =
    setting("paperweb.assetDirectory", "PAPERWEB_ASSET_DIRECTORY").map(Paths.get(_))

  /** Builds an Ember server, using fast shutdown when Paperweb development mode is active. */
  def createServer[F[_]](server: EmberServerBuilder[F]): Resource[F, Server] =
    if isDevelopment then server.withShutdownTimeout(Duration.Zero).build
    else server.build

  /** Compatibility name for [[createServer]]. */
  def buildServer[F[_]](server: EmberServerBuilder[F]): Resource[F, Server] =
    createServer(server)

  private def setting(property: String, environment: String): Option[String] =
    sys.props.get(property).orElse(sys.env.get(environment)).filter(_.nonEmpty)
