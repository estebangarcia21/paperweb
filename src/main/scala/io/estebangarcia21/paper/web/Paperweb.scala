package io.estebangarcia21.paper.web

import cats.effect.{Resource, Sync}
import cats.syntax.all.*
import org.http4s.{Header, HttpRoutes, Method, Response, Status}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.ci.*

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest
import java.util.HexFormat
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*
import scala.concurrent.duration.Duration
import scala.util.Try

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

  private[web] def liveReloadEnabled: Boolean =
    isDevelopment && sys.props.get("paperweb.liveReload").contains("true")

  private[web] def liveReloadVersion: String =
    val compiled = setting("paperweb.reloadStampFile", "PAPERWEB_RELOAD_STAMP_FILE")
      .flatMap(path => Try(Files.readString(Paths.get(path)).trim).toOption)
      .getOrElse("0")
    val assets = developmentAssetDirectory
      .flatMap(path => Try(assetTreeVersion(path)).toOption)
      .getOrElse("0")

    s"$compiled:$assets"

  private[web] def assetTreeVersion(directory: Path): String =
    if !Files.isDirectory(directory) then "0"
    else
      val files = Files.walk(directory)

      try
        val digest = MessageDigest.getInstance("SHA-256")

        files
          .iterator()
          .asScala
          .filter(path => Files.isRegularFile(path))
          .toVector
          .sortBy(_.toString)
          .foreach { path =>
            val entry =
              s"${directory.relativize(path)}:${Files.size(path)}:${Files.getLastModifiedTime(path).to(TimeUnit.NANOSECONDS)}\n"

            digest.update(entry.getBytes(StandardCharsets.UTF_8))
          }

        HexFormat.of().formatHex(digest.digest())
      finally files.close()

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

  /** Exposes the current compiled-code and asset version only in hot development mode.
    *
    * Applications add these routes to their HTTP route assembly. The response is never cached, and
    * ordinary development and production modes expose no reload endpoint.
    */
  def liveReloadRoutes[F[_]: Sync]: HttpRoutes[F] =
    if liveReloadEnabled then
      HttpRoutes.of[F] {
        case request
            if request.method == Method.GET && request.uri.path.renderString == "/_paperweb/live-reload" =>
          Sync[F].delay(liveReloadVersion).map { version =>
            Response[F](Status.Ok)
              .withEntity(version)
              .putHeaders(Header.Raw(ci"Cache-Control", "no-store"))
          }
      }
    else HttpRoutes.empty[F]

  /** Builds an Ember server, using fast shutdown when Paperweb development mode is active. */
  def createServer[F[_]](server: EmberServerBuilder[F]): Resource[F, Server] =
    if isDevelopment then server.withShutdownTimeout(Duration.Zero).build
    else server.build

  /** Compatibility name for [[createServer]]. */
  def buildServer[F[_]](server: EmberServerBuilder[F]): Resource[F, Server] =
    createServer(server)

  private def setting(property: String, environment: String): Option[String] =
    sys.props.get(property).orElse(sys.env.get(environment)).filter(_.nonEmpty)
