# Paperweb

Paperweb is a lightweight Scala 3 foundation for server-rendered web applications using http4s,
ScalaTags, HTMX, Alpine, CSS, and locally served JavaScript. It is intentionally two things shipped
from one repository:

- `paperweb`, the runtime library under `io.estebangarcia21.paper.web`
- `sbt-paperweb`, the sbt plugin that adds the matching runtime and testkit and provides the
  `paperweb` command

Consumers do not install a separate CLI or Scala CLI. Add the sbt plugin, enable it, and run commands
through the sbt launcher already used by the project.

```scala
// project/plugins.sbt
addSbtPlugin("io.estebangarcia21.paper" % "sbt-paperweb" % "<version>")
```

```scala
// build.sbt
lazy val root = project.enablePlugins(PaperwebPlugin)
```

```sh
sbt "paperweb init com.example.myapp"
sbt "paperweb icon add star"
sbt "paperweb js add htmx 2.0.10"
```

The plugin version selects the same version of `paperweb` and `paperwebtestkit`, so consumers have
one version to manage. The original `paperweb` Scala package remains as a compatibility facade for
MovieNerd and SquigglyMoney while they move to the canonical package.

Paperweb belongs to the broader **PaperKit** family. Its artifact, plugin, command, configuration,
and package are web-specific, leaving room for an independently versioned `papermobile` project and
`io.estebangarcia21.paper.mobile` package later without coupling mobile dependencies into web apps.

Start with [Getting started](docs/getting-started.md). Configuration, tooling, compatibility, and
repository maintenance are covered in the other documents under [`docs/`](docs/).
