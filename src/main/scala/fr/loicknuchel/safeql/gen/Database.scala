package fr.loicknuchel.safeql.gen

/**
 * This is the database representation models.
 *
 * They holds all the needed informations to generate tables.
 */

case class Database(schemas: List[Database.Schema])

object Database {

  case class Schema(name: String,
                    tables: List[Table])

  case class Table(schema: String,
                   name: String,
                   fields: List[Field])

  case class Field(schema: String,
                   table: String,
                   name: String,
                   jdbcType: Int,
                   jdbcTypeName: String,
                   jdbcTypeDeclaration: String,
                   nullable: Boolean,
                   index: Int,
                   defaultValue: Option[String],
                   ref: Option[FieldRef]) // assume foreign keys have only one field and link to only one table

  case class FieldRef(schema: String, table: String, field: String)

}
