package com.authService

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

abstract class AsyncUnitSpec
  extends AsyncFunSpec
  with AsyncMockFactory
  with BeforeAndAfterEach
  with Matchers
