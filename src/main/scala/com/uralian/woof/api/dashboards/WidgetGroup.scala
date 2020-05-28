package com.uralian.woof.api.dashboards

import org.json4s.FieldSerializer

/**
 * Group of widgets.
 *
 * @param widgets widgets to include into the group.
 * @param title   group title.
 */
case class WidgetGroup(widgets: Seq[Widget[LayoutType.Ordered.type]], title: Option[String] = None) extends WidgetContent {

  def withTitle(str: String) = copy(title = Some(str))

  private val `type` = "group"
  private val layout_type = LayoutType.Ordered
}

/**
 * Factory for [[WidgetGroup]] instances.
 */
object WidgetGroup {
  val serializer = FieldSerializer[WidgetGroup]()
}