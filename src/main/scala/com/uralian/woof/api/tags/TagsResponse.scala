package com.uralian.woof.api.tags

import com.uralian.woof.api.Tag

/**
 * Tag API response.
 *
 * @param tags returned tags.
 */
final case class TagsResponse(tags: Seq[Tag])
