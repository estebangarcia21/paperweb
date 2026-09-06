# Development

From the Paperweb repository root, compile and test the library:

```sh
sbt test
```

When Paperweb is checked out as a submodule of an sbt application, start that application with
automatic recompilation and restart:

```sh
./paperweb/scripts/startDev.sc
```

The script locates the nearest parent sbt build outside Paperweb, frees port 8080 with `lsof`, sets
`PAPERWEB_DEVELOPMENT=true`, and starts `sbt ~reStart`. It can be invoked from any working directory
because paths are resolved from the script itself.

Application startup code passes its configured Ember builder to Paperweb:

```scala
import paperweb.Paperweb

Paperweb.buildServer(server)
```

Paperweb keeps the environment flag private and applies a zero-second shutdown timeout when the
development script launches the application. It also adds a server-start version query parameter to
the global stylesheet and CSS and JavaScript declared through `Meta.assets`. Browser asset caches are
therefore invalidated after an automatic restart while URLs remain stable between requests. Normal
sbt runs and production launches retain the server's graceful shutdown behavior and unversioned asset
URLs.

Manage locally served JavaScript dependencies with `scripts/jsDeps.sc`. See
[javascript-dependencies.md](javascript-dependencies.md) for its add, remove, list, and sync commands.
