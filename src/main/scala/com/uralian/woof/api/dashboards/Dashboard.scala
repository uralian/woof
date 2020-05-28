package com.uralian.woof.api.dashboards

import java.time.Instant

import com.uralian.woof.util.JsonUtils

/**
 * DataDog Dashboard.
 *
 * @param id           dashboard id.
 * @param title        dashboard title.
 * @param url          dashboard relative url.
 * @param description  dashboard description.
 * @param authorName   author's name.
 * @param authorHandle author's handle.
 * @param createdAt    creation time.
 * @param modifiedAt   last modification time.
 * @param isReadOnly   whether others can modify this dashboard.
 * @param layoutType   layout type.
 * @param notifyList   notification list.
 * @param templateVars template variables.
 * @param presets      dashboard presets.
 */
final case class Dashboard(id: String,
                           title: String,
                           url: String,
                           description: Option[String],
                           authorName: String,
                           authorHandle: String,
                           createdAt: Instant,
                           modifiedAt: Instant,
                           isReadOnly: Boolean,
                           layoutType: LayoutType,
                           notifyList: Seq[String],
                           templateVars: Seq[TemplateVar],
                           presets: Seq[Preset])

/**
 * Factory for [[Dashboard]] instances.
 */
object Dashboard extends JsonUtils {

  val serializer = translateFields[Dashboard]("authorName" -> "author_name", "authorHandle" -> "author_handle",
    "createdAt" -> "created_at", "modifiedAt" -> "modified_at", "isReadOnly" -> "is_read_only",
    "layoutType" -> "layout_type", "notifyList" -> "notify_list", "templateVars" -> "template_variables",
    "presets" -> "template_variable_presets")
}
