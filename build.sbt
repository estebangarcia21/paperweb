val scala3Version = "3.8.4"
val http4sVersion = "0.23.30"
val munitVersion = "1.3.4"
val jsoupVersion = "1.23.2"
val configVersion = "1.4.9"
val revolverVersion = "0.11.2"

ThisBuild / organization := "io.estebangarcia21.paper"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / homepage := Some(url("https://github.com/estebangarcia21/paperweb"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/estebangarcia21/paperweb"),
    "scm:git:https://github.com/estebangarcia21/paperweb.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "estebangarcia21",
    name = "Esteban Garcia",
    email = "",
    url = url("https://github.com/estebangarcia21")
  )
)

lazy val root: Project = project
  .in(file("."))
  .aggregate(paperwebtestkit, sbtpaperweb)
  .settings(
    name := "paperweb",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % munitVersion % Test,
      "org.jsoup" % "jsoup" % jsoupVersion % Test,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "com.lihaoyi" %% "scalatags" % "0.13.1"
    )
  )

lazy val paperwebtestkit: Project = project
  .in(file("paperwebtestkit"))
  .settings(
    name := "paperwebtestkit",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % munitVersion,
      "org.jsoup" % "jsoup" % jsoupVersion,
      "org.typelevel" %% "cats-effect" % "3.7.0",
      "org.http4s" %% "http4s-core" % http4sVersion
    )
  )

lazy val sbtpaperweb: Project = project
  .in(file("sbtpaperweb"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-paperweb",
    sbtPlugin := true,
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= Seq(
      "-Xmx1024M",
      s"-Dplugin.version=${version.value}"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % configVersion,
      "org.scalameta" %% "munit" % munitVersion % Test,
      Defaults.sbtPluginExtra(
        "com.indoorvivants" % "sbt-revolver" % revolverVersion,
        (pluginCrossBuild / sbtBinaryVersion).value,
        (update / scalaBinaryVersion).value
      )
    ),
    Compile / resourceGenerators += Def.task {
      val output = (Compile / resourceManaged).value / "paperweb-version.properties"
      IO.write(output, s"version=${version.value}\n")
      Seq(output)
    }.taskValue
  )
