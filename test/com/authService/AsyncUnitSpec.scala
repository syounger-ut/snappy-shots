package com.authService

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AsyncFunSpec

abstract class AsyncUnitSpec extends AsyncFunSpec with AsyncMockFactory with BeforeAndAfterEach
