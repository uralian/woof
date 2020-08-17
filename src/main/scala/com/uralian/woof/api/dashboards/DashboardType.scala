package com.uralian.woof.api.dashboards

import enumeratum.EnumEntry._
import enumeratum._

/**
 * Dashboard type.
 */
sealed trait DashboardType extends EnumEntry with Snakecase

/**
 * Available dashboard types.
 */
object DashboardType extends Enum[DashboardType] {

  final case object CustomTimeboard extends DashboardType

  final case object CustomScreenboard extends DashboardType

  final case object IntegrationScreenboard extends DashboardType

  final case object IntegrationTimeboard extends DashboardType

  final case object HostTimeboard extends DashboardType

  val values = findValues
}
