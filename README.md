## Sprest - companion libraries to Spray ##

[![Build Status](https://travis-ci.org/markschaake/sprest.png)](https://travis-ci.org/markschaake/sprest)

Sprest is a collection of libaries to make building applications simpler using [Spray](http://spray.io). Spray provides a general toolkit for building your own web application stack. Sprest builds on top of the toolkit to provide additional (more implementation-specific) libraries to help with building your application's stack.

## Libraries ##

### sprest-core ###
Required by other sprest libraries.
Provides:

* Model and DAO conventions
* REST routing generators
* Security including password salting and session and user traits

### sprest-slick ###
Provides [Slick](http://slick.typesafe.com/) DAO implementation.

### sprest-reactivemongo ###
Provides [ReactiveMongo](http://reactivemongo.org/) DAO implementation.

## SBT ##
Sprest is published to a GitHub-based repository. To use it, add the following resolver:

    resolvers += "sprest releases" at "http://markschaake.github.io/releases"

Then you can include sprest libraries:

    libraryDependencies += "sprest" %% "sprest-core" % "0.3.2"
    libraryDependencies += "sprest" %% "sprest-slick" % "0.3.2"
    libraryDependencies += "sprest" %% "sprest-reactivemongo" % "0.3.2"

Note: it is unnecessary to explicitly declare the "sprest-core" dependency if you declare a dependency on a library that depends upon the core (such as sprest-slick or sprest-reactivemongo).
