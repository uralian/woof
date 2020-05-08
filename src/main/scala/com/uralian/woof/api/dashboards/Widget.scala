package com.uralian.woof.api.dashboards

/**
 * UI component encapsulated by a widget.
 */
trait WidgetContent

/**
 * Dashboard widget.
 *
 * @tparam L layout type.
 */
sealed trait Widget[L <: LayoutType] {
  /**
   * Optional widget it.
   *
   * @return widget id.
   */
  def id: Option[Int]

  /**
   * Widget content.
   *
   * @return content encapsulated by this widget.
   */
  def content: WidgetContent
}

/**
 * Factory for [[Widget]] instances.
 */
object Widget {

  /**
   * Free layout widget, requires explicit X, Y, Width and Height coordinates when created.
   *
   * @param content widget content.
   * @param layout  widget coordinates.
   * @param id      optional widget id.
   */
  final case class Free(content: WidgetContent, layout: Layout, id: Option[Int] = None) extends Widget[LayoutType.Free.type]

  /**
   * Ordered layout widget.
   *
   * @param content widget content.
   * @param id      optional widget id.
   */
  final case class Ordered(content: WidgetContent, id: Option[Int] = None) extends Widget[LayoutType.Ordered.type]

  val serializer = translateFields[Widget[_ <: LayoutType]]("content" -> "definition")
}