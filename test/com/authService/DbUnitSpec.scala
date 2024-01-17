package com.authService

import com.authService.utils.{Connection, SlickDBDriver}
import org.scalatest.concurrent.ScalaFutures

class DbUnitSpec extends AsyncUnitSpec with ScalaFutures {
  protected val profile = SlickDBDriver.getDriver

  protected val db = new Connection(profile).db()

  override def beforeEach(): Unit = {
    val session = db.createSession()
    session.createStatement().execute("SET REFERENTIAL_INTEGRITY TO FALSE");
    session.createStatement().execute("TRUNCATE TABLE \"users\" RESTART IDENTITY");
    session.createStatement().execute("ALTER TABLE \"users\" ALTER COLUMN \"id\" RESTART WITH 1");
    session.createStatement().execute("TRUNCATE TABLE \"photos\" RESTART IDENTITY");
    session.createStatement().execute("ALTER TABLE \"photos\" ALTER COLUMN \"id\" RESTART WITH 1");
    session.createStatement().execute("SET REFERENTIAL_INTEGRITY TO TRUE");
  }

  override def afterAll(): Unit = {
    db.close()
  }
}
