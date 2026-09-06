# Icons

Add a Lucide icon by running this command from the Paperweb directory:

```sh
./scripts/addIcon.sc circle-dollar-sign
```

The script downloads the pinned Lucide SVG, makes it decorative, and adds a camel-cased method such
as `Icons.circleDollarSign` to `src/main/scala/paperweb/Icons.scala`. It accepts one lowercase,
kebab-case Lucide icon name and requires an internet connection.

When updating the pinned Lucide version, review the generated markup and distribute Lucide's license
with the application or library artifact.
