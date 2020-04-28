package com.uralian.woof.api

import com.uralian.woof.AbstractUnitSpec

/**
 * MetricQuery test suite.
 */
class MetricQuerySpec extends AbstractUnitSpec {

  import MetricAggregator._
  import MetricQuery._

  "FreeformQuery" should {
    "allow direct text query" in {
      text("avg:system.cpu.user{$myvar}by{host}").q mustBe "avg:system.cpu.user{$myvar}by{host}"
    }
  }

  "CompoundQuery" should {
    val q1: MetricQuery = "q1"
    "combine queries via +" in {
      (q1 + "q2").q mustBe text("q1+q2").q
    }
    "combine queries via -" in {
      (q1 - "q2").q mustBe text("q1-q2").q
    }
    "combine queries via *" in {
      (q1 * "q2").q mustBe text("q1*q2").q
    }
    "combine queries via /" in {
      (q1 / "q2").q mustBe text("q1/q2").q
    }
    "combine queries via arbitrary separator" in {
      q1.combine("^")("q2").q mustBe text("q1^q2").q
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
    "wrap in a function" in {
      val query = metric("system.cpu.user").filterBy("env:dev").groupBy("host")
      val q2 = query
        .wrapIn("top", 10, "'mean'", "'asc'")
        .wrapIn("bottom", 20, "'last'", "'desc'")
      q2.q mustBe """bottom(top(avg:system.cpu.user{env:dev}by{host},10,'mean','asc'),20,'last','desc')"""
    }
    "apply transformation functions" in {
      val query = metric("system.cpu.user").filterBy("client:abc").groupBy("host", "env")
      val q2 = query.transform(q => "top(" + q + ")").transform(q => "bottom(" + q + ")")
      q2.q mustBe "bottom(top(avg:system.cpu.user{client:abc}by{host,env}))"
    }
  }
}
