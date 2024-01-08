name := """auth-service"""
organization := "com.authService"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.12"

// Code coverage settings
coverageFailOnMinimum := true
coverageMinimumStmtTotal := 90
coverageMinimumBranchTotal := 90
coverageExcludedFiles := ".*\\/target\\/.*"

val jwtScalaVersion = "9.4.4"
libraryDependencies ++= Seq(
  guice,
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
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test
)

enablePlugins(FlywayPlugin)

flywayUrl := s"jdbc:postgresql://localhost:5432/${sys.env.getOrElse("SNAPPY_SHOTS_DB_NAME", "db_name")}"
flywayUser := sys.env.getOrElse("SNAPPY_SHOTS_DB_USER", "db_user")
flywayPassword := sys.env.getOrElse("SNAPPY_SHOTS_DB_PASSWORD", "db_password")
flywayLocations := Seq(s"filesystem:conf/db/migration/${sys.env.getOrElse("SNAPPY_SHOTS_DB_NAME", "db_name")}")
Test / flywayUrl := s"jdbc:postgresql://localhost:5432/${sys.env.getOrElse("SNAPPY_SHOTS_DB_NAME", "db_name")}_test"
Test / flywayUser := sys.env.getOrElse("SNAPPY_SHOTS_DB_USER", "db_user")
Test / flywayPassword := sys.env.getOrElse("SNAPPY_SHOTS_DB_PASSWORD", "db_password")
flywayBaselineOnMigrate := true
flywayBaselineDescription := "Lets go!"
flywayInstalledBy := "Sam"
