# JavaScript dependencies

Paperweb's `jsDeps.sc` script downloads browser-ready npm assets from jsDelivr into the consuming
project's `src/main/resources/assets/js/vendor/` directory. The checked-in `jsDeps.lock` records an
exact version, jsDelivr path, local filename, and SHA-256 checksum for each dependency. Applications
serve these files locally; they do not depend on a CDN at runtime.

Run commands from the SquigglyMoney repository root:

```sh
./paperweb/scripts/jsDeps.sc add htmx
./paperweb/scripts/jsDeps.sc add htmx 2.0.10
./paperweb/scripts/jsDeps.sc list
./paperweb/scripts/jsDeps.sc sync
./paperweb/scripts/jsDeps.sc remove htmx
```

`add` without a version resolves jsDelivr's current `latest` tag once and writes the resulting exact
version to the lock file. Supplying a version must use an exact semantic version; ranges and tags are
not accepted. Re-running `add` updates that dependency. `sync` downloads the locked files and rejects
content whose checksum differs from the lock file.

The aliases `htmx`, `htmx-head-support`, and `alpine` select the browser distributions and stable
local filenames used by Paperweb. Other npm package names are supported when their `package.json`
declares a JavaScript `jsdelivr` or `unpkg` entry. Review a package's license before adding it and
store any required license or notice beside the dependency in `assets/js/vendor/`; the script does
not make licensing decisions.

The script resolves the project and asset paths from its own location, so it works from any current
working directory. It requires an internet connection and Scala CLI.
