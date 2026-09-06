#!/usr/bin/env -S scala-cli shebang
//> using scala "3.8.4"
// Documentation: docs/development.md

import scala.concurrent.duration.*
import scala.jdk.OptionConverters.*
import java.nio.file.{Files, Path}

val port = 8080
val paperwebRoot = Path.of(scriptPath).toRealPath().getParent.getParent

val projectRoot =
  Iterator
    .iterate(paperwebRoot.getParent)(_.getParent)
    .takeWhile(_ != null)
    .find(path => Files.isRegularFile(path.resolve("build.sbt")))
    .getOrElse {
      System.err.println(s"Could not find an sbt project from $scriptPath.")
      sys.exit(1)
    }

def listeningPids(): List[Long] =
  val process = new ProcessBuilder(
    "lsof",
    "-nP",
    s"-tiTCP:$port",
    "-sTCP:LISTEN"
  ).redirectErrorStream(true).start()
  val output = new String(process.getInputStream.readAllBytes()).trim
  process.waitFor()
  output.linesIterator.flatMap(_.toLongOption).toList

val initialPids =
  try listeningPids()
  catch
    case _: java.io.IOException =>
      System.err.println(s"lsof is required to free port $port.")
      sys.exit(1)
if initialPids.nonEmpty then
  println(s"Stopping process on port $port...")
  initialPids.flatMap(pid => ProcessHandle.of(pid).toScala).foreach(_.destroy())

  var attempts = 0
  while listeningPids().nonEmpty && attempts < 20 do
    Thread.sleep(100.millis.toMillis)
    attempts += 1

  listeningPids()
    .flatMap(pid => ProcessHandle.of(pid).toScala)
    .foreach(_.destroyForcibly())

println("Watching Scala sources and resources...")
val sbtBuilder = new ProcessBuilder("sbt", "~reStart")
  .directory(projectRoot.toFile)
  .inheritIO()

sbtBuilder.environment().put("PAPERWEB_DEVELOPMENT", "true")
val sbt = sbtBuilder.start()
sys.addShutdownHook {
  if sbt.isAlive then sbt.destroy()
}
sys.exit(sbt.waitFor())
