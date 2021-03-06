package fr.loicknuchel.safeql.models

import cats.data.NonEmptyList
import doobie.util.Read
import doobie.util.fragment.Fragment
import fr.loicknuchel.safeql.{Cond, Field, SqlField, Table}

object Exceptions {
  def check[A, T <: Table](fields: List[Field[_]], table: T, value: => A): A =
    NonEmptyList.fromList(
      fields
        .collect { case f: SqlField[_, Table.SqlTable] => f }
        .filterNot(table.has)
    ).map(unknownFields => throw UnknownTableFields(table, unknownFields)).getOrElse(value)

  def check[A, T <: Table](cond: Cond, table: T, value: => A): A = check(cond.getFields, table, value)

  def check[T <: Table](cond: Cond, table: T): T = check(cond.getFields, table, table)
}

case class UnknownTableFields[T <: Table](table: T, unknownFields: NonEmptyList[Field[_]])
  extends Exception(s"Fields ${unknownFields.toList.map("'" + _.sql + "'").mkString(", ")} do not belong to the table '${table.sql}'")

case class ConflictingTableFields[T <: Table](table: T, name: String, conflictingFields: NonEmptyList[Field[_]])
  extends Exception(s"Table '${table.sql}' has multiple fields with name '$name': ${conflictingFields.toList.map("'" + _.sql + "'").mkString(", ")}")

case class InvalidNumberOfValues[T <: Table.SqlTable](table: T, fields: List[SqlField[_, T]], expectedLength: Int)
  extends Exception(s"Insert in '${table.sql}' has $expectedLength values but expects ${fields.length} (${fields.map(_.sql).mkString(", ")})")

case class InvalidNumberOfFields[A](read: Read[A], fields: List[Field[_]])
  extends Exception(s"Expects ${read.length} fields but got ${fields.length}: ${fields.map(_.sql).mkString(", ")}")

case class FailedQuery(fr: Fragment, cause: Throwable)
  extends Exception(s"Fail on ${fr.query.sql}: ${cause.getMessage}", cause)

case class FailedScript(script: String, cause: Throwable)
  extends Exception(s"Script has an error: ${cause.getMessage}", cause)

case class NotImplementedJoin[T <: Table, T2 <: Table](t: T, t2: T2)
  extends Exception(s"Join between ${t.sql} and ${t2.sql} is not implemented")

case class MultiException(errs: NonEmptyList[Throwable]) extends RuntimeException {
  override def getMessage: String = errs.toList.map(e => s"\n  - ${e.getMessage}").mkString

  override def getLocalizedMessage: String = errs.toList.map(e => s"\n  - ${e.getLocalizedMessage}").mkString

  override def getStackTrace: Array[StackTraceElement] = errs.head.getStackTrace

  override def getCause: Throwable = errs.head.getCause
}

object MultiException {
  def apply(err: Throwable, others: Throwable*): MultiException = new MultiException(NonEmptyList(err, others.toList))
}
