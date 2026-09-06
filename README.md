# Paperweb

Paperweb is a small Scala 3 library for rendering complete server-side HTML documents with
ScalaTags and returning them from http4s. It provides typed views, page metadata and asset helpers,
an HTML entity encoder, and a deliberately small local Lucide icon set.

The library is a standalone sbt project and can be included in another build as a Git submodule.
Consumer application code imports its public API with:

```scala
import paperweb.{*, given}
```

See [docs/getting-started.md](docs/getting-started.md) for usage and
[docs/development.md](docs/development.md) for maintenance commands.
