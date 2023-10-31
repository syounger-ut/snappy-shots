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
  evolutions,
  guice,
  jdbc,
  "org.postgresql" % "postgresql" % "42.6.0",
  "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
  "com.github.jwt-scala" %% "jwt-play" % jwtScalaVersion,
  
  // Test dependencies
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,
  "org.scalamock" %% "scalamock" % "5.2.0" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test
)
