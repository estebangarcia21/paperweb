#!/usr/bin/env -S scala-cli shebang
//> using scala "3.8.4"
// Documentation: docs/javascript-dependencies.md

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.http.HttpClient.Redirect
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardCopyOption}
import java.security.MessageDigest

final case class Dependency(
    name: String,
    npmPackage: String,
    version: String,
    sourcePath: String,
    fileName: String,
    sha256: String
):
  def lockLine: String =
    List(name, npmPackage, version, sourcePath, fileName, sha256).mkString("\t")

final case class KnownDependency(
    npmPackage: String,
    sourcePath: String,
    fileName: String
)

val knownDependencies = Map(
  "htmx" -> KnownDependency("htmx.org", "dist/htmx.min.js", "htmx.min.js"),
  "htmx-head-support" -> KnownDependency(
    "htmx-ext-head-support",
    "dist/head-support.min.js",
    "htmx-head-support.min.js"
  ),
  "alpine" -> KnownDependency("alpinejs", "dist/cdn.min.js", "alpine.min.js")
)

val paperwebRoot = Path.of(scriptPath).toRealPath().getParent.getParent
val projectRoot = Iterator
  .iterate(paperwebRoot.getParent)(_.getParent)
  .takeWhile(_ != null)
  .find(path => Files.isRegularFile(path.resolve("build.sbt")))
  .getOrElse {
    System.err.println(s"Could not find an sbt project from $scriptPath.")
    sys.exit(1)
  }
val vendorDirectory = projectRoot.resolve("src/main/resources/assets/js/vendor")
val lockFile = vendorDirectory.resolve("jsDeps.lock")
val httpClient =
  HttpClient.newBuilder().followRedirects(Redirect.NORMAL).build()

def fail(message: String, exitCode: Int = 1): Nothing =
  System.err.println(message)
  sys.exit(exitCode)

def usage(): Nothing =
  System.err.println(
    "Usage: ./paperweb/scripts/jsDeps.sc <command> [arguments]"
  )
  System.err.println("  add <name> [version]  Add or update a dependency")
  System.err.println("  remove <name>         Remove a dependency")
  System.err.println("  sync                  Restore every pinned dependency")
  System.err.println("  list                  List pinned dependencies")
  fail("Known aliases: alpine, htmx, htmx-head-support", 2)

def get(url: String): Array[Byte] =
  val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
  val response =
    httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
  if response.statusCode() / 100 != 2 then
    fail(s"Could not download $url (HTTP ${response.statusCode()}).")
  response.body()

def text(url: String): String = String(get(url), StandardCharsets.UTF_8)

def jsonString(json: String, field: String): Option[String] =
  val pattern = ("(?s)\\\"" + java.util.regex.Pattern.quote(
    field
  ) + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").r
  pattern.findFirstMatchIn(json).map(_.group(1))

def latestVersion(npmPackage: String): String =
  val metadata = text(s"https://data.jsdelivr.com/v1/package/npm/$npmPackage")
  val tags = "(?s)\\\"tags\\\"\\s*:\\s*\\{(.*?)\\}".r
    .findFirstMatchIn(metadata)
    .map(_.group(1))
    .getOrElse(fail(s"jsDelivr returned no tags for $npmPackage."))
  jsonString(tags, "latest").getOrElse(
    fail(s"jsDelivr returned no latest version for $npmPackage.")
  )

def defaultSourcePath(npmPackage: String, version: String): String =
  val packageJson = text(
    s"https://cdn.jsdelivr.net/npm/$npmPackage@$version/package.json"
  )
  jsonString(packageJson, "jsdelivr")
    .orElse(jsonString(packageJson, "unpkg"))
    .filter(_.endsWith(".js"))
    .getOrElse(
      fail(
        s"$npmPackage@$version does not declare a jsDelivr or unpkg JavaScript entry."
      )
    )

def safeName(value: String): String =
  value.stripPrefix("@").replace('/', '-').replaceAll("[^A-Za-z0-9._-]", "-")

def hash(bytes: Array[Byte]): String =
  MessageDigest
    .getInstance("SHA-256")
    .digest(bytes)
    .map("%02x".format(_))
    .mkString

def readLock(): List[Dependency] =
  if !Files.isRegularFile(lockFile) then Nil
  else
    Files
      .readAllLines(lockFile, StandardCharsets.UTF_8)
      .toArray(new Array[String](0))
      .toList
      .filter(line => line.nonEmpty && !line.startsWith("#"))
      .map { line =>
        line.split("\t", -1).toList match
          case List(name, npmPackage, version, sourcePath, fileName, sha256) =>
            Dependency(name, npmPackage, version, sourcePath, fileName, sha256)
          case _ => fail(s"Invalid entry in $lockFile: $line")
      }

def atomicWrite(path: Path, bytes: Array[Byte]): Unit =
  Files.createDirectories(path.getParent)
  val temporary =
    Files.createTempFile(path.getParent, s".${path.getFileName}", ".tmp")
  try
    Files.write(temporary, bytes)
    try
      Files.move(
        temporary,
        path,
        StandardCopyOption.ATOMIC_MOVE,
        StandardCopyOption.REPLACE_EXISTING
      )
    catch
      case _: java.nio.file.AtomicMoveNotSupportedException =>
        Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING)
  finally Files.deleteIfExists(temporary)

def writeLock(dependencies: List[Dependency]): Unit =
  val contents =
    "# name\tnpm-package\tversion\tjsdelivr-path\tlocal-file\tsha256\n" +
      dependencies.sortBy(_.name).map(_.lockLine).mkString("\n") + "\n"
  atomicWrite(lockFile, contents.getBytes(StandardCharsets.UTF_8))

def install(dependency: Dependency, verifyHash: Boolean): Dependency =
  val url =
    s"https://cdn.jsdelivr.net/npm/${dependency.npmPackage}@${dependency.version}/${dependency.sourcePath}"
  val bytes = get(url)
  val actualHash = hash(bytes)
  if verifyHash && actualHash != dependency.sha256 then
    fail(
      s"Checksum mismatch for ${dependency.name}: expected ${dependency.sha256}, got $actualHash."
    )
  atomicWrite(vendorDirectory.resolve(dependency.fileName), bytes)
  dependency.copy(sha256 = actualHash)

args.toList match
  case "add" :: name :: optionalVersion if optionalVersion.length <= 1 =>
    if !name.matches("(?:@[a-z0-9._-]+/)?[a-z0-9._-]+") then
      fail(
        "Dependency names must be lowercase npm package names or known aliases.",
        2
      )
    val known = knownDependencies.get(name)
    val npmPackage = known.map(_.npmPackage).getOrElse(name)
    val version =
      optionalVersion.headOption.getOrElse(latestVersion(npmPackage))
    if !version.matches("[0-9]+\\.[0-9]+\\.[0-9]+(?:[-+][A-Za-z0-9.-]+)?") then
      fail("Versions must be exact semantic versions, for example 2.0.10.", 2)
    val sourcePath =
      known.map(_.sourcePath).getOrElse(defaultSourcePath(npmPackage, version))
    val fileName = known.map(_.fileName).getOrElse(s"${safeName(name)}.min.js")
    val installed = install(
      Dependency(name, npmPackage, version, sourcePath, fileName, ""),
      false
    )
    writeLock(readLock().filterNot(_.name == name) :+ installed)
    println(
      s"Pinned $name ${installed.version} at assets/js/vendor/${installed.fileName}."
    )

  case "remove" :: name :: Nil =>
    val dependencies = readLock()
    dependencies.find(_.name == name) match
      case None             => fail(s"$name is not pinned.")
      case Some(dependency) =>
        Files.deleteIfExists(vendorDirectory.resolve(dependency.fileName))
        writeLock(dependencies.filterNot(_.name == name))
        println(s"Removed $name.")

  case "sync" :: Nil =>
    val dependencies = readLock()
    dependencies.foreach(dependency => install(dependency, true))
    println(s"Restored ${dependencies.size} pinned JavaScript dependencies.")

  case "list" :: Nil =>
    val dependencies = readLock()
    if dependencies.isEmpty then
      println("No JavaScript dependencies are pinned.")
    else
      dependencies
        .sortBy(_.name)
        .foreach(dependency =>
          println(s"${dependency.name} ${dependency.version}")
        )

  case _ => usage()
