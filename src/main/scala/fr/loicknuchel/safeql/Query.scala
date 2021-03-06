package fr.loicknuchel.safeql

import java.time.Instant

import cats.data.NonEmptyList
import cats.effect.IO
import doobie.syntax.connectionio._
import doobie.syntax.string._
import doobie.util.fragment.Fragment
import doobie.util.fragment.Fragment.const0
import doobie.util.{Put, Read}
import fr.loicknuchel.safeql.Field.Order
import fr.loicknuchel.safeql.Query.Inner._
import fr.loicknuchel.safeql.Table.Sort
import fr.loicknuchel.safeql.models._
import fr.loicknuchel.safeql.utils.Extensions._

import scala.util.control.NonFatal

sealed trait Query[A] {
  def fr: Fragment

  def sql: String = fr.query.sql

  def run(xa: doobie.Transactor[IO]): IO[A]
}

object Query {

  case class Insert[T <: Table.SqlTable](table: T, fields: List[SqlField[_, T]], values: Fragment) extends Query[Unit] {
    def fr: Fragment = const0(s"INSERT INTO ${table.getName} (${fields.map(_.name).mkString(", ")}) VALUES (") ++ values ++ fr0")"

    def run(xa: doobie.Transactor[IO]): IO[Unit] =
      exec(fr, _.update.run, xa).flatMap {
        case 1 => IO.pure(())
        case rows => IO.raiseError(new Exception(s"Insert affected $rows rows instead of 1 for table $table"))
      }
  }

  object Insert {

