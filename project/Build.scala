import sbt._
import Keys._

object Build extends Build {

  import BuildSettings._
  import Dependencies._

  lazy val root = Project("root", file("."))
    .aggregate(core, sprestSlick, sprestReactiveMongo, sprestAuth)
    .settings(basicSettings: _*)
    .settings(noPublishing: _*)

  lazy val core = Project("sprest-core", file("sprest-core"))
    .settings(sprestModuleSettings: _*)
    .settings(libraryDependencies ++=
      provided(akkaActor) ++
      compile(logback) ++
      compile(sprayCan) ++
      compile(sprayRouting) ++
      compile(sprayJson) ++
      compile(joda) ++
      compile(jodaConvert) ++
      test(specs2) ++
      test(sprayTestKit))

  lazy val sprestSlick = Project("sprest-slick", file("sprest-slick"))
    .dependsOn(core)
    .settings(sprestModuleSettings: _*)
    .settings(libraryDependencies ++=
      compile(slick) ++
      compile(joda) ++
      compile(jodaConvert) ++
      test(specs2))

  lazy val sprestReactiveMongo = Project("sprest-reactivemongo", file("sprest-reactivemongo"))
    .dependsOn(core)
    .settings(sprestModuleSettings: _*)
    .settings(libraryDependencies ++=
      compile(reactiveMongo) ++
      compile(joda) ++
      compile(jodaConvert) ++
      test(specs2))

  lazy val sprestAuth = Project("sprest-security", file("sprest-security"))
    .settings(sprestModuleSettings: _*)
    .settings(libraryDependencies ++=
      test(specs2))
}
