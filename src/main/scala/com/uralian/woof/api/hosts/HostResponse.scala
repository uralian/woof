package com.uralian.woof.api.hosts

/**
 * Response to host mute/unmute actions.
 *
 * @param action   action performed.
 * @param hostname host name.
 * @param message  optional action message.
 */
final case class HostResponse(action: String, hostname: String, message: Option[String])
