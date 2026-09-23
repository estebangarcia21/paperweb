# Development

Paperweb is one sbt build with three published modules:

| sbt project | artifact | responsibility |
| --- | --- | --- |
| `root` | `paperweb_3` | Runtime HTML and http4s API |
| `paperwebtestkit` | `paperwebtestkit_3` | DOM-oriented route test helpers |
| `sbtpaperweb` | `sbt-paperweb` | Consumer setup and the `paperweb` command |

Compile, format, test, and publish snapshots from the repository root:

```sh
sbt compile
sbt scalafmtAll scalafmtSbt
sbt scalafmtCheckAll scalafmtSbtCheck
sbt test
sbt root/publishLocal paperwebtestkit/publishLocal sbtpaperweb/scripted
sbt publishLocal
```

In a configured consumer, run development mode with:

```sh
sbt "paperweb dev"
```

The command reads the configured development port, asks `lsof` for a listening process on that port,
stops it when present, and enters sbt-revolver's `~reStart` loop. It supplies the development flag
and absolute asset source directory to the application. Paperweb then gives Ember a zero-second
shutdown timeout and versions rendered asset URLs once per application start. Ordinary and
production launches keep Ember's configured shutdown behavior and stable asset URLs.

For ScalaTags page rendering and other class changes that can be redefined in a running JVM, an
opt-in hot mode keeps the application process and port alive:

```sh
sbt --java-home /path/to/jbrsdk-21 "paperweb dev hot"
```

Download a [JBR 21 SDK](https://github.com/JetBrains/JetBrainsRuntime/releases) and the
[open-source Hotswap Agent JAR](https://github.com/HotswapProjects/HotswapAgent/releases). Place the
agent at `/path/to/jbrsdk-21/lib/hotswap/hotswap-agent.jar` before starting sbt. Use the JBR **SDK**,
because sbt needs a compiler-capable JDK. Hot mode starts the application once with enhanced class
redefinition and Hotswap Agent's automatic class watcher, then runs sbt's `~compile` loop. Saving a
Scala source file compiles it while the server remains running; refresh the browser to see a changed
render body. Paperweb adds a development-only `hotswap-agent.properties` to the forked application's
classpath to enable automatic class watching. The command checks that the agent JAR exists and fails
with setup guidance if it does not. In an interactive sbt shell, Enter leaves the watch while keeping
the server running; `reStop` then stops it. When sbt itself exits, its background server stops too.

Hot swapping changes compiled code, but does not rerun application startup or rebuild values already
created at startup. Restart with `reStart` after changing route assembly, configuration, migration
behavior, or other startup state. The ordinary `paperweb dev` command remains the restart-based
option and does not require JBR or Hotswap Agent.

The plugin is the only supported CLI implementation. The old Scala CLI scripts were removed so the
tooling cannot drift into a second implementation or require consumers to install another launcher.

`sbt publishLocal` publishes the runtime, testkit, and plugin together. A consuming build can then use
`0.1.0-SNAPSHOT` in `project/plugins.sbt` while testing integration locally.
