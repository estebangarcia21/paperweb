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

The plugin is the only supported CLI implementation. The old Scala CLI scripts were removed so the
tooling cannot drift into a second implementation or require consumers to install another launcher.

`sbt publishLocal` publishes the runtime, testkit, and plugin together. A consuming build can then use
`0.1.0-SNAPSHOT` in `project/plugins.sbt` while testing integration locally.
