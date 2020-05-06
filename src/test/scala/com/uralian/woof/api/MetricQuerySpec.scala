package com.uralian.woof.api

import com.uralian.woof.AbstractUnitSpec
import com.uralian.woof.api.dsl._

/**
 * MetricQuery test suite.
 */
class MetricQuerySpec extends AbstractUnitSpec {

  import MetricAggregator._
  import MetricQuery._

  "FreeformQuery" should {
    "allow direct text query" in {
      direct("avg:system.cpu.user{$myvar}by{host}").q mustBe "avg:system.cpu.user{$myvar}by{host}"
    }
  }

  "BinOpQuery" should {
    val q1: MetricQuery = "q1"
    "combine queries via +" in {
      (q1 + "q2").q mustBe direct("q1+q2").q
    }
    "combine queries via -" in {
      (q1 - "q2").q mustBe direct("q1-q2").q
    }
    "combine queries via *" in {
      (q1 * "q2").q mustBe direct("q1*q2").q
    }
    "combine queries via /" in {
      (q1 / "q2").q mustBe direct("q1/q2").q
    }
    "combine queries via arbitrary separator" in {
      q1.combine("^", "q2").q mustBe direct("q1^q2").q
    }
  }

  "FuncQuery" should {
    "provide a function without arguments" in {
      function("sum")().q mustBe "sum()"
    }
    "provide a function with one argument" in {
      function("sum")("123").q mustBe "sum(123)"
    }
    "provide a function with multiple arguments" in {
      function("sum")("123", "avg:system.mem.free{*}", "'abc'").q mustBe "sum(123,avg:system.mem.free{*},'abc')"
    }
  }

  "QueryBuilder" should {
    "default to AVG aggregation and ALL scope" in {
      val query = metric("system.cpu.user")
      query mustBe QueryBuilder(metric = "system.cpu.user")
      query.q mustBe "avg:system.cpu.user{*}"
    }
    "customize aggregation" in {
      val query = metric("system.cpu.user").aggregate(Max)
      query mustBe QueryBuilder(metric = "system.cpu.user", aggregator = Max)
      query.q mustBe "max:system.cpu.user{*}"
    }
    "customize scope" in {
      val query = metric("system.cpu.user").filterBy("env" -> "qa", "$myvar", "a:b")
      query mustBe QueryBuilder("system.cpu.user", Avg, Scope.Filter(Tag("env", "qa"), VarName("$myvar"), Tag("a", "b")))
      query.q mustBe "avg:system.cpu.user{env:qa,$myvar,a:b}"
    }
    "customize grouping" in {
      val query = metric("system.cpu.user").groupBy("host", "env")
      query mustBe QueryBuilder("system.cpu.user", groupBy = Seq(TagName("host"), TagName("env")))
      query.q mustBe "avg:system.cpu.user{*}by{host,env}"
    }
  }
}
