package sprest.examples.reactivemongo

import spray.routing.SimpleRoutingApp
import sprest.routing.RestRoutes

trait Routes extends RestRoutes { this: SimpleRoutingApp with spray.httpx.TwirlSupport =>
  import spray.routing.Directives._
  import spray.httpx.SprayJsonSupport._
  import spray.httpx.encoding.Gzip
  import spray.json._
  import JsonAPI._
  import JsonAPI.Formats._

  val js = pathPrefix("js" / Rest) { fileName =>
    get {
      encodeResponse(Gzip) {
        getFromResource("js/" + fileName)
      }
    }
  }

  val css = pathPrefix("css" / Rest) { fileName =>
    get {
      getFromResource("css/" + fileName)
    }
  }

  val index = path("") {
    get {
      complete {
        html.index.render("Hello, Spray!")
      }
    }
  }

  val bootstrap = pathPrefix("bootstrap" / Rest) { fileName =>
    get {
      getFromResource("twitter/bootstrap/" + fileName)
    }
  }

  val publicAssets = js ~ css ~ bootstrap

  val api = pathPrefix("api") {
    restString("todos", DB.ToDos)
  }

  val routes = index ~ publicAssets ~ api
}
