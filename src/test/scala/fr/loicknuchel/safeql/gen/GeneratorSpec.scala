package fr.loicknuchel.safeql.gen

import java.util.UUID

import fr.loicknuchel.safeql.gen.Generator.{FlywayGenerator, ReaderGenerator, SQLFilesGenerator}
import fr.loicknuchel.safeql.gen.cli.CliCommandSpec
import fr.loicknuchel.safeql.gen.reader.H2Reader
import fr.loicknuchel.safeql.gen.writer.{ScalaWriter, Writer}
import fr.loicknuchel.safeql.testingutils.FileSpec
import fr.loicknuchel.safeql.utils.FileUtils
import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.jdbc.DriverDataSource

class GeneratorSpec extends FileSpec {
  protected val root = "target/tests-GeneratorSpec"
  private val writer: ScalaWriter = ScalaWriter(
    now = now,
    directory = "src/test/scala",
    packageName = "fr.loicknuchel.safeql.testingutils.database",
    identifierStrategy = Writer.IdentifierStrategy.UpperCase,
    config = CliCommandSpec.dbConf)
  // cli: `sbt "run gen --sql-files src/test/resources/sql_migrations/V1__test_schema.sql --output scala --dir src/test/scala --package fr.loicknuchel.safeql.testingutils.database --identifiers UpperCase --config src/test/resources/cli/db.conf"`

  describe("Generator") {
    it("should keep the generated database up to date") {
      // tip: if needed generate db files using the cli command (see above) and then set the generated time in `BaseSpec`
      val flywayWriter = writer.directory(s"$root/flyway-gen")
      Generator.flyway("classpath:sql_migrations").writer(flywayWriter).generate().unsafeRunSync()

      val flywayDb = FileUtils.getDirContent(flywayWriter.rootFolderPath).get
      val currentDb = FileUtils.getDirContent(writer.rootFolderPath).get
      currentDb.size shouldBe flywayDb.size
      flywayDb.foreach { case (path, content) => currentDb.getOrElse(path, "") shouldBe content }
    }
    it("should generate the same files with all the generators") {
      // Basic generation
      val reader = H2Reader(
        url = s"jdbc:h2:mem:${UUID.randomUUID()};MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
        schema = Some("PUBLIC"),
        excludes = Some(".*flyway.*"))
      Flyway.configure()
        .dataSource(new DriverDataSource(this.getClass.getClassLoader, reader.driver, reader.url, reader.user, reader.pass))
        .locations("classpath:sql_migrations")
        .load().migrate()
      val basicPath = s"$root/basic-gen"
      Generator.reader(reader).writer(writer.directory(basicPath)).generate().unsafeRunSync()
      val basicDb = FileUtils.getDirContent(basicPath).get

      // Flyway generator
      val flywapPath = s"$root/flyway-gen"
      Generator.flyway("classpath:sql_migrations").writer(writer.directory(flywapPath)).generate().unsafeRunSync()
      val flywayDb = FileUtils.getDirContent(flywapPath).get
      flywayDb shouldBe basicDb

      // SQL files generator
      val sqlFilesPath = s"$root/sql-gen"
      Generator.sqlFiles(List("src/test/resources/sql_migrations/V1__test_schema.sql")).writer(writer.directory(sqlFilesPath)).generate().unsafeRunSync()
      val sqlFilesDb = FileUtils.getDirContent(sqlFilesPath).get
      sqlFilesDb shouldBe basicDb
    }
    it("should set the writer before the reader") {
      val writer = ScalaWriter(now)
      val reader = H2Reader("url")

      Generator.writer(writer).reader(reader) shouldBe a[ReaderGenerator]
      Generator.writer(writer).flyway("classpath:sql_migrations") shouldBe a[FlywayGenerator]
      Generator.writer(writer).sqlFiles(List("migrations.sql")) shouldBe a[SQLFilesGenerator]
    }
    it("should update exclude") {
      val writer = ScalaWriter(now)
      val fb = Generator.flyway("classpath:sql_migrations")
      fb.reader.excludes shouldBe Some(".*flyway.*")
      fb.excludes("new").reader.excludes shouldBe Some("new")

      val f = fb.writer(writer)
      f.reader.excludes shouldBe Some(".*flyway.*")
      f.excludes("new").reader.excludes shouldBe Some("new")

      val sb = Generator.sqlFiles(List("migrations.scala"))
      sb.reader.excludes shouldBe None
      sb.excludes("new").reader.excludes shouldBe Some("new")

      val s = sb.writer(writer)
      s.reader.excludes shouldBe None
      s.excludes("new").reader.excludes shouldBe Some("new")
    }
  }
}
