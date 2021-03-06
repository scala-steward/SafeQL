package fr.loicknuchel.safeql

import cats.data.NonEmptyList
import doobie.syntax.string._
import doobie.util.Put
import doobie.util.fragment.Fragment
import doobie.util.fragment.Fragment.const0
import fr.loicknuchel.safeql.Cond._
import fr.loicknuchel.safeql.Field.Order

sealed trait Field[A] {
  val name: String
  val alias: Option[String]

  def ref: Fragment

  def value: Fragment

  def fr: Fragment = value ++ const0(this.alias.map(" as " + _).getOrElse(""))

  def sql: String = fr.query.sql

  def as(alias: String): Field[A]

  def is(value: A)(implicit p: Put[A]): Cond = IsValue(this, value)

  def isNot(value: A)(implicit p: Put[A]): Cond = IsNotValue(this, value)

  def is(field: Field[A]): Cond = IsField(this, field)

  def is(select: Query.Select[A]): Cond = IsQuery(this, select)

  // TODO restrict to fields with sql string type
  def like(value: String): Cond = Like(this, value)

  def notLike(value: String): Cond = NotLike(this, value)

  def ilike(value: String): Cond = ILike(this, value)

  def gt(value: A)(implicit p: Put[A]): Cond = GtValue(this, value)

  def gte(value: A)(implicit p: Put[A]): Cond = GteValue(this, value)

  def lt(value: A)(implicit p: Put[A]): Cond = LtValue(this, value)

  def lte(value: A)(implicit p: Put[A]): Cond = LteValue(this, value)

  def isNull: Cond = IsNull(this)

  def notNull: Cond = NotNull(this)

  def in(values: NonEmptyList[A])(implicit p: Put[A]): Cond = InValues(this, values)

  def notIn(values: NonEmptyList[A])(implicit p: Put[A]): Cond = NotInValues(this, values)

  def in(q: Query.Select[A]): Cond = InQuery(this, q)

  def notIn(q: Query.Select[A]): Cond = NotInQuery(this, q)

  def cond(fr: Fragment): Cond = CustomCond(this, fr)

  def lower: Expr = Expr.Lower(Expr.ValueField(this))

  def asc: Order[A] = Order(this, asc = true, expr = None)

  def desc: Order[A] = Order(this, asc = false, expr = None)
}

object Field {
  def apply[A](query: Query[A], alias: String): QueryField[A] = QueryField(query, Some(alias))

  case class Order[A](field: Field[A], asc: Boolean, expr: Option[String]) {
    def reverse: Order[A] = copy(asc = !asc)

    def fr(nullsFirst: Boolean): Fragment = {
      val value = expr.map(e => const0(e.replaceFirst("\\?", field.value.query.sql))).getOrElse(field.value)
      if (nullsFirst) {
        value ++ fr0" IS NOT NULL, " ++ value ++ (if (asc) fr0"" else fr0" DESC")
      } else {
        value ++ fr0" IS NULL, " ++ value ++ (if (asc) fr0"" else fr0" DESC")
      }
    }
  }

  object Order {
    def apply[A](field: Field[A], asc: Boolean): Order[A] = new Order(field, asc, None)

    def apply[A](field: String, alias: Option[String] = None): Order[A] =
      new Order(TableField(field.stripPrefix("-"), alias), !field.startsWith("-"), expr = None)
  }

}

sealed trait SqlField[A, +T <: Table.SqlTable] extends Field[A] {
  val table: T
  val name: String
  val info: SqlField.JdbcInfo
  val alias: Option[String]

  override def ref: Fragment = const0(s"${table.getAlias.getOrElse(table.getName)}.$name")

  override def value: Fragment = ref

  override def as(alias: String): SqlField[A, T]

  // create a null TableField based on a sql field, useful on union when a field is available on one side only
  def asNull: NullField[A] = NullField[A](alias.getOrElse(name))

  def asNull(name: String): NullField[A] = NullField[A](name)

  def nullable: Boolean = info.nullable
}

object SqlField {

