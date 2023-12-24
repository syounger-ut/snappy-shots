package com.authService.auth

import pdi.jwt.{Jwt, JwtAlgorithm}

import java.time.Clock
import scala.util.{Failure, Try}

// $COVERAGE-OFF$
class AuthService {
  implicit val clock: Clock = Clock.systemUTC

  def createToken(userId: Long): String = {
    Jwt.encode(s"""{"user":${userId}""", "secretKey", JwtAlgorithm.HS256)
  }

  def validateToken(token: String): Try[(String, String, String)] = {
    if (Jwt.isValid(token, "secretKey", Seq(JwtAlgorithm.HS256))) {
      decodeToken(token)
    } else {
      Failure(new Exception("JWT did not pass validation"))
    }
  }

  private def decodeToken(token: String): Try[(String, String, String)] = {
    Jwt.decodeRawAll(token, "secretKey", Seq(JwtAlgorithm.HS256))
  }
}
// $COVERAGE-ON$
