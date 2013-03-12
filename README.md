## Sprest - REST easy with Spray ##

Sprest is a collection of libaries to make building REST services simpler using [Spray](http://spray.io).

Even though it is very easy to build REST services with Spray out of the box, as a learning exercise I decided to build some libraries to make working with specific technologies easier.

## Libraries ##

### sprest-core ###
Required by other sprest libraries. Defines model and DAO conventions as well as provides REST routing generators.

### sprest-slick ###
Provides [Slick](http://slick.typesafe.com/) DAO implementation.

### sprest-reactivemongo ###
Provides [ReactiveMongo](http://reactivemongo.org/) DAO implementation.

## SBT ##
Sprest is published to a GitHub-based repository. To use it, add the following resolver:

    resolvers += "sprest snapshots" at "http://markschaake.github.com/snapshots"

Then you can include sprest libraries:

    libraryDependencies += "sprest" %% "sprest-core % "0.1.0-SNAPSHOT"
	libraryDependencies += "sprest" %% "sprest-slick" % "0.1.0-SNAPSHOT"
	libraryDependencies += "sprest" %% "sprest-reactivemongo % "0.1.0-SNAPSHOT"

Note: it is unnecessary to explicitly declare the "sprest-core" dependency if you declare a dependency on a library that depends upon the core (such as sprest-slick or sprest-reactivemongo).
