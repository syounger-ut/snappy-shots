package com.authService.auth

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

import java.time.Clock
import scala.util.{Failure, Try}

// $COVERAGE-OFF$
class AuthService {
  implicit val clock: Clock = Clock.systemUTC
  private val ONE_DAY_SECONDS = 60 * 60 * 24

  def createToken(userId: Long): String = {
    val claim =
      JwtClaim(s"""{"user_id":${userId}}""").expiresIn(ONE_DAY_SECONDS)
    Jwt.encode(claim, "secretKey", JwtAlgorithm.HS256)
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
