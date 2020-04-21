package com.uralian.woof.api

import enumeratum.Json4s

/**
 * Helper methods and types for Embeds API.
 */
package object embed {

  implicit val metricFormats = apiFormats +
    Json4s.serializer(Timeframe) +
    Json4s.serializer(GraphSize) +
    Embed.serializer +
    CreateEmbed.serializer
}
