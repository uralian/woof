// general project attributes
organization in ThisBuild := "com.uralian"
organizationName in ThisBuild := "Uralian"
version in ThisBuild := "0.1.0"

scalaVersion in ThisBuild := "2.12.8"

// build options
scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding", "UTF-8"
)
ThisBuild / scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings")

// scoverage options
coverageHighlighting in ThisBuild := true
coverageMinimum in ThisBuild := 80

lazy val root = project.in(file("."))
  .settings(name := "woof-core")
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(
    libraryDependencies ++= commonDependencies ++ testDependencies ++ itDependencies,
    trapExit := false
  )

lazy val commonDependencies = Seq(
  "com.typesafe" % "config" % "1.4.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.clapper" %% "grizzled-slf4j" % "1.3.4",
  "com.beachape" %% "enumeratum-json4s" % "1.5.15",
  "org.json4s" %% "json4s-native" % "3.6.7",
  "com.softwaremill.sttp.client" %% "core" % "2.0.6",
  "com.softwaremill.sttp.client" %% "json4s" % "2.0.6",
  "com.softwaremill.sttp.client" %% "async-http-client-backend-future" % "2.0.6"
)

lazy val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
  "org.mockito" % "mockito-core" % "2.23.4" % Test
)

lazy val itDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % IntegrationTest,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % IntegrationTest,
  "org.mockito" % "mockito-core" % "2.23.4" % IntegrationTest
)
