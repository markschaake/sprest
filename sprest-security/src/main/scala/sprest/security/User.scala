package sprest.security

trait User {
  type ID
  type Permission
  type Role

  def userId: ID
  def hasPermission(permission: Permission): Boolean
  def hasRole(role: Role): Boolean

  def hasPermissions(permissions: Permission*) = permissions forall { hasPermission }
  def hasRoles(roles: Role*) = roles forall { hasRole }
}
