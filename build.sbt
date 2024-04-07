import Dependencies._
import BuildHelper._

ThisBuild / version                := "0.1.0-SNAPSHOT"
ThisBuild / organization           := "io.github.ragazoor"
ThisBuild / publishTo              := sonatypePublishToBundle.value
ThisBuild / sonatypeCredentialHost := "central.sonatype.com"

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val root =
  project
    .in(file("."))
    .settings(publish / skip := true)
    .aggregate(result, benchmark, examples)

lazy val result = module("attempt", "result")
  .enablePlugins(BuildInfoPlugin)
  .settings(buildInfoSettings("io.github.ragazoor"))
  .settings(libraryDependencies += munit % Test)
  .settings(stdSettings("attempt"))

lazy val examples = module("examples", "examples")
  .enablePlugins(BuildInfoPlugin)
  .settings(buildInfoSettings("io.github.ragazoor"))
  .settings(libraryDependencies += munit % Test)
  .settings(publish / skip := true)
  .settings(stdSettings("examples"))
  .dependsOn(result)

lazy val benchmark = module("typed-future-benchmark", "benchmark")
  .enablePlugins(BuildInfoPlugin, JmhPlugin)
  .settings(buildInfoSettings("io.github.ragazoor"))
  .settings(libraryDependencies += munit % Test)
  .settings(publish / skip := true)
  .settings(stdSettings("benchmark"))
  .dependsOn(result)

def module(moduleName: String, fileName: String): Project =
  Project(moduleName, file(fileName))
    .settings(stdSettings(moduleName))

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
