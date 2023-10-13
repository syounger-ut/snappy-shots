package com.authService

import org.scalatest.funspec.AnyFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach

abstract class UnitSpec
  extends AnyFunSpec
  with MockFactory
  with BeforeAndAfterEach
