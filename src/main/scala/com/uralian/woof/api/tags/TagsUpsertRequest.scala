package com.uralian.woof.api.tags

import com.uralian.woof.api.Tag

case class TagsUpsertRequest(tags: Seq[Tag], source: Option[String])
