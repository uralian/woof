package com.uralian.woof.api

import com.uralian.woof.AbstractUnitSpec
import org.json4s.{DefaultFormats, Extraction, JString, MappingException}

/**
 * Tag test suite.
 */
class TagSpec extends AbstractUnitSpec {

  "Tag" when {
    "created" should {
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
    }
    "serialized" should {
      "convert Tag into name:value string" in {
        implicit val formats = DefaultFormats + Tag.serializer
        Extraction.decompose(Tag("abc", "xyz")) mustBe JString("abc:xyz")
      }
    }
    "deserialized" should {
      implicit val formats = DefaultFormats + Tag.serializer
      "restore Tag from name:value string" in {
        JString("abc:xyz").extract[Tag] mustBe Tag("abc", "xyz")
      }
      "fail for invalid string format" in {
        a[MappingException] mustBe thrownBy(JString("abcde").extract[Tag])
      }
    }
  }

  "pairToTag" should {
    "implicitly create a Tag from name->value pair" in {
      val tag: Tag = "aBc" -> "XYZ"
      tag.name mustBe "abc"
      tag.value mustBe "xyz"
    }
  }

  "encodeTags" should {
    "convert a list of tags into a string" in {
      val tags = List[Tag]("a" -> "a1", "b" -> "b2", "c" -> "c3")
      encodeTags(tags) mustBe "a:a1,b:b2,c:c3"
    }
  }

  "decodeTags" should {
    "convert a string into a list of tags" in {
      val str = "a:a1,b:b2,c:c3"
      decodeTags(str) mustBe List[Tag]("a" -> "a1", "b" -> "b2", "c" -> "c3")
    }
  }
}