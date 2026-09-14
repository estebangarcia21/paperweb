# Configuration

Paperweb reads one optional HOCON file named `paperweb.conf` from the consuming sbt project's root.
Generate the usual starting point with:

```sh
sbt "paperweb init com.example.myapp"
```

The complete configuration surface is:

```hocon
paperweb {
  scala-package = "com.example.myapp"
  icons-output = "src/main/scala/com/example/myapp/Icons.scala"
  assets-directory = "src/main/resources/assets"

  development {
    port = 8080
  }
}
```

`scala-package` is required only for icon generation. When it is present, `icons-output` defaults to
an `Icons.scala` in that package and normally should be omitted. `assets-directory` and
`development.port` have the values shown above by default.

All configured paths must resolve inside the consuming project. This keeps commands scoped to the
repository where sbt invoked them. Use project-relative paths so configuration remains portable.

Inspect the resolved configuration with:

```sh
sbt "paperweb doctor"
```

The plugin settings `paperwebConfigFile` and `paperwebAddLibrary` support unusual sbt builds. Most
applications should leave them at their defaults.
