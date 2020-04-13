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
  def create(data: CreateEvent): Future[Event]

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
   * @return a sequence of events.
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

  def create(data: CreateEvent): Future[Event] = apiPostJ[CreateEvent](path, data).map { json =>
    val event = (json \ "event").extract[Event]
    val status = (json \ "status").extract[String]
    event -> status
  }.collect {
    case (event, "ok") => event
  }

  def get(eventId: Long): Future[Event] = apiGetJ(s"$path/$eventId") map { json =>
    (json \ "event").extract[Event]
  }

  def query(query: EventQuery): Future[Seq[Event]] = apiGetJ(path, query) map { json =>
    (json \ "events").extract[Seq[Event]]
  }
}