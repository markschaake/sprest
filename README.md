**Notice: this project is no longer maintained**

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

### sprest-reactivemongo ###
Provides [ReactiveMongo](http://reactivemongo.org/) DAO implementation.

## SBT ##
Sprest is published to Bintray for scala 2.10 and 2.11, and is available on JCenter. To use it, add on of the following resolvers:

    resolvers += Resolver.bintrayRepo("markschaake", "maven")
    -- or --
    resolvers += Resolver.jcenterRepo

Then you can include sprest libraries:

    libraryDependencies += "sprest" %% "sprest-core" % "0.3.12"
    libraryDependencies += "sprest" %% "sprest-reactivemongo" % "0.3.12"

Note: it is unnecessary to explicitly declare the "sprest-core" dependency if you declare a dependency on `sprest-reactivemongo`.