    case class Builder[T <: Table.SqlTable](private val table: T, private val fields: List[SqlField[_, T]]) {
      def fields(fields: List[SqlField[_, T]]): Builder[T] = copy(fields = fields)

      def fields(fields: SqlField[_, T]*): Builder[T] = this.fields(fields.toList)

      // FIXME 2020-10-15: temporary hack waiting I solve the Put[Option[A]] problem (cf https://gist.github.com/loicknuchel/2297d612b58b399395bdd08d3c6dd217)
      def values(fr: Fragment): Insert[T] = Insert(table, fields, fr)

      def values[A: Put](a: A): Insert[T] = build(1, fr0"$a")

      def values[A: Put, B: Put](a: A, b: B): Insert[T] = build(2, fr0"$a, $b")

      def values[A: Put, B: Put, C: Put](a: A, b: B, c: C): Insert[T] = build(3, fr0"$a, $b, $c")

      def values[A: Put, B: Put, C: Put, D: Put](a: A, b: B, c: C, d: D): Insert[T] = build(4, fr0"$a, $b, $c, $d")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put](a: A, b: B, c: C, d: D, e: E): Insert[T] = build(5, fr0"$a, $b, $c, $d, $e")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put](a: A, b: B, c: C, d: D, e: E, f: F): Insert[T] = build(6, fr0"$a, $b, $c, $d, $e, $f")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G): Insert[T] = build(7, fr0"$a, $b, $c, $d, $e, $f, $g")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H): Insert[T] = build(8, fr0"$a, $b, $c, $d, $e, $f, $g, $h")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I): Insert[T] = build(9, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J): Insert[T] = build(10, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K): Insert[T] = build(11, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L): Insert[T] = build(12, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M): Insert[T] = build(13, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N): Insert[T] = build(14, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O): Insert[T] = build(15, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P): Insert[T] = build(16, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q): Insert[T] = build(17, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R): Insert[T] = build(18, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S): Insert[T] = build(19, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U): Insert[T] = build(20, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put, V: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U, v: V): Insert[T] = build(21, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u, $v")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put, V: Put, W: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U, v: V, w: W): Insert[T] = build(22, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u, $v, $w")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put, V: Put, W: Put, X: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U, v: V, w: W, x: X): Insert[T] = build(23, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u, $v, $w, $x")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put, V: Put, W: Put, X: Put, Y: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U, v: V, w: W, x: X, y: Y): Insert[T] = build(24, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u, $v, $w, $x, $y")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put, V: Put, W: Put, X: Put, Y: Put, Z: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U, v: V, w: W, x: X, y: Y, z: Z): Insert[T] = build(25, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u, $v, $w, $x, $y, $z")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put, V: Put, W: Put, X: Put, Y: Put, Z: Put, AA: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U, v: V, w: W, x: X, y: Y, z: Z, aa: AA): Insert[T] = build(26, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u, $v, $w, $x, $y, $z, $aa")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put, V: Put, W: Put, X: Put, Y: Put, Z: Put, AA: Put, AB: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U, v: V, w: W, x: X, y: Y, z: Z, aa: AA, ab: AB): Insert[T] = build(27, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u, $v, $w, $x, $y, $z, $aa, $ab")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put, V: Put, W: Put, X: Put, Y: Put, Z: Put, AA: Put, AB: Put, AC: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U, v: V, w: W, x: X, y: Y, z: Z, aa: AA, ab: AB, ac: AC): Insert[T] = build(28, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u, $v, $w, $x, $y, $z, $aa, $ab, $ac")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put, V: Put, W: Put, X: Put, Y: Put, Z: Put, AA: Put, AB: Put, AC: Put, AD: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U, v: V, w: W, x: X, y: Y, z: Z, aa: AA, ab: AB, ac: AC, ad: AD): Insert[T] = build(29, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u, $v, $w, $x, $y, $z, $aa, $ab, $ac, $ad")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put, V: Put, W: Put, X: Put, Y: Put, Z: Put, AA: Put, AB: Put, AC: Put, AD: Put, AE: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U, v: V, w: W, x: X, y: Y, z: Z, aa: AA, ab: AB, ac: AC, ad: AD, ae: AE): Insert[T] = build(30, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u, $v, $w, $x, $y, $z, $aa, $ab, $ac, $ad, $ae")

      def values[A: Put, B: Put, C: Put, D: Put, E: Put, F: Put, G: Put, H: Put, I: Put, J: Put, K: Put, L: Put, M: Put, N: Put, O: Put, P: Put, Q: Put, R: Put, S: Put, U: Put, V: Put, W: Put, X: Put, Y: Put, Z: Put, AA: Put, AB: Put, AC: Put, AD: Put, AE: Put, AF: Put](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L, m: M, n: N, o: O, p: P, q: Q, r: R, s: S, u: U, v: V, w: W, x: X, y: Y, z: Z, aa: AA, ab: AB, ac: AC, ad: AD, ae: AE, af: AF): Insert[T] = build(31, fr0"$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $u, $v, $w, $x, $y, $z, $aa, $ab, $ac, $ad, $ae, $af")

      private def build(size: Int, fr: Fragment): Insert[T] =
        if (size == fields.length) Insert(table, fields, fr)
        else throw InvalidNumberOfValues(table, fields, size)
    }

  }

  case class Update[T <: Table.SqlTable](table: T, values: List[Fragment], where: WhereClause) extends Query[Unit] {
    def fr: Fragment = fr0"UPDATE " ++ table.fr ++ fr0" SET " ++ values.mkFragment(", ") ++ where.fr

    def run(xa: doobie.Transactor[IO]): IO[Unit] =
      exec(fr, _.update.run, xa).flatMap {
        case 1 => IO.pure(())
        case rows => IO.raiseError(new Exception(s"Update affected $rows rows instead of 1 for table $table: ${fr.update.sql}"))
      }
  }

  object Update {

    case class Builder[T <: Table.SqlTable](private val table: T,
                                            private val values: List[Fragment]) {
      def set[A: Put](field: T => SqlField[A, T], value: A): Builder[T] = copy(values = values :+ (const0(field(table).name) ++ fr0"=$value"))

      def set[A: Put](field: T => SqlField[A, T], value: Option[A]): Builder[T] = {
        val f = field(table)
        if (f.nullable) copy(values = values :+ (const0(f.name) ++ fr0"=$value")) else throw new Exception(s"Can't use an Option for non nullable field $f")
      }

      def where(cond: Cond): Update[T] = Exceptions.check(cond, table, Update(table, values, WhereClause(Some(cond), None, None)))

      def where(cond: T => Cond): Update[T] = where(cond(table))
    }

  }

  case class Delete[T <: Table.SqlTable](table: T, where: WhereClause) extends Query[Unit] {
    def fr: Fragment = fr0"DELETE FROM " ++ table.fr ++ where.fr

    def run(xa: doobie.Transactor[IO]): IO[Unit] =
      exec(fr, _.update.run, xa).flatMap {
        case 1 => IO.pure(())
        case rows => IO.raiseError(new Exception(s"Delete affected $rows rows instead of 1 for table $table: ${fr.update.sql}"))
      }
  }

  object Delete {

    case class Builder[T <: Table.SqlTable](private val table: T) {
      def where(cond: Cond): Delete[T] = Exceptions.check(cond, table, Delete(table, WhereClause(Some(cond), None, None)))

      def where(cond: T => Cond): Delete[T] = where(cond(table))
    }

  }

  sealed trait Select[A] {
    val table: Table
    val fields: List[Field[_]]
    val where: WhereClause
    val groupBy: GroupByClause
    val having: HavingClause
    val orderBy: OrderByClause
    val limit: LimitClause
    val offset: OffsetClause

    def fr: Fragment = fr0"SELECT " ++ fields.map(_.fr).mkFragment(", ") ++ fr0" FROM " ++ table.fr ++ where.fr ++ groupBy.fr ++ having.fr ++ orderBy.fr ++ limit.fr ++ offset.fr
  }

  object Select {

    case class All[A: Read](table: Table, fields: List[Field[_]], where: WhereClause, groupBy: GroupByClause, having: HavingClause, orderBy: OrderByClause, limit: LimitClause, offset: OffsetClause) extends Select[A] with Query[List[A]] {
      def query: doobie.Query0[A] = fr.query[A]

      def run(xa: doobie.Transactor[IO]): IO[List[A]] = exec(fr, _.query[A].to[List], xa)
    }

    case class Paginated[A: Read](table: Table, fields: List[Field[_]], where: WhereClause, groupBy: GroupByClause, having: HavingClause, orderBy: OrderByClause, limit: LimitClause, offset: OffsetClause, params: Page.Params) extends Select[A] with Query[Page[A]] {
      def query: doobie.Query0[A] = fr.query[A]

      def countFr: Fragment = fr0"SELECT COUNT(*) FROM (SELECT " ++ fields.headOption.map(_.fr).getOrElse(fr0"*") ++ fr0" FROM " ++ table.fr ++ where.fr ++ groupBy.fr ++ having.fr ++ fr0") as cnt"

      def run(xa: doobie.Transactor[IO]): IO[Page[A]] = exec(fr, fr => for {
        elts <- fr.query[A].to[List]
        total <- countFr.query[Long].unique
      } yield Page(elts, params.defaultOrderBy(table.getSorts.headOption.map(_.slug).getOrElse("")), total), xa)
    }

    object Paginated {
      def apply[A: Read](table: Table, fields: List[Field[_]], where: WhereClause, groupBy: GroupByClause, having: HavingClause, orderBy: OrderByClause, params: Page.Params, ctx: Ctx): Paginated[A] =
        new Paginated(
          table,
          fields,
          where.copy(search = params.search.map(_ -> table.searchOn), filter = Some((params.filters, table.getFilters, ctx))),
          groupBy,
          having.copy(filter = Some((params.filters, table.getFilters, ctx))),
          orderBy.copy(order = params.orderByNel.map((_, table.getSorts, table.getFields)), nullsFirst = params.nullsFirst),
          LimitClause(params.pageSize),
          OffsetClause(params.offset),
          params)
    }

    case class Optional[A: Read](table: Table, fields: List[Field[_]], where: WhereClause, groupBy: GroupByClause, having: HavingClause, orderBy: OrderByClause, limit: LimitClause, offset: OffsetClause) extends Select[A] with Query[Option[A]] {
      def query: doobie.Query0[A] = fr.query[A]

      def run(xa: doobie.Transactor[IO]): IO[Option[A]] = exec(fr, _.query[A].option, xa)
    }

    case class One[A: Read](table: Table, fields: List[Field[_]], where: WhereClause, groupBy: GroupByClause, having: HavingClause, orderBy: OrderByClause, limit: LimitClause, offset: OffsetClause) extends Select[A] with Query[A] {
      def query: doobie.Query0[A] = fr.query[A]

      def run(xa: doobie.Transactor[IO]): IO[A] = exec(fr, _.query[A].unique, xa)
    }

    case class Exists[A: Read](table: Table, fields: List[Field[_]], where: WhereClause, groupBy: GroupByClause, having: HavingClause, orderBy: OrderByClause, limit: LimitClause, offset: OffsetClause) extends Select[A] with Query[Boolean] {
      def query: doobie.Query0[A] = fr.query[A]

      def run(xa: doobie.Transactor[IO]): IO[Boolean] = exec(fr, _.query[A].option.map(_.isDefined), xa)
    }

    case class Builder[T <: Table](private val table: T,
                                   private val fields: List[Field[_]],
                                   private val where: WhereClause,
                                   private val groupBy: GroupByClause,
                                   private val having: HavingClause,
                                   private val orderBy: OrderByClause,
                                   private val limit: LimitClause,
                                   private val offset: OffsetClause) {
      def fields(fields: List[Field[_]]): Builder[T] = Exceptions.check(fields, table, copy(fields = fields))

      def fields(fields: Field[_]*): Builder[T] = this.fields(fields.toList)

      def prependFields(fields: Field[_]*): Builder[T] = this.fields(fields.toList ++ this.fields)

      def appendFields(fields: Field[_]*): Builder[T] = this.fields(this.fields ++ fields.toList)

      def dropFields(p: Field[_] => Boolean): Builder[T] = this.fields(fields.filterNot(p))

      def dropFields(fields: List[Field[_]]): Builder[T] = dropFields(fields.contains(_))

      def dropFields(fields: Field[_]*): Builder[T] = dropFields(fields.contains(_))

      def withFields(fns: (T => Field[_])*): Builder[T] = this.fields(fns.map(f => f(table)).toList)

      def withoutFields(fns: (T => Field[_])*): Builder[T] = dropFields(fns.map(f => f(table)).toList)

      // unsafe option is useful when a nested queries use a parent field, there is no way to track this right now as it's built independently
      def where(cond: Cond, unsafe: Boolean = false): Builder[T] =
        if (unsafe) copy(where = WhereClause(Some(cond), None, None)) else Exceptions.check(cond, table, copy(where = WhereClause(Some(cond), None, None)))

      def where(cond: T => Cond): Builder[T] = where(cond(table))

      def where(cond: T => Cond, unsafe: Boolean): Builder[T] = where(cond(table), unsafe)

      def groupBy(fields: SqlField[_, Table.SqlTable]*): Builder[T] = Exceptions.check(fields.toList, table, copy(groupBy = GroupByClause(fields.toList)))

      def orderBy(fields: Field.Order[_]*): Builder[T] = Exceptions.check(fields.map(_.field).toList, table, copy(orderBy = orderBy.copy(fields = fields.toList)))

      def limit(e: Expr): Builder[T] = copy(limit = LimitClause(Some(e)))

      def limit(i: Long): Builder[T] = limit(Expr.Value(i))

      def offset(e: Expr): Builder[T] = copy(offset = OffsetClause(Some(e)))

      def offset(i: Long): Builder[T] = offset(Expr.Value(i))

      def union[T2 <: Table](other: Builder[T2], alias: Option[String] = None, sorts: List[(String, String, List[String])] = List(), search: List[String] = List()): Table.UnionTable = {
        if (fields.length != other.fields.length) throw new Exception(s"Field number do not match (${fields.length} vs ${other.fields.length})")
        val invalidFields = fields.zip(other.fields).filter { case (f1, f2) => f1.alias.getOrElse(f1.name) != f2.alias.getOrElse(f2.name) } // FIXME check also match of sql type (should be added)
        if (invalidFields.nonEmpty) throw new Exception(s"Some field names do not match: ${invalidFields.map { case (f1, f2) => f1.name + " != " + f2.name }.mkString(", ")}")

        val getFields = fields.map(f => TableField(f.alias.getOrElse(f.name), alias))
        val duplicateFieldName = getFields.groupBy(_.name).filter(_._2.length > 1)
        if (duplicateFieldName.nonEmpty) throw new Exception(s"Some fields have duplicate name: ${duplicateFieldName.keys.map("'" + _ + "'").mkString(", ")}")

        val (invalidSorts, validSorts) = sorts.partition(s => s._3.isEmpty || s._3.exists(name => !getFields.exists(_.name == name.stripPrefix("-"))))
        if (invalidSorts.nonEmpty) throw new Exception(s"Sorts ${invalidSorts.map(_._1).mkString(", ")} can't have empty list")
        val getSorts = validSorts.map { case (slug, label, fields) => Sort(slug, label, NonEmptyList.fromListUnsafe(fields).map(name => Field.Order(name, alias))) }

        val (validSearch, invalidSearch) = search.partition(s => getFields.exists(_.name == s))
        if (invalidSearch.nonEmpty) throw new Exception(s"Search fields ${invalidSearch.mkString(", ")} does not exists in table ${table.sql}")
        val searchOn = validSearch.flatMap(s => getFields.find(_.name == s))

        Table.UnionTable(
          select1 = this.select,
          select2 = other.select,
          alias = alias,
          getFields = getFields,
          getSorts = getSorts,
          searchOn = searchOn,
          getFilters = List())
      }

      def all[A: Read]: Select.All[A] = build[A, Select.All[A]](new Select.All[A](table, fields, where, groupBy, having, orderBy, limit, offset))

      def page[A: Read](params: Page.Params, ctx: Ctx): Select.Paginated[A] = build[A, Select.Paginated[A]](Select.Paginated[A](table, fields, where, groupBy, having, orderBy, params, ctx))

      def option[A: Read](limit: Boolean = false): Select.Optional[A] = build[A, Select.Optional[A]](new Select.Optional[A](table, fields, where, groupBy, having, orderBy, if (limit) LimitClause(1) else this.limit, offset))

      def option[A: Read]: Select.Optional[A] = option[A]()

      def one[A: Read]: Select.One[A] = build[A, Select.One[A]](new Select.One[A](table, fields, where, groupBy, having, orderBy, limit, offset))

      def exists[A: Read]: Select.Exists[A] = build[A, Select.Exists[A]](new Select.Exists[A](table, fields, where, groupBy, having, orderBy, limit, offset))

      private def select[A: Read]: Select[_] = {
        val that = this
        new Select[Nothing] {
          override val table: Table = that.table
          override val fields: List[Field[_]] = that.fields
          override val where: WhereClause = that.where
          override val groupBy: GroupByClause = that.groupBy
          override val having: HavingClause = that.having
          override val orderBy: OrderByClause = that.orderBy
          override val limit: LimitClause = that.limit
          override val offset: OffsetClause = that.offset
        }
      }

      private def build[A: Read, S <: Select[A]](res: => S): S =
        if (implicitly[Read[A]].length == fields.length) res else throw InvalidNumberOfFields(implicitly[Read[A]], fields)
    }

  }

  trait Ctx {
    val now: Instant
  }

  object Ctx {

    final case class Basic(now: Instant) extends Ctx

  }

  object Inner {

    case class WhereClause(cond: Option[Cond], search: Option[(String, List[Field[_]])], filter: Option[(Map[String, String], List[Table.Filter], Ctx)]) {
      def fr: Fragment = {
        List(
          cond,
          computeFilters(filter, aggregation = false),
          search.filter(_._2.nonEmpty).flatMap { case (s, f) => f.map(_.ilike("%" + s + "%")).mk(_ or _) }
        ).flatten match {
          case List() => fr0""
          case List(clause) => fr0" WHERE " ++ clause.fr
          case clauses => fr0" WHERE " ++ clauses.map(_.par).mk(_ and _).get.fr // .get is safe thanks to previous `case List()` (non empty List)
        }
      }

      def sql: String = fr.query.sql
    }

    case class GroupByClause(fields: List[Field[_]]) {
      def fr: Fragment = NonEmptyList.fromList(fields).map(fr0" GROUP BY " ++ _.map(_.ref).mkFragment(", ")).getOrElse(fr0"")

      def sql: String = fr.query.sql
    }

    case class HavingClause(cond: Option[Cond], filter: Option[(Map[String, String], List[Table.Filter], Ctx)]) {
      def fr: Fragment = {
        List(
          cond,
          computeFilters(filter, aggregation = true)
        ).flatten match {
          case List() => fr0""
          case List(clause) => fr0" HAVING " ++ clause.fr
          case clauses => fr0" HAVING " ++ clauses.map(_.par).mk(_ and _).get.fr // .get is safe thanks to previous `case List()` (non empty List)
        }
      }

      def sql: String = fr.query.sql
    }

    case class OrderByClause(fields: List[Field.Order[_]], order: Option[(NonEmptyList[String], List[Sort], List[Field[_]])], nullsFirst: Boolean) {
      def fr: Fragment = {
        order.map { case (orderBy, sorts, fields) =>
          build(orderBy.toList.flatMap { order =>
            val name = order.stripPrefix("-")
            val asc = !order.startsWith("-")
            sorts.find(_.slug == name).map { sort =>
              sort.fields.map(o => if (asc) o else o.reverse).toList
            }.getOrElse {
              fields.find(_.name == name).map(Order(_, asc)).toList
            }
          }, nullsFirst)
        }.getOrElse(build(this.fields, nullsFirst))
      }

      def sql: String = fr.query.sql

      private def build(fields: List[Field.Order[_]], nullsFirst: Boolean): Fragment = NonEmptyList.fromList(fields).map(fr0" ORDER BY " ++ _.map(_.fr(nullsFirst)).mkFragment(", ")).getOrElse(fr0"")
    }

    object OrderByClause {
      def apply(fields: List[Order[_]]): OrderByClause = new OrderByClause(fields, None, false)
    }

    case class LimitClause(expr: Option[Expr]) {
      def fr: Fragment = expr.map {
        case Expr.Value(v: Long) => fr0" LIMIT " ++ const0(v.toString)
        case e => fr0" LIMIT " ++ e.fr
      }.getOrElse(fr0"")

      def sql: String = fr.query.sql
    }

    object LimitClause {
      def apply(v: Long): LimitClause = new LimitClause(Some(Expr.Value(v)))
    }

    case class OffsetClause(expr: Option[Expr]) {
      def fr: Fragment = expr.map {
        case Expr.Value(v: Long) => fr0" OFFSET " ++ const0(v.toString)
        case e => fr0" OFFSET " ++ e.fr
      }.getOrElse(fr0"")

      def sql: String = fr.query.sql
    }

    object OffsetClause {
      def apply(v: Long): OffsetClause = new OffsetClause(Some(Expr.Value(v)))
    }

    private def computeFilters(filter: Option[(Map[String, String], List[Table.Filter], Ctx)], aggregation: Boolean): Option[Cond] =
      filter.flatMap { case (filterBy, filters, ctx) =>
        filters.filter(_.aggregation == aggregation).flatMap(f => filterBy.get(f.key).flatMap(f.filter(_)(ctx))) match {
          case List() => None
          case List(c) => Some(c)
          case conds => conds.map(_.par).mk(_ and _)
        }
      }

  }

  private def exec[A](fr: Fragment, run: Fragment => doobie.ConnectionIO[A], xa: doobie.Transactor[IO]): IO[A] =
    run(fr).transact(xa).recoverWith { case NonFatal(e) => IO.raiseError(FailedQuery(fr, e)) }
}
