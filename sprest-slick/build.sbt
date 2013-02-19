resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Mark Schaake" at "http://markschaake.github.com/snapshots"

scalaVersion := "2.10.0"

organization := "sprest"

name := "sprest-slick"

version := "0.1.0-SNAPSHOT"

isSnapshot := true

publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/projects/markschaake.github.com/snapshots")))

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "sprest" %% "sprest-core" % "0.1.0-SNAPSHOT",
  "com.typesafe.slick" %% "slick" % "1.0.0",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "org.specs2" %% "specs2" % "1.13" % "test"
)

