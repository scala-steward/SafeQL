package fr.loicknuchel.safeql.testingutils.database.tables

import java.time.Instant

import fr.loicknuchel.safeql.Table._
import fr.loicknuchel.safeql._
import fr.loicknuchel.safeql.testingutils.Entities._

/**
 * Hello
 *
 * Class generated by fr.loicknuchel.safeql.gen.writer.ScalaWriter
 */
class FEATURED private(getAlias: Option[String] = None) extends Table.SqlTable("PUBLIC", "featured", getAlias) {
  type Self = FEATURED

  val POST_ID: SqlFieldRef[Post.Id, FEATURED, POSTS] = SqlField(this, "post_id", "INT NOT NULL", JdbcType.Integer, nullable = false, 1, POSTS.table.ID)
  val BY: SqlFieldRef[User.Id, FEATURED, USERS] = SqlField(this, "by", "INT NOT NULL", JdbcType.Integer, nullable = false, 2, USERS.table.ID)
  val START: SqlField[Instant, FEATURED] = SqlField(this, "start", "TIMESTAMP NOT NULL", JdbcType.Timestamp, nullable = false, 3)
  val STOP: SqlField[Instant, FEATURED] = SqlField(this, "stop", "TIMESTAMP NOT NULL", JdbcType.Timestamp, nullable = false, 4)

  override def getFields: List[SqlField[_, FEATURED]] = List(POST_ID, BY, START, STOP)

  override def getSorts: List[Sort] = List()

  override def searchOn: List[SqlField[_, FEATURED]] = List(POST_ID, BY, START, STOP)

  override def getFilters: List[Filter] = List()

  def alias(alias: String): FEATURED = new FEATURED(Some(alias))
}

private[database] object FEATURED {
  val table = new FEATURED() // table instance, should be accessed through `fr.loicknuchel.safeql.testingutils.database.Tables` object
}