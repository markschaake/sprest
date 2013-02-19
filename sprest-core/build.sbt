resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

scalaVersion := "2.10.0"

organization := "sprest"

name := "sprest-core"

version := "0.1.0-SNAPSHOT"

isSnapshot := true

publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/projects/markschaake.github.com/snapshots")))

libraryDependencies ++= Seq(
  "io.spray" % "spray-can" % "1.1-M7",
  "io.spray" % "spray-routing" % "1.1-M7",
  "io.spray" %% "spray-json" % "1.2.3",
  "io.spray" % "spray-testkit" % "1.1-M7" % "test"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.1.0",
  "ch.qos.logback" % "logback-classic" % "1.0.1",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.3",
  "org.specs2" %% "specs2" % "1.13" % "test"
)

