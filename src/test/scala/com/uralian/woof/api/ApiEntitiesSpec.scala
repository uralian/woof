package com.uralian.woof.api

import com.uralian.woof.AbstractUnitSpec
import org.json4s._
import com.uralian.woof.api.dsl._

/**
 * API entities test suite.
 */
class ApiEntitiesSpec extends AbstractUnitSpec {

  "Tag" should {
    implicit val formats = DefaultFormats + Tag.serializer
    "convert name and value to lowercase" in {
      Tag("myTAG", "Secret_Value") mustBe Tag("mytag", "secret_value")
    }
    "validate tag name and value" in {
      noException must be thrownBy Tag("abc/1.2.3/x_y-z", "abc/1.2.3/x_y-z:mmmm")
      a[RuntimeException] must be thrownBy Tag("a:b", "mmmm")
      a[RuntimeException] must be thrownBy Tag("abc", "xy+c")
      noException must be thrownBy Tag("a" * 100, "b" * 100)
      a[RuntimeException] must be thrownBy Tag("a" * 100, "b" * 101)
    }
    "split string into name and value and convert them to lowercase" in {
      Tag("AaA:BBB") mustBe Tag("aaa", "bbb")
      Tag("AaA:BBB:ccc:ddd") mustBe Tag("aaa", "bbb:ccc:ddd")
    }
    "fail on invalid string format" in {
      a[RuntimeException] mustBe thrownBy(Tag("abcde"))
      a[RuntimeException] mustBe thrownBy(Tag("xy+c:123"))
      a[RuntimeException] must be thrownBy Tag("a" * 100 + ":" + "b" * 101)
    }
    "render toString() as key:value" in {
      Tag("abc", "_d_").toString mustBe "abc:_d_"
    }
    "serialize to JSON" in {
      Extraction.decompose(Tag("abc", "xyz")) mustBe JString("abc:xyz")
    }
    "deserialize from JSON" in {
      JString("abc:xyz").extract[Tag] mustBe Tag("abc", "xyz")
      a[MappingException] mustBe thrownBy(JString("abcde").extract[Tag])
    }
  }

  "TagName" should {
    implicit val formats = DefaultFormats + TagName.serializer
    "accept valid strings" in {
      noException must be thrownBy TagName("abc_12")
    }
    "fail on invalid strings" in {
      a[RuntimeException] must be thrownBy TagName("a:b")
    }
    "render toString() as name" in {
      TagName("abc").toString mustBe "abc"
    }
    "serialize into JSON" in {
      Extraction.decompose(TagName("abc")) mustBe JString("abc")
    }
    "deserialize from JSON" in {
      JString("abc").extract[TagName] mustBe TagName("abc")
      a[MappingException] must be thrownBy JString("ab:c").extract[TagName]
    }
  }

  "VarName" should {
    implicit val formats = DefaultFormats + VarName.serializer
    "accept valid strings" in {
      noException must be thrownBy VarName("$abc")
    }
    "fail on invalid strings" in {
      a[RuntimeException] must be thrownBy VarName("ab")
    }
    "render toString() as name" in {
      VarName("$abc").toString mustBe "$abc"
    }
    "serialize into JSON" in {
      Extraction.decompose(VarName("$abc")) mustBe JString("$abc")
    }
    "deserialize from JSON" in {
      JString("$abc").extract[VarName] mustBe VarName("$abc")
      a[MappingException] must be thrownBy JString("abc").extract[VarName]
    }
  }

  "ScopeElement" should {
    implicit val formats = DefaultFormats + ScopeElement.serializer
    "resolve string as a Tag" in {
      ScopeElement("abc:def") mustBe Tag("abc", "def")
    }
    "resolve string as a Tag name" in {
      ScopeElement("abcde") mustBe TagName("abcde")
    }
    "resolve string as a Var name" in {
      ScopeElement("$ab12") mustBe VarName("$ab12")
    }
    "fail string resolution for invalid input" in {
      a[RuntimeException] must be thrownBy ScopeElement("$ab:cde")
      a[RuntimeException] must be thrownBy ScopeElement("abc+def")
    }
    "serialize to JSON" in {
      Extraction.decompose(Tag("abc", "def:xyz")) mustBe JString("abc:def:xyz")
      Extraction.decompose(TagName("abc")) mustBe JString("abc")
      Extraction.decompose(VarName("$abc")) mustBe JString("$abc")
    }
    "deserialize from JSON" in {
      JString("abc:def:gh").extract[ScopeElement] mustBe Tag("abc", "def:gh")
      JString("abc").extract[ScopeElement] mustBe TagName("abc")
      JString("$abc").extract[ScopeElement] mustBe VarName("$abc")
      a[MappingException] must be thrownBy JString("a+b").extract[ScopeElement]
    }
  }

  "Scope" should {
    implicit val formats = DefaultFormats + Scope.serializer
    "resolve * as All" in {
      Scope("*") mustBe Scope.All
    }
    "resolve element list as Filter" in {
      Scope("ab:1, ab:2,  c:3") mustBe Scope.Filter(Tag("ab", "1"), Tag("ab", "2"), Tag("c", "3"))
      Scope("ab,c:de,$fg,h:i:j") mustBe Scope.Filter(TagName("ab"), Tag("c", "de"), VarName("$fg"), Tag("h", "i:j"))
    }
    "serialize to JSON" in {
      Extraction.decompose(Scope.All) mustBe JString("*")
      Extraction.decompose(Scope.Filter(TagName("ab"), Tag("c", "de"), VarName("$fg"))) mustBe JString("ab,c:de,$fg")
    }
    "deserialize from JSON" in {
      JString("*").extract[Scope] mustBe Scope.All
      JString("ab,c:de,$fg").extract[Scope] mustBe Scope.Filter(TagName("ab"), Tag("c", "de"), VarName("$fg"))
    }
  }

  "Implicits" should {
    "convert (name,value) string pair into a tag" in {
      (("ab" -> "cd"): ScopeElement) mustBe Tag("ab", "cd")
    }
    "convert 'name:value' string into a tag" in {
      (("ab:cd"): ScopeElement) mustBe Tag("ab", "cd")
    }
    "convert 'name' string into a tag name" in {
      (("ab"): ScopeElement) mustBe TagName("ab")
    }
    "convert '$name' string into a var name" in {
      (("$ab"): ScopeElement) mustBe VarName("$ab")
    }
    "allow mixed scope elements for filter arguments" in {
      Scope.Filter("ab", "c" -> "d", "$ef", "g:h") mustBe
        Scope.Filter(TagName("ab"), Tag("c", "d"), VarName("$ef"), Tag("g", "h"))
    }
  }
}
