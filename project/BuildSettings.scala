import sbt._
import Keys._

object BuildSettings {

  val VERSION = "0.3.0"

  def versionIsSnapshot = VERSION.endsWith("SNAPSHOT")

  def publishDir = {
    if (versionIsSnapshot)
      new File(Path.userHome.absolutePath + "/projects/markschaake.github.com/snapshots")
    else
      new File(Path.userHome.absolutePath + "/projects/markschaake.github.com/releases")
  }

  lazy val noPublishing = Seq(
    publish := Nil,
    publishLocal := Nil)

  lazy val basicSettings = Seq(
    version := VERSION,
    organization := "sprest",
    description := "A suite of libraries leveraging Spray",
    startYear := Some(2013),
    licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalaVersion := "2.10.3",
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
      "-Ywarn-adapted-args"))

  lazy val sprestModuleSettings =
    basicSettings ++
      Seq(
        version := VERSION,
        isSnapshot := versionIsSnapshot,
        publishTo := Some(Resolver.file("file", publishDir)))
}
