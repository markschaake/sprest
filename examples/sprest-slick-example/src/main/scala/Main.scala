package sprest.examples.slick

import akka.actor.ActorSystem
import spray.routing.SimpleRoutingApp
import spray.httpx.TwirlSupport
import spray.httpx.encoding.Gzip

object Main extends App
  with SimpleRoutingApp
  with spray.httpx.SprayJsonSupport
  with Routes
  with DB {

  override implicit val system = ActorSystem("sprest-reactive-mongo")

  startServer(interface = "localhost", port = 8080) {
    routes
  }
}

