package com.uralian.woof.api.dashboards

import com.uralian.woof.util.JsonUtils
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.Serialization

/**
 * Dashboard template variable.
 *
 * @param name
 * @param prefix
 * @param default
 */
final case class TemplateVar(name: String, prefix: Option[String] = None, default: Option[String] = None) {

  def withPrefix(str: String) = copy(prefix = Some(str))

  def withDefault(str: String) = copy(default = Some(str))
}

/**
 * Dashboard preset.
 *
 * @param name
 * @param vars
 */
final case class Preset(name: String, vars: Map[String, String] = Map.empty) {
  def withVars(moreVars: (String, String)*) = copy(vars = vars ++ moreVars)
}

/**
 * Factory for [[Preset]] instances.
 */
object Preset extends JsonUtils {

  private val ser: FSer = {
    case ("vars", entries: Map[_, _]) => Some("template_variables" -> entries.map {
      case (key, value) => JObject("name" -> key.toString, "value" -> value.toString)
    })
  }

  private val des: FDes = {
    case ("template_variables", JArray(items)) =>
      val fields = items.map { jv =>
        val name = (jv \ "name").extract[String]
        val value = jv \ "value"
        name -> value
      }
      "vars" -> JObject(fields)
  }

  val serializer = FieldSerializer[Preset](ser, des)
}

/**
 * Request to create a dashboard.
 *
 * @param title
 * @param layoutType
 * @param widgets
 * @param description
 * @param readOnly
 * @param notifyList
 * @param templateVars
 * @param presets
 * @tparam L
 */
final case class CreateDashboard[L <: LayoutType](title: String,
                                                  layoutType: L,
                                                  widgets: Seq[Widget[L]],
                                                  description: Option[String] = None,
                                                  readOnly: Boolean = false,
                                                  notifyList: Seq[String] = Nil,
                                                  templateVars: Seq[TemplateVar] = Nil,
                                                  presets: Seq[Preset] = Nil) {

  def withDescription(str: String) = copy(description = Some(str))

  def setReadOnly = copy(readOnly = true)

  def notifyUsers(users: String*) = copy(notifyList = notifyList ++ users)

  def withVars(vars: TemplateVar*) = copy(templateVars = templateVars ++ vars)

  def withPresets(ps: Preset*) = copy(presets = presets ++ ps)

  override def toString: String = Serialization.write(this)
}

/**
 * Factory for [[CreateDashboard]] instances.
 */
object CreateDashboard extends JsonUtils {

  val serializer = translateFields[CreateDashboard[_ <: LayoutType]]("layoutType" -> "layout_type",
    "readOnly" -> "is_read_only", "notifyList" -> "notify_list", "templateVars" -> "template_variables",
    "presets" -> "template_variable_presets")
}