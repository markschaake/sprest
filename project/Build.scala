import sbt._
import Keys._

object Build extends Build {

  import BuildSettings._
  import Dependencies._

  lazy val root = Project("root", file("."))
    .aggregate(core, sprestSlick)
    .settings(basicSettings: _*)
    .settings(noPublishing: _*)

  lazy val core = Project("sprest-core", file("sprest-core"))
    .settings(sprestModuleSettings: _*)
    .settings(libraryDependencies ++=
      provided(akkaActor) ++
      compile(sprayCan) ++
      compile(sprayRouting) ++
      compile(sprayJson) ++
      test(specs2))

  lazy val sprestSlick = Project("sprest-slick", file("sprest-slick"))
    .dependsOn(core)
    .settings(sprestModuleSettings: _*)
    .settings(libraryDependencies ++=
      compile(slick) ++
      test(specs2))
}
