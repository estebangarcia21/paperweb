package smoke

import paperweb.{*, given}
import scalatags.Text.all.*

object LegacyPage:
  val view: View[Unit] =
    page(_ => PageMeta("Legacy smoke test"))(_ => div(Icons.star, "Paperweb"))
