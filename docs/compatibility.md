# Compatibility and migration

Paperweb keeps a source-compatible facade in the original `paperweb` package so MovieNerd and
SquigglyMoney can adopt the published artifact before changing application imports.

The compatibility surface includes:

- `paperweb.{Assets, DocumentConfig, Favicon, OpenGraph, PageMeta, PageRenderer, View}`
- `paperweb.page` with the original `/assets/app.css` and favicon defaults
- `paperweb.Paperweb.buildServer`
- `paperweb.testing.HtmlSuite`
- the small historical `paperweb.Icons` set used by the current applications

New code should use `io.estebangarcia21.paper.web`. Its default renderer is intentionally asset-free;
applications construct a `PageRenderer(DocumentConfig(...))` to make their global stylesheet,
scripts, favicon, and language explicit.

For SquigglyMoney, migrate in two safe phases:

1. Add and enable the sbt plugin, remove the embedded source project or source dependency, and keep
   existing `paperweb.*` imports. Its historical stylesheet and favicon defaults remain unchanged.
2. Add `paperweb.conf`, generate application-owned icons, create the application's `PageRenderer`,
   and move imports to `io.estebangarcia21.paper.web.*` in an ordinary reviewed change.

MovieNerd's embedded fork also hardcodes MovieNerd-specific global stylesheets and scripts. Those are
deliberately not defaults in a reusable library. Before removing that embedded fork, define its
`DocumentConfig` with the same asset lists and route existing page construction through that
application-owned `PageRenderer`. Its existing `PageMeta`, `OpenGraph`, icon, server, and HTML test
APIs are otherwise represented by the compatibility surface.

The facade is a migration boundary, not a second implementation. It delegates to the canonical
runtime and can be deprecated only after both applications no longer import it.

## Future PaperKit modules

The shared organization and package root are `io.estebangarcia21.paper`. Paperweb owns only the
`.web` package, web artifacts, `paperweb.conf`, and the `paperweb` command. A future Papermobile can
live in the same repository or another one, but should have independent artifacts, versions,
configuration, and a `.mobile` package. No placeholder mobile dependency belongs in Paperweb.
