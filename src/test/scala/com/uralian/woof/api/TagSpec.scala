package com.uralian.woof.api

import com.uralian.woof.AbstractUnitSpec
import org.json4s.JsonAST.JString
import org.json4s.{DefaultFormats, Extraction, MappingException}

/**
 * Tag test suite.
 */
class TagSpec extends AbstractUnitSpec {

  "Tag" when {
    "created" should {
      "convert name and value to lowercase" in {
        Tag("myTAG", "Secret_Value") mustBe Tag("mytag", "secret_value")
      }
      "split string into name and value and convert them to lowercase" in {
        Tag("AaA:BBB") mustBe Tag("aaa", "bbb")
      }
      "fail on invalid string format" in {
        a[RuntimeException] mustBe thrownBy(Tag("abcde"))
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
}
