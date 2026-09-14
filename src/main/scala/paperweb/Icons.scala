package paperweb

import scalatags.Text.Frag
import scalatags.Text.all.*

/** Compatibility icons retained for existing Paperweb consumers.
  *
  * New applications should generate an application-owned `Icons` object with `paperweb icon add`.
  */
object Icons:

  def layoutDashboard: Frag = raw(
    """<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/></svg>"""
  )

  def star: Frag = raw(
    """<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M11.525 2.295a.47.47 0 0 1 .95 0l2.002 6.162a.47.47 0 0 0 .447.325h6.48a.47.47 0 0 1 .276.85l-5.243 3.81a.47.47 0 0 0-.17.525l2.002 6.162a.47.47 0 0 1-.724.526l-5.243-3.81a.47.47 0 0 0-.552 0l-5.243 3.81a.47.47 0 0 1-.724-.526l2.002-6.162a.47.47 0 0 0-.17-.525L2.32 9.632a.47.47 0 0 1 .276-.85h6.48a.47.47 0 0 0 .447-.325z"/></svg>"""
  )

  def link: Frag = raw(
    """<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>"""
  )

  def stopwatch: Frag = raw(
    """<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M10 2h4"/><path d="M12 14v-4"/><path d="m4 13 2-2"/><path d="M12 22a8 8 0 1 0 0-16 8 8 0 0 0 0 16Z"/></svg>"""
  )

  def circleInfo: Frag = raw(
    """<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>"""
  )

  def languages: Frag = raw(
    """<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="m5 8 6 6"/><path d="m4 14 6-6 2-3"/><path d="M2 5h12"/><path d="M7 2h1"/><path d="m22 22-5-10-5 10"/><path d="M14 18h6"/></svg>"""
  )

  def panels: Frag = raw(
    """<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><rect width="18" height="18" x="3" y="3" rx="2"/><path d="M3 9h18"/><path d="M9 21V9"/></svg>"""
  )

  def chevronDown: Frag = raw(
    """<svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="m6 9 6 6 6-6"/></svg>"""
  )
