# Getting started

Paperweb requires Scala 3 and exposes its API from the `paperweb` package. Its sbt subproject
declares ScalaTags and http4s as library dependencies.

Define a typed `View`, wrap it with `page`, and import the supplied http4s encoder:

```scala
import paperweb.{*, given}
import scalatags.Text.all.*
import scalatags.Text.tags2.main

val greeting: View[String] =
  page(name => Meta(title = s"Hello, $name")) { name =>
    main(h1(s"Hello, $name"))
  }
```

`Meta.assets` adds page-specific CSS and JavaScript URLs. Paperweb also emits links for
`/assets/app.css` and `/assets/images/favicon.png`; applications are responsible for serving those
resources. When launched through `scripts/startDev.sc`, stylesheet and module script URLs
receive a development-only version query parameter so resource edits are visible after the automatic
server restart. `Meta.robots` can add a robots directive such as `noindex`.

`Icons` contains local inline SVG fragments. Icons are decorative by default, inherit
`currentColor`, and should be paired with visible text or an accessible name on their control.
