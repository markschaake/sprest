package sprest.security

trait Permission

trait Role {
  def permissions: List[Permission]
  def hasPermission(p: Permission) = permissions.contains(p)
  def hasPermissions(ps: Permission*) = ps forall { hasPermission }
}
