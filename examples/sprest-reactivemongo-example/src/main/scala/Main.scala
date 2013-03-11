package sprest.examples.reactivemongo

import spray.routing.SimpleRoutingApp
import twirl.api._
import spray.httpx.TwirlSupport
import spray.httpx.encoding.Gzip
import spray.can.server.SprayCanHttpServerApp

object Main extends App
  with SprayCanHttpServerApp
  with SimpleRoutingApp
  with TwirlSupport
  with spray.httpx.SprayJsonSupport
  with Routes {

  startServer(interface = "localhost", port = 8080) {
    routes
  }
}
