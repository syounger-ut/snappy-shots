package com.authService.utils

import slick.jdbc.{H2Profile, JdbcProfile, PostgresProfile}

object SlickDBDriver {
  private val TEST = "test"
  private val PRODUCTION = "production"

  def getDriver: JdbcProfile = {
    scala.util.Properties.envOrElse("ENVIRONMENT", "production") match {
      case TEST => H2Profile
      case PRODUCTION => PostgresProfile
      case _ => PostgresProfile
    }
  }
}
