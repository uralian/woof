package com.uralian.woof.api

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.api.dsl._
import com.uralian.woof.api.tags._
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.Serialization

/**
 * TagsApi test suite.
 */
class TagsApiSpec extends AbstractUnitSpec {

  "TagsUpsertRequest" should {
    "produce valid payload with source" in {
      val request = TagsUpsertRequest(List[Tag]("a" -> "a1", "b" -> "b1"), Some("abc"))
      val json = Extraction.decompose(request)
      json mustBe ("tags" -> List("a:a1", "b:b1")) ~ ("source" -> "abc")
    }
    "produce valid payload without source" in {
      val request = TagsUpsertRequest(List[Tag]("a" -> "a1", "b" -> "b1"), None)
      val json = Extraction.decompose(request)
      json mustBe (("tags" -> List("a:a1", "b:b1")): JValue)
    }
  }

  "TagsResponse" should {
    "deserialize from valid JSON" in {
      val json = """{"tags": ["a:aaa", "b:bbb", "c:ccc"]}"""
      val response = Serialization.read[TagsResponse](json)
      response mustBe TagsResponse(List[Tag]("a" -> "aaa", "b" -> "bbb", "c" -> "ccc"))
    }
  }
}
