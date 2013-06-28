package sprest.security

trait User {
  type ID

  type R <: Role
  type P <: Permission

  def userId: ID

  def roles: Set[R] = Set.empty
  def additionalPermissions: Set[P] = Set.empty

  lazy val allPermissions = roles.map(_.permissions).flatten ++ additionalPermissions

  def hasPermission(permission: P): Boolean = allPermissions.contains(permission)
  def hasRole(role: R): Boolean = roles.contains(role)

  def hasPermissions(permissions: P*) = permissions forall { hasPermission }
  def hasRoles(roles: R*) = roles forall { hasRole }
}
