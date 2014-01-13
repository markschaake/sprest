# Example of using sprest-reactivemongo #

This example application illustrates the following funtionality:

* Connect to MongoDB in a reactive way (using ReactiveMongo behind the scenes)
* Generate REST CRUD routes off of DAOs
* Use of sprest.util.enum.Enum for To Do priorities
* Integrate Coffeescript and Less CSS compilers
* AngularJS based frontend

## Pre-requisites ##

* You must have MongoDB installed locally and running on the default port (27017)
* Requires SBT 0.13 launcher

## Run the example app ##

`sbt run`

Then navigate to http://localhost:8081 and use the app.
