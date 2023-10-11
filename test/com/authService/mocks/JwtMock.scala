package com.authService.mocks

import pdi.jwt.{Jwt, JwtAlgorithm}

import java.time.Clock

class JwtMock extends Jwt(Clock.systemUTC()) {
  override def encode(header: String, claim: String, algorithm: JwtAlgorithm): String = {
    "fake-jwt-token"
  }
}
