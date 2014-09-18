resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/service/local/repositories/snapshots/content"

resolvers += "Sprest Snapshots" at "http://sprest.io/snapshots"

scalaVersion := "2.10.4"

name := "sprest-reactivemongo-example"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.3",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.6",
  "sprest" %% "sprest-reactivemongo" % "0.3.3-SNAPSHOT",
  "org.specs2" %% "specs2" % "2.3.10" % "test"
)

seq(Revolver.settings: _*)

seq(coffeeSettings: _*)

seq(lessSettings:_*)
