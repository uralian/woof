package com.uralian.woof.api.graphs

/**
 * Options to customize a graph axis.
 *
 * @param label       axis label.
 * @param scale       graph rendering scale.
 * @param min         minimum value.
 * @param max         maximum value.
 * @param includeZero whether to ensure that zero point is always visible.
 */
final case class AxisOptions(label: Option[String] = None,
                             scale: GraphScale = GraphScale.Default,
                             min: Option[BigDecimal] = None,
                             max: Option[BigDecimal] = None,
                             includeZero: Boolean = true) {

  def withLabel(label: String) = copy(label = Some(label))

  def withScale(scale: GraphScale) = copy(scale = scale)

  def withMin(min: BigDecimal) = copy(min = Some(min))

  def withMax(max: BigDecimal) = copy(max = Some(max))

  def noZero = copy(includeZero = false)
}

/**
 * Factory for [[AxisOptions]] instances.
 */
object AxisOptions {
  val Default = AxisOptions()
}
