package com.uralian.woof.http

import enumeratum._

/**
 * DataDog site.
 *
 * @param url
 */
sealed abstract class DataDogSite(val url: String) extends EnumEntry

/**
 * Enumerates available DataDog sites.
 */
object DataDogSite extends Enum[DataDogSite] {

  case object US extends DataDogSite("https://api.datadoghq.com")

  case object EU extends DataDogSite("https://api.datadoghq.eu")

  val Default = US

  val values = findValues
}
