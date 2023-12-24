package com.authService

object Connection {
  import slick.jdbc.PostgresProfile.api._

  val db = Database.forConfig("auth_service_db")
}
