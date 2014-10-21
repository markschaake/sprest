import sbt._
import Keys._
import sbtrelease.ReleasePlugin._

object BuildSettings {

  def publishDir(subDir: String) = new File(Path.userHome.absolutePath + s"/projects/sprest-hg-pages/$subDir")

  lazy val noPublishing = Seq(
    publish := Nil,
    publishLocal := Nil)

  lazy val basicSettings = releaseSettings ++ Seq(
    organization := "sprest",
    description := "A suite of libraries leveraging Spray",
    startYear := Some(2013),
    licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalaVersion := "2.10.4",
    crossScalaVersions := Seq("2.10.4", "2.11.2"),
    resolvers ++= Dependencies.resolutionRepos,
    scalacOptions := Seq(
      "-encoding", "utf8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-target:jvm-1.6",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-Xlog-reflective-calls",
      "-Ywarn-adapted-args"),
    publishTo := {
      if (isSnapshot.value)
        Some(Resolver.file("file", publishDir("snapshots")))
      else
        Some(Resolver.file("file", publishDir("releases")))
    })

  lazy val sprestModuleSettings = basicSettings
}
