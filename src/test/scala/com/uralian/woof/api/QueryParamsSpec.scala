package com.uralian.woof.api

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.api.QueryParamsSpec.TestData
import org.json4s.DefaultFormats

/**
 * QueryParams test suite.
 */
class QueryParamsSpec extends AbstractUnitSpec {

  implicit val formats = DefaultFormats

  "toParams" should {
    "convert an object to parameters" in {
      val data = TestData("john", None, true)
      val params = data.toParams
      params mustBe List("name" -> "john", "enrolled" -> true)
    }
  }

  "Empty.toParams" should {
    "yield an empty list" in {
      QueryParams.Empty.toParams mustBe empty
    }
  }
}

/**
 * Provides data for QueryParam tests.
 */
object QueryParamsSpec {

  case class TestData(name: String, age: Option[Int], enrolled: Boolean) extends QueryParams

}

