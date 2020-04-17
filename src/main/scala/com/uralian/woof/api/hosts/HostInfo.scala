package com.uralian.woof.api.hosts

import java.time.Instant

import com.uralian.woof.api.Tag
import com.uralian.woof.util.JsonUtils._
import org.json4s.native.Serialization

/**
 * Host information.
 *
 * @param name             host name.
 * @param lastReportedTime last time the host reported to DataDog.
 * @param isMuted          whether the host has been muted.
 * @param muteTimeout      if the host has been muted, the time when it will be unmuted (if any).
 * @param apps             host applications.
 * @param tagsBySource     tags by source.
 * @param up               host status (`true` means the host is up).
 * @param metrics          host current metrics.
 * @param sources          sources.
 * @param hostName         hostname.
 * @param id               host id.
 * @param aliases          host aliases.
 */
final case class HostInfo(name: String,
                          lastReportedTime: Instant,
                          isMuted: Boolean,
                          muteTimeout: Option[Instant],
                          apps: Seq[String],
                          tagsBySource: Map[String, Seq[Tag]],
                          up: Boolean,
                          metrics: Map[String, BigDecimal],
                          sources: Seq[String],
                          hostName: String,
                          id: Long,
                          aliases: Seq[String]) {
  override def toString: String = Serialization.write(this)
}

/**
 * Provides JSON serializer for [[HostInfo]] instances.
 */
object HostInfo {
  val serializer = translateFields[HostInfo]("lastReportedTime" -> "last_reported_time",
    "isMuted" -> "is_muted", "muteTimeout" -> "mute_timeout", "tagsBySource" -> "tags_by_source",
    "hostName" -> "host_name")
}
