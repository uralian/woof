package com.uralian.woof.http

import com.uralian.woof.AbstractITSpec
import com.uralian.woof.api.DataDogApiError
import com.uralian.woof.http.DataDogClientSpec.{TestRequest, TestResponse}
import org.json4s._
import sttp.model.StatusCode

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * DataDogClient test suite.
 */
class DataDogClientSpec extends AbstractITSpec {

  import Authenticator._

  implicit val formats = DefaultFormats
  implicit val serialization = org.json4s.native.Serialization

  val client = DataDogClient()
  val apiClient = new DataDogClient(sys.env(ApiKeyEnv), None, DataDogSite.US)
  val noAuthClient = new DataDogClient("invalid key", None)

  "DataDogClient" should {
    "fail for missing api key" in {
      an[IllegalArgumentException] mustBe thrownBy(new DataDogClient("", None))
    }
  }

  "httpGet" should {
    "return response for valid requests" in {
      val result = client.httpGet[TestResponse]("v1/validate", AddQueryParam)
      result.futureValue mustBe TestResponse(true)
    }
    "throw backend error for unsuccsessful requests" in {
      val result = noAuthClient.httpGet[String]("v1/validate", AddHeaders)
      inside(result.failed.futureValue) {
        case e: DataDogApiError => e.code mustBe StatusCode.Forbidden.code
      }
    }
  }

  "httpPost" should {
    "return response for valid requests" in {
      val result = apiClient.httpPost[TestRequest, JValue]("v1/events", TestRequest("test", "test event"), AddQueryParam)
      val json = result.futureValue
      json \ "status" mustBe JString("ok")
      json \ "event" \ "title" mustBe JString("test")
    }
    "throw backend error for unsuccsessful requests" in {
      val result = noAuthClient.httpGet[String]("v1/validate", AddQueryParam)
      inside(result.failed.futureValue) {
        case e: DataDogApiError => e.code mustBe StatusCode.Forbidden.code
      }
    }
  }
}

/**
 * Aux classes for DataDogClient testing.
 */
object DataDogClientSpec {

  case class TestResponse(valid: Boolean)

  case class TestRequest(title: String, text: String)

}