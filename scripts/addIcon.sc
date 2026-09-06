#!/usr/bin/env -S scala-cli shebang
//> using scala "3.8.4"
// Documentation: docs/icons.md

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardCopyOption}

val lucideVersion = "1.37.0"
val paperwebRoot = Path.of(scriptPath).toRealPath().getParent.getParent
val iconsFile = paperwebRoot.resolve("src/main/scala/paperweb/Icons.scala")

if !iconsFile.toFile.isFile then
  System.err.println(s"Could not find Paperweb's Icons.scala from $scriptPath.")
  sys.exit(1)

def fail(message: String, exitCode: Int): Nothing =
  System.err.println(message)
  sys.exit(exitCode)

if args.length != 1 then
  System.err.println("Usage: ./scripts/addIcon.sc <lucide-icon-name>")
  fail("Example: ./scripts/addIcon.sc circle-dollar-sign", 2)

val iconName = args.head
if !iconName.matches("[a-z0-9]+(?:-[a-z0-9]+)*") then
  fail("Icon names must use lowercase kebab-case.", 2)

val methodName =
  val parts = iconName.split('-')
  parts.head + parts.tail
    .map(part => s"${part.head.toUpper}${part.tail}")
    .mkString

val currentIcons = Files.readString(iconsFile, StandardCharsets.UTF_8)
if currentIcons.contains(s"def $methodName:") then
  fail(s"Icons.$methodName already exists.", 1)

val iconUrl =
  URI.create(
    s"https://cdn.jsdelivr.net/npm/lucide-static@$lucideVersion/icons/$iconName.svg"
  )
val request = HttpRequest.newBuilder(iconUrl).GET().build()
val response =
  HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
if response.statusCode() / 100 != 2 then
  fail(
    s"Could not download $iconName from Lucide (HTTP ${response.statusCode()}).",
    1
  )

val svg = response
  .body()
  .replaceAll("(?s)<!--.*?-->", "")
  .replaceAll("\\s+", " ")
  .replaceFirst("class=\"lucide lucide-[^\"]*\"", "class=\"icon\"")
  .replace(" width=\"24\"", "")
  .replace(" height=\"24\"", "")
  .replaceAll("\\s+>", ">")
  .replaceAll("\\s+/>", "/>")
  .replaceAll("</svg>.*$", "</svg>")
  .replaceFirst("<svg ", "<svg aria-hidden=\"true\" focusable=\"false\" ")
  .trim

val marker = "  // add-icon: insert before this line"
if !currentIcons.contains(marker) then
  fail(s"Insertion marker is missing from $iconsFile.", 1)

val method = s"  def $methodName: Frag = raw(\"\"\"$svg\"\"\")"
val updatedIcons = currentIcons.replace(marker, s"$method\n\n$marker")
val temporaryFile =
  Files.createTempFile(iconsFile.getParent, "Icons", ".scala.tmp")

try
  Files.writeString(temporaryFile, updatedIcons, StandardCharsets.UTF_8)
  try
    Files.move(
      temporaryFile,
      iconsFile,
      StandardCopyOption.ATOMIC_MOVE,
      StandardCopyOption.REPLACE_EXISTING
    )
  catch
    case _: java.nio.file.AtomicMoveNotSupportedException =>
      Files.move(temporaryFile, iconsFile, StandardCopyOption.REPLACE_EXISTING)
finally Files.deleteIfExists(temporaryFile)

println(s"Added Icons.$methodName from Lucide $lucideVersion.")
