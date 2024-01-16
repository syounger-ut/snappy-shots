package com.authService.utils

import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcProfile

class Connection(override val profile: JdbcProfile) extends Profile {
  import slick.jdbc.PostgresProfile.api._

  def db(): Database = {
    val env = scala.util.Properties.envOrElse("ENVIRONMENT", "production")
    val config = ConfigFactory.load(env)
    val url = config.getString("db.url")
    val username = config.getString("db.username")
    val password = config.getString("db.password")
    val driver = config.getString("db.driver")
    Database.forURL(url, username, password, null, driver)
  }
}
