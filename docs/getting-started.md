# Getting started

Paperweb requires Scala 3 and an sbt build. The sbt plugin is both the setup entry point and the
command-line tool, so there is no separate executable to install.

Add the released plugin version to the consumer's meta-build:

```scala
// project/plugins.sbt
addSbtPlugin("io.estebangarcia21.paper" % "sbt-paperweb" % "<version>")
```

Enable Paperweb on the application project:

```scala
// build.sbt
lazy val root = project
  .in(file("."))
  .enablePlugins(PaperwebPlugin)
```

The plugin automatically adds matching `paperweb` compile and `paperwebtestkit` test dependencies.
Set `paperwebAddLibrary := false` only when a build deliberately manages those dependencies itself.

Create the application configuration once:

```sh
sbt "paperweb init com.example.myapp"
```

Then define a renderer that owns the application's global browser assets and document defaults:

```scala
package com.example.myapp

import io.estebangarcia21.paper.web.*
import scalatags.Text.all.*
import scalatags.Text.tags2.main

val pages = PageRenderer(
  DocumentConfig(
    globalAssets = Assets(
      css = List("/assets/app.css"),
      js = List("/assets/js/vendor/htmx.min.js")
    ),
    favicon = Some(Favicon("/assets/images/favicon.png"))
  )
)

val greeting: View[String] =
  pages.page(name => PageMeta(title = s"Hello, $name")) { name =>
    main(h1(s"Hello, $name"))
  }
```

Import `io.estebangarcia21.paper.web.given` where http4s needs Paperweb's HTML entity encoder. Build
an Ember server with `Paperweb.createServer(serverBuilder)`. The older
`Paperweb.buildServer(serverBuilder)` name remains available for existing applications.

The default `PageRenderer()` deliberately assumes no stylesheet, JavaScript, or favicon. Global
assets are application policy; page-specific assets belong in `PageMeta.assets`. The renderer also
supports robots, description, canonical URL, and Open Graph metadata.

During repository development, publish all three artifacts locally and use the snapshot version:

```sh
sbt publishLocal
```

The current development version is `0.1.0-SNAPSHOT`.
