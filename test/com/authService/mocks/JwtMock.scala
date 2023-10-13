package com.authService.mocks

import pdi.jwt.algorithms.JwtHmacAlgorithm
import pdi.jwt.{Jwt, JwtAlgorithm}

import java.time.Clock
import scala.util.Try

class JwtMock extends Jwt(Clock.systemUTC()) {
  override def encode(
    header: String,
    claim: String,
    algorithm: JwtAlgorithm
  ): String = ???

  override def isValid(
    token: String,
    key: String,
    algorithms: Seq[JwtHmacAlgorithm]
  ): Boolean = ???

  override def decodeRawAll(
    token: String,
    key: String,
    algorithms: Seq[JwtHmacAlgorithm]
  ): Try[(String, String, String)] = ???
}
