package com.uralian.woof.api.events

import com.uralian.woof.api.AbstractHttpApi
import com.uralian.woof.http.DataDogClient
import org.json4s._

import scala.concurrent.{ExecutionContext, Future}

/**
 * DataDog Events API.
 */
trait EventsApi {
  /**
   * Creates a new event.
   *
   * @param data new event data.
   * @return a new event.
   */
  def create(data: CreateEventData): Future[Event]

  /**
   * Retrieves an existing event by ID.
   *
   * @param eventId event id to look for.
   * @return an existing event.
   */
  def get(eventId: Long): Future[Event]

  /**
   * Queries DataDog event stream.
   *
   * @param query event query.
   * @return
   */
  def query(query: EventQuery): Future[Seq[Event]]
}

/**
 * HTTP-based implementation of [[EventsApi]].
 *
 * @param client DataDog client.
 * @param ec     execution context.
 */
class EventsHttpApi(client: DataDogClient)(implicit ec: ExecutionContext)
  extends AbstractHttpApi(client) with EventsApi {

  val path = "v1/events"

  def create(data: CreateEventData): Future[Event] = postAndExtract(path, data, json => {
    val event = (json \ "event").extract[Event]
    val status = (json \ "status").extract[String]
    event -> status
  }).collect {
    case (event, "ok") => event
  }

  def get(eventId: Long): Future[Event] =
    getAndExtract(s"$path/$eventId", NoParams, json => (json \ "event").extract[Event])

  def query(query: EventQuery): Future[Seq[Event]] =
    getAndExtract(path, query, json => (json \ "events").extract[Seq[Event]])
}