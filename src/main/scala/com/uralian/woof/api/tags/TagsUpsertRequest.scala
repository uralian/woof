package com.uralian.woof.api.tags

import com.uralian.woof.api.Tag

/**
 * Tag insert or update API request.
 *
 * @param tags   tags to add/update.
 * @param source optional source.
 */
final case class TagsUpsertRequest(tags: Seq[Tag], source: Option[String])
