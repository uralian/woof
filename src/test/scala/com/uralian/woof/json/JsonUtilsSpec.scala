package com.uralian.woof.json

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.json.JsonUtilsSpec.TestData
import org.json4s.JsonDSL._
import org.json4s._

/**
 * JsonUtils test suite.
 */
class JsonUtilsSpec extends AbstractUnitSpec {

  import JsonUtils._

  "combine()" should {
    "combine partial functions" in {
      val pf1: PartialFunction[Int, String] = {
        case 1 => "one"
        case 5 => "five"
      }
      val pf2: PartialFunction[Int, String] = {
        case 2 => "two"
      }
      val pf3: PartialFunction[Int, String] = {
        case 4 => "four"
      }
      val pf = combine(pf1, pf2, pf3)
      pf(1) mustBe "one"
      pf(2) mustBe "two"
      pf(4) mustBe "four"
      pf(5) mustBe "five"
      a[MatchError] mustBe thrownBy(pf(0))
      a[MatchError] mustBe thrownBy(pf(3))
    }
  }

  "renameFieldsToJson()" should {
    "produce a FieldSerializer" in {
      val pf = renameFieldsToJson("a" -> "AAA", "b" -> "BBB")
      pf("a" -> 1) mustBe Some("AAA" -> 1)
      pf("b" -> 2) mustBe Some("BBB" -> 2)
      a[MatchError] mustBe thrownBy(pf("c" -> "?"))
    }
  }

  "renameFieldsFromJson()" should {
    "produce a FieldSerializer" in {
      val pf = renameFieldsFromJson("AAA" -> "a", "BBB" -> "b")
      pf("AAA" -> JInt(1)) mustBe "a" -> JInt(1)
      pf("BBB" -> JInt(1)) mustBe "b" -> JInt(1)
      a[MatchError] mustBe thrownBy(pf("CCC" -> JString("?")))
    }
  }

  "translateFields()" should {
    val fs = translateFields[TestData]("relatedEventId" -> "related_id", "eventSource" -> "source")
    implicit val formats = DefaultFormats + fs
    "translate Scala field names to JSON" in {
      val data = TestData("abcde", 1111L, 123)
      val json = Extraction.decompose(data)
      json mustBe ("value" -> 123) ~ ("source" -> "abcde") ~ ("related_id" -> 1111L)
    }
    "translate JSON field names to Scala" in {
      val json = ("source" -> "abcde") ~ ("related_id" -> 1111L) ~ ("value" -> 123)
      val data = json.extract[TestData]
      data mustBe TestData("abcde", 1111L, 123)
    }
  }

  "instantSerializerAsSeconds" should {
    implicit val formats = DefaultFormats + instantSerializerAsSeconds
    val seconds = System.currentTimeMillis()
    "translate java.time.Instant to JSON as number of seconds since Epoch" in {
      val time = java.time.Instant.ofEpochSecond(seconds)
      val json = Extraction.decompose(time)
      json mustBe JLong(seconds)
    }
    "translate number of seconds as BigInt since epoch into java.time.Instant" in {
      val json: JValue = JInt(seconds)
      val time = json.extract[java.time.Instant]
      time.getEpochSecond mustBe seconds
    }
    "translate number of seconds as Long since epoch into java.time.Instant" in {
      val json: JValue = JLong(seconds)
      val time = json.extract[java.time.Instant]
      time.getEpochSecond mustBe seconds
    }
  }

  "urlSerializer" should {
    implicit val formats = DefaultFormats + urlSerializer
    val str = "http://www.example.com:8080/secret"
    "translate java.net.URL into JSON string" in {
      val url = new java.net.URL(str)
      val json = Extraction.decompose(url)
      json mustBe JString(str)
    }
    "translate JSON string into java.net.URL" in {
      val json: JValue = str
      val url = json.extract[java.net.URL]
      url.toExternalForm mustBe str
    }
  }
}

/**
 * Factory for test classes.
 */
object JsonUtilsSpec {

  final case class TestData(eventSource: String, relatedEventId: Long, value: Int)

}
