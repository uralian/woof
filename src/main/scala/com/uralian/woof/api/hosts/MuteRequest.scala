package com.uralian.woof.api.hosts

import java.time.Instant

/**
 * Request to mute a certain host.
 *
 * @param message    optional message.
 * @param end        when the host should be unmuted (if ever).
 * @param `override` muting override flag.
 */
final case class MuteRequest(message: Option[String], end: Option[Instant], `override`: Boolean)