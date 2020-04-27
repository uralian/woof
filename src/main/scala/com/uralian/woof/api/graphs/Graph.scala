package com.uralian.woof.api.graphs

import com.uralian.woof.util.JsonUtils.translateFields
import org.json4s.native.Serialization

/**
 * An embeddable live graph.
 *
 * @param id                graph id.
 * @param templateVariables template variables.
 * @param html              html to render the graph.
 * @param title             graph title.
 * @param revoked           whether the graph was revoked.
 * @param dashName          name of the dashboard this graph is on.
 * @param dashUrl           URL of the dashboard this graph it on.
 * @param sharedBy          ID of the user who shared the graph.
 */
final case class Graph(id: String,
                       templateVariables: Seq[String],
                       html: String,
                       title: String,
                       revoked: Boolean,
                       dashName: Option[String],
                       dashUrl: Option[String],
                       sharedBy: Option[Int]) {
  override def toString: String = Serialization.write(this)
}

/**
 * Provides JSON serializer for [[Graph]] instances.
 */
object Graph {
  val serializer = translateFields[Graph]("id" -> "embed_id", "templateVariables" -> "template_variables",
    "title" -> "graph_title", "dashName" -> "dash_name", "dashUrl" -> "dash_url", "sharedBy" -> "shared_by")
}