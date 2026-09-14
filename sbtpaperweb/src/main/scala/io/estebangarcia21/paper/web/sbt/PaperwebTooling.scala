package io.estebangarcia21.paper.web.sbt

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.concurrent.{TimeUnit, TimeoutException}
import scala.util.Try
import scala.util.control.NonFatal

import Support._

private[sbt] object PaperwebTooling {

  val help: Seq[String] = Seq(
    "Paperweb commands:",
    "  paperweb init <scala-package>",
    "  paperweb dev",
    "  paperweb doctor",
    "  paperweb icon add <lucide-name>",
    "  paperweb icon remove <lucide-name>",
    "  paperweb icon list",
    "  paperweb icon sync",
    "  paperweb js add <name> [version]",
    "  paperweb js remove <name>",
    "  paperweb js list",
    "  paperweb js sync"
  )

  def run(
      arguments: Seq[String],
      config: PaperwebConfig,
      downloader: Downloader = HttpDownloader
  ): Either[String, Seq[String]] =
    arguments.toList match {
      case Nil | "help" :: Nil =>
        Right(help)
      case "init" :: scalaPackage :: Nil =>
        initialize(config, scalaPackage)
      case "doctor" :: Nil =>
        doctor(config)
      case "icon" :: "add" :: name :: Nil =>
        IconTooling.add(config, name, downloader)
      case "icon" :: "remove" :: name :: Nil =>
        IconTooling.remove(config, name)
      case "icon" :: "list" :: Nil =>
        IconTooling.list(config)
      case "icon" :: "sync" :: Nil =>
        IconTooling.sync(config, downloader)
      case "js" :: "add" :: name :: Nil =>
        JavaScriptTooling.add(config, name, None, downloader)
      case "js" :: "add" :: name :: version :: Nil =>
        JavaScriptTooling.add(config, name, Some(version), downloader)
      case "js" :: "remove" :: name :: Nil =>
        JavaScriptTooling.remove(config, name)
      case "js" :: "list" :: Nil =>
        JavaScriptTooling.list(config)
      case "js" :: "sync" :: Nil =>
        JavaScriptTooling.sync(config, downloader)
      case _ =>
        Left("Unknown Paperweb command. Run sbt \"paperweb help\" for usage.")
    }

  def stopDevelopmentPort(port: Int): Either[String, Seq[String]] =
    try {
      val lookup = new ProcessBuilder(
        "lsof",
        "-nP",
        s"-tiTCP:$port",
        "-sTCP:LISTEN"
      ).redirectErrorStream(true).start()
      val output = new String(lookup.getInputStream.readAllBytes(), StandardCharsets.UTF_8).trim
      val exit = lookup.waitFor()

      if (exit != 0 && output.nonEmpty)
        Left(s"Could not inspect development port $port: $output")
      else {
        val pids = output
          .split("\\s+")
          .toSeq
          .filter(_.nonEmpty)
          .flatMap(value => Try(value.toLong).toOption)
        val stopped = pids.flatMap { pid =>
          val process = java.lang.ProcessHandle.of(pid)

          if (process.isPresent) {
            stop(process.get())
            Some(pid)
          } else None
        }

        Right(
          if (stopped.isEmpty) Seq(s"Development port $port is available.")
          else Seq(s"Stopped ${stopped.mkString(", ")} on development port $port.")
        )
      }
    } catch {
      case _: java.io.IOException =>
        Left(s"lsof is required to free development port $port.")
      case NonFatal(error) =>
        Left(s"Could not prepare development port $port: ${safeMessage(error)}")
    }

  private def stop(process: java.lang.ProcessHandle): Unit = {
    process.destroy()

    try process.onExit().get(2, TimeUnit.SECONDS)
    catch {
      case _: TimeoutException => process.destroyForcibly()
    }
  }

  private def initialize(
      config: PaperwebConfig,
      scalaPackage: String
  ): Either[String, Seq[String]] = {
    val validPackage =
      scalaPackage.matches("[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)*")

    if (config.configFile.exists())
      Left(s"${config.configFile.getName} already exists.")
    else if (!validPackage)
      Left("The Scala package must be a valid dotted package name.")
    else {
      val contents =
        s"""paperweb {
           |  scala-package = "$scalaPackage"
           |  assets-directory = "src/main/resources/assets"
           |
           |  development {
           |    port = 8080
           |  }
           |}
           |""".stripMargin

      write(config.configFile.toPath, contents.getBytes(StandardCharsets.UTF_8)).map { _ =>
        Seq(s"Created ${config.configFile.getName} for $scalaPackage.")
      }
    }
  }

  private def doctor(config: PaperwebConfig): Either[String, Seq[String]] = {
    val configState =
      if (config.configFile.isFile) s"configuration: ${config.configFile.getPath}"
      else s"configuration: missing ${config.configFile.getPath}"
    val packageState =
      config.scalaPackage.fold("Scala package: not configured")(value => s"Scala package: $value")
    val iconState =
      config.iconsOutput.fold("icons output: not configured")(value =>
        s"icons output: ${value.getPath}"
      )

    Right(
      Seq(
        configState,
        packageState,
        iconState,
        s"assets directory: ${config.assetsDirectory.getPath}",
        s"development port: ${config.developmentPort}"
      )
    )
  }

}
