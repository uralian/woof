package com.uralian.woof.api.hosts

import com.uralian.woof.util.JsonUtils.translateFields

/**
 * The total number of active and up hosts.
 *
 * @param totalUp     total number of hosts that are up.
 * @param totalActive total number of hosts that are active.
 */
final case class HostTotals(totalUp: Int, totalActive: Int)

/**
 * Provides JSON serializer for [[HostTotals]] instances.
 */
object HostTotals {
  val serializer = translateFields[HostTotals]("totalUp" -> "total_up", "totalActive" -> "total_active")
}
