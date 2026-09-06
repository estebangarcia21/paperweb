val scala3Version = "3.8.4"
val http4sVersion = "0.23.30"

lazy val root = project
  .in(file("."))
  .settings(
    name := "paperweb",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.3.4" % Test,
      "org.jsoup" % "jsoup" % "1.23.2" % Test,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "com.lihaoyi" %% "scalatags" % "0.13.1"
    )
  )
