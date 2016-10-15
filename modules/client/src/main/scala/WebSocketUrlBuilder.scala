package com.github.devnfun.grenadier

import org.scalajs.dom._

/**
 * Helper object that constructs a web-socket URL
 * from a relative-URL.
 *
 * Important in doing this is:
 * - that the resulting URL has to be absolute.
 * - because of this the port-number has to be included.
 * - the web-socket protocol (ws/wss) has to correspond
 *   to the http one (http/https).
 *
 */
object WebSocketUrlBuilder {
  private val host = window.location.hostname
  private val port = {
    val p = window.location.port
    if (p.isEmpty)
      ""
    else ":" + p
  }
  private val wsProtocol = {
    val p = window.location.protocol
    if (p.startsWith("https"))
      "wss"
    else
      "ws"
  }

  def fullUrl(relativeUrl: String): String = {
    s"$wsProtocol://$host$port$relativeUrl"
  }
}
