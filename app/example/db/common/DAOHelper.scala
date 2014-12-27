package example.db.common

/** Common functionality that needs to work with types from the DAO
  * but in a DAO-independent way.
  */
class DAOHelper(val dao: DAO) {
  import dao.driver.simple._

  def restrictKey[C[_]](s: String, q: Query[DAO#Props, _, C]) =
    q.filter(_.key === s)
}