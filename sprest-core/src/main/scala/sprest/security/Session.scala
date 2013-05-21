package sprest.security

trait Session {
  type ID

  def sessionId: ID
  def user: User
}
