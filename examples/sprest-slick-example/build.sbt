resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Mark Schaake" at "http://markschaake.github.com/snapshots"

scalaVersion := "2.10.2"

name := "sprest-slick-example"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "com.typesafe.akka" %% "akka-actor" % "2.2.1",
  "ch.qos.logback" % "logback-classic" % "1.0.1",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.3",
  "sprest" %% "sprest-slick" % "0.3.0-SNAPSHOT",
  "org.specs2" %% "specs2" % "2.2" % "test"
)

seq(Revolver.settings: _*)

seq(coffeeSettings: _*)

seq(lessSettings:_*)
