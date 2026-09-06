package paperweb

import scalatags.Text.Frag
import scalatags.Text.all.*

/** The small set of Lucide icons bundled with Paperweb. */
object Icons:
  def layoutDashboard: Frag = raw(
    """<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/></svg>"""
  )

  // add-icon: insert before this line
