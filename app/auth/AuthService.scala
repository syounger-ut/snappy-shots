package auth

import java.time.Clock
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtHeader, JwtOptions}

import scala.util.{Failure, Success, Try}

class AuthService {
  implicit val clock: Clock = Clock.systemUTC

  def createToken(): String = {
    Jwt.encode("""{"user":1}""", "secretKey", JwtAlgorithm.HS256)
  }

  def decodeToken(token: String): Try[(String, String, String)] = {
    Jwt.decodeRawAll(token, "secretKey", Seq(JwtAlgorithm.HS256))
  }

  def validateToken(token: String): Try[(String, String, String)] = {
    if (Jwt.isValid(token, "secretKey", Seq(JwtAlgorithm.HS256))) {
      decodeToken(token)
    } else {
      Failure(new Exception("JWT did not pass validation"))
    }
  }
}