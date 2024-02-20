package com.snappyShots

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

abstract class AsyncUnitSpec
  extends AsyncFunSpec
  with AsyncMockFactory
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with Matchers
