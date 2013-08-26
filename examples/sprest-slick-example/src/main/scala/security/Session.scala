package sprest.examples.reactivemongo.security

import sprest.security.{ Session => SprestSession, User }

case class Session() extends SprestSession {
  type ID = String
  override val sessionId = "singletonsession"
  override val user = new User {
    type ID = String
    override val userId = "singletonuser"
  }

}