  def apply[A, T <: Table.SqlTable](table: T, name: String, jdbcDeclaration: String, jdbcType: JdbcType, nullable: Boolean, index: Int): SqlFieldRaw[A, T] =
    SqlFieldRaw(table, name, JdbcInfo(nullable, index, jdbcType, jdbcDeclaration), None)

  def apply[A, T <: Table.SqlTable, T2 <: Table.SqlTable](table: T, name: String, jdbcDeclaration: String, jdbcType: JdbcType, nullable: Boolean, index: Int, references: SqlField[A, T2]): SqlFieldRef[A, T, T2] =
    SqlFieldRef(table, name, JdbcInfo(nullable, index, jdbcType, jdbcDeclaration), None, references)

  case class JdbcInfo(nullable: Boolean, index: Int, jdbcType: JdbcType, jdbcDeclaration: String)

}

case class SqlFieldRaw[A, +T <: Table.SqlTable](table: T,
                                                name: String,
                                                info: SqlField.JdbcInfo,
                                                alias: Option[String]) extends SqlField[A, T] {
  override def as(alias: String): SqlFieldRaw[A, T] = copy(alias = Some(alias))

  override def toString: String = s"SqlFieldRaw(${table.getName}.$name)"
}

case class SqlFieldRef[A, T <: Table.SqlTable, T2 <: Table.SqlTable](table: T,
                                                                     name: String,
                                                                     info: SqlField.JdbcInfo,
                                                                     alias: Option[String],
                                                                     references: SqlField[A, T2]) extends SqlField[A, T] {
  override def as(alias: String): SqlFieldRef[A, T, T2] = copy(alias = Some(alias))

  override def toString: String = s"SqlFieldRef(${table.getName}.$name, ${references.table.getName}.${references.name})"
}

case class TableField[A](name: String, table: Option[String] = None, alias: Option[String] = None) extends Field[A] {
  override def ref: Fragment = const0(alias.getOrElse(table.map(_ + ".").getOrElse("") + name))

  override def value: Fragment = const0(table.map(_ + ".").getOrElse("") + name)

  override def as(alias: String): TableField[A] = copy(alias = Some(alias))
}

// Null fields, useful for UNION tables when a field is on one side
case class NullField[A](name: String) extends Field[A] {
  override val alias: Option[String] = Some(name)

  override def ref: Fragment = const0(name)

  override def value: Fragment = fr0"null"

  override def as(alias: String): NullField[A] = copy(name = alias)
}

case class QueryField[A](query: Query[A], alias: Option[String] = None) extends Field[A] {
  override val name: String = "(" + query.sql + ")"

  override def ref: Fragment = alias.map(const0(_)).getOrElse(query.fr)

  override def value: Fragment = fr0"(" ++ query.fr ++ fr0")"

  override def as(alias: String): QueryField[A] = copy(alias = Some(alias))
}

// fields using aggregation methods (COUNT, SUM...)
sealed trait AggField[A] extends Field[A]

object AggField {
  def apply[A](name: String): SimpleAggField[A] = SimpleAggField(name, None)

  def apply[A](name: String, alias: String): SimpleAggField[A] = SimpleAggField(name, Some(alias))

  def apply[A](query: Query[A]): QueryAggField[A] = QueryAggField(query, None)

  def apply[A](query: Query[A], alias: String): QueryAggField[A] = QueryAggField(query, Some(alias))
}

case class SimpleAggField[A](name: String, alias: Option[String] = None) extends AggField[A] {
  override def ref: Fragment = const0(alias.getOrElse(name))

  override def value: Fragment = const0(name)

  override def as(alias: String): AggField[A] = copy(alias = Some(alias))
}

case class QueryAggField[A](query: Query[A], alias: Option[String] = None) extends AggField[A] {
  override val name: String = "(" + query.sql + ")"

  override def ref: Fragment = alias.map(const0(_)).getOrElse(query.fr)

  override def value: Fragment = fr0"(" ++ query.fr ++ fr0")"

  override def as(alias: String): QueryAggField[A] = copy(alias = Some(alias))
}
