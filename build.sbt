import org.scalajs.linker.interface.ModuleSplitStyle

lazy val commonSettings = Seq(
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.13.12"
)

lazy val snappyShots = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(
    name := "snappyShots",
    commonSettings
  )

val jwtScalaVersion = "9.4.4"
lazy val server = snappyShots.jvm
  .enablePlugins(FlywayPlugin, PlayScala)
  .settings(
    organization := "com.snappyShots",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.12",
    // Code coverage settings
    coverageFailOnMinimum := true,
    coverageMinimumStmtTotal := 90,
    coverageMinimumBranchTotal := 90,
    coverageExcludedFiles := ".*\\/target\\/.*",
    libraryDependencies ++= Seq(
      guice,
      "com.amazonaws" % "aws-java-sdk" % "1.12.646",
      "org.postgresql" % "postgresql" % "42.6.0",
      "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
      "com.github.jwt-scala" %% "jwt-play" % jwtScalaVersion,
      "com.typesafe.slick" %% "slick" % "3.4.1",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
      "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",
      "org.slf4j" % "slf4j-nop" % "2.0.9" % Test,

      // Test dependencies
      "org.scalatest" %% "scalatest" % "3.2.17" % Test,
      "org.scalamock" %% "scalamock" % "5.2.0" % Test,
      "com.h2database" % "h2" % "1.4.200" % Test
    ),
    flywayUrl := s"jdbc:postgresql://localhost:5432/${sys.env.getOrElse("SNAPPY_SHOTS_DB_NAME", "db_name")}",
    flywayUser := sys.env.getOrElse("SNAPPY_SHOTS_DB_USER", "db_user"),
    flywayPassword := sys.env
      .getOrElse("SNAPPY_SHOTS_DB_PASSWORD", "db_password"),
    flywayLocations := Seq(
      s"filesystem:jvm/conf/db/migration/${sys.env.getOrElse("SNAPPY_SHOTS_DB_NAME", "db_name")}"
    ),
    flywayBaselineOnMigrate := true,
    flywayBaselineDescription := "Lets go!",
    flywayInstalledBy := "Sam",
    envVars := Map(
      "ENVIRONMENT" -> sys.env.getOrElse("ENVIRONMENT", "production")
    ),

    // Test setup
    Test / fork := true,
    Test / envVars := Map("ENVIRONMENT" -> "test"),
    Test / flywayUrl := s"jdbc:h2:./jvm/test/db/${sys.env.getOrElse("SNAPPY_SHOTS_DB_NAME", "db_name")}_test;MODE=PostgreSQL;DATABASE_TO_UPPER=false;AUTO_SERVER=true",
    Test / flywayUser := "test_user",
    Test / flywayPassword := "test_password",
    Test / flywayLocations := Seq(
      s"filesystem:jvm/conf/db/migration/${sys.env.getOrElse("SNAPPY_SHOTS_DB_NAME", "db_name")}"
    )
  )

lazy val client = snappyShots.js
  .enablePlugins(ScalaJSPlugin) // Enable the Scala.js plugin in this project
  .settings(
    scalaVersion := "3.2.2",
    // Code coverage settings
    coverageEnabled := false,

    // Tell Scala.js that this is an application with a main method
    scalaJSUseMainModuleInitializer := true,

    /* Configure Scala.js to emit modules in the optimal way to
     * connect to Vite's incremental reload.
     * - emit ECMAScript modules
     * - emit as many small modules as possible for classes in the "livechart" package
     * - emit as few (large) modules as possible for all other classes
     *   (in particular, for the standard library)
     */
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("snappyShots"))
        )
    },

    /* Depend on the scalajs-dom library.
     * It provides static types for the browser DOM APIs.
     */
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.4.0",
      "com.raquo" %%% "laminar" % "15.0.1",
      "com.github.japgolly.scalacss" %%% "core" % "1.0.0-RC2",
      "com.lihaoyi" %%% "upickle" % "3.0.0"
    )
  )
