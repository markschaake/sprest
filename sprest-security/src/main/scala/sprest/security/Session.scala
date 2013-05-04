package sprest.security

trait Session {
  type SessionID

  def sessionId: SessionID
  def user: User
}
