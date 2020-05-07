package com.uralian.woof.api.graphs

import com.uralian.woof.util.JsonUtils
import enumeratum.EnumEntry.Lowercase
import enumeratum._
import org.json4s.FieldSerializer

/**
 * Color for conditional format.
 */
sealed trait FormatColor extends EnumEntry with Lowercase

/**
 * Available format colors.
 */
object FormatColor extends Enum[FormatColor] {

  case object White extends FormatColor

  case object Red extends FormatColor

  case object Yellow extends FormatColor

  case object Green extends FormatColor

  case object Gray extends FormatColor

  val values = findValues
}

/**
 * Conditional format palette.
 *
 * @param entryName
 */
sealed abstract class FormatPalette(override val entryName: String) extends EnumEntry

/**
 * Factory for format palettes.
 */
object FormatPalette extends Enum[FormatPalette] {

  final case class Standard(textColor: FormatColor, backgroundColor: FormatColor)
    extends FormatPalette(textColor.entryName + "_on_" + backgroundColor.entryName) {
    assert(textColor != backgroundColor, "text and background cannot be of the same color")
  }

  case object CustomText extends FormatPalette("custom_text")

  case object CustomBackgrdound extends FormatPalette("custom_bg")

  case object CustomImage extends FormatPalette("custom_image")

  val values = findValues
}

/**
 * Comparator for conditional formatting.
 *
 * @param entryName
 */
sealed abstract class FormatComparator(override val entryName: String) extends EnumEntry

/**
 * Available comparators.
 */
object FormatComparator extends Enum[FormatComparator] {

  case object GT extends FormatComparator(">")

  case object GE extends FormatComparator(">=")

  case object LT extends FormatComparator("<")

  case object LE extends FormatComparator("<=")

  val values = findValues
}

import com.uralian.woof.api.graphs.FormatColor._
import com.uralian.woof.api.graphs.FormatPalette._

/**
 * Conditional format for rendering text labels.
 *
 * @param comparator            value comparator.
 * @param value                 value to compare.
 * @param palette               color palette.
 * @param customBackgroundColor custom background color.
 * @param customTextColor       custom text color.
 * @param imageUrl              custom image url.
 * @param hideValue             whether to hide the value.
 */
final case class ConditionalFormat(comparator: FormatComparator,
                                   value: BigDecimal,
                                   palette: FormatPalette = Standard(White, Green),
                                   customBackgroundColor: Option[Int] = None,
                                   customTextColor: Option[Int] = None,
                                   imageUrl: Option[String] = None,
                                   hideValue: Boolean = false) {

  def withStandardColors(colors: Standard) = copy(palette = colors)

  def withCustomBackgroundColor(color: Int) = copy(
    palette = CustomBackgrdound,
    customBackgroundColor = Some(color))

  def withCustomTextColor(color: Int) = copy(
    palette = CustomText,
    customTextColor = Some(color))

  def withImageUrl(url: String) = copy(
    palette = CustomImage,
    imageUrl = Some(url))

  def withHiddenValue = copy(hideValue = true)
}

/**
 * Provides JSON serializer for [[ConditionalFormat]].
 */
object ConditionalFormat extends JsonUtils {

  private val ser: FSer = {
    case ("customTextColor", Some(color: Int))       => Some("custom_fg_color" -> ("#" + color.toHexString))
    case ("customBackgroundColor", Some(color: Int)) => Some("custom_bg_color" -> ("#" + color.toHexString))
    case ("imageUrl", Some(url: String))             => Some("image_url" -> url)
    case ("hideValue", hv)                           => Some("hide_value" -> hv)
  }

  val serializer = FieldSerializer[ConditionalFormat](serializer = ser)
}