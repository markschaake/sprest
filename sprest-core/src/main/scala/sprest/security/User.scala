package sprest.security

trait User {
  type ID

  def userId: ID

  def roles: Set[Role] = Set.empty
  def additionalPermissions: Set[Permission] = Set.empty

  lazy val allPermissions = roles.map(_.permissions).flatten ++ additionalPermissions

  def hasPermission(permission: Permission): Boolean = allPermissions.contains(permission)
  def hasRole(role: Role): Boolean = roles.contains(role)

  def hasPermissions(permissions: Permission*) = permissions forall { hasPermission }
  def hasRoles(roles: Role*) = roles forall { hasRole }
}
