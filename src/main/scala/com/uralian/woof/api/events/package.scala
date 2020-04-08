package com.uralian.woof.api

import enumeratum.Json4s

/**
 * Helper methods and types for Events API.
 */
package object events {

  implicit val eventFormats = apiFormats +
    Json4s.serializer(Priority) +
    Json4s.serializer(AlertType) +
    Event.serializer +
    ChildEvent.serializer +
    CreateEventData.serializer +
    EventQuery.serializer
}
