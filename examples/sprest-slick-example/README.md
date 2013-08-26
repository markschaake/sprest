# Example of using sprest-reactivemongo #

This example application illustrates the following funtionality:

* Connect to a SQL database via Slick
* Defer to application.conf for database configuration
* Generate REST CRUD routes off of DAOs
* Integrate Coffeescript and Less CSS compilers
* AngularJS based frontend

## Pre-requisites ##

* Requires SBT 0.13 launcher
* Modify `src/main/resources/application.conf` for your database configuration
* Schema of your database must basically match the Postgres-specific schema in `src/main/resources/schema-pg.sql`

## Run the example app ##

`sbt run`

Then navigate to http://localhost:8080 and use the app.
