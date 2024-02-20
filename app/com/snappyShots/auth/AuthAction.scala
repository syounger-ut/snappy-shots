package com.snappyShots.auth

import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

// A custom request type to hold our JWT claims, we can pass these on to the
// handling action
case class UserRequest[A](
  jwt: (String, String, String),
  token: String,
  request: Request[A],
  userId: Long
) extends WrappedRequest[A](request)

// Our custom action implementation
class AuthAction @Inject() (
  bodyParser: BodyParsers.Default,
  authService: AuthService
)(implicit ec: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] {

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec

  // A regex for parsing the Authorization header value
  private val headerTokenRegex = """Bearer (.+?)""".r

  // Called when a request is invoked. We should validate the bearer token here
  // and allow the request to proceed if it is valid.
  override def invokeBlock[A](
    request: Request[A],
    block: UserRequest[A] => Future[Result]
  ): Future[Result] =
    extractBearerToken(request) map { token =>
      authService.validateToken(token) match {
        case Success(claim) =>
          val userId = Try(parseUserId(claim))
          userId match {
            case Success(id) =>
              block(
                UserRequest(claim, token, request, id)
              ) // token was valid - proceed!
            case Failure(t) =>
              Future.successful(
                Results.Unauthorized(t.getMessage)
              ) // token was invalid - return 401
          }
        case Failure(t) =>
          Future.successful(
            Results.Unauthorized(t.getMessage)
          ) // token was invalid - return 401
      }
    } getOrElse Future.successful(
      Results.Unauthorized
    ) // no token was sent - return 401

  // Helper for extracting the token value
  private def extractBearerToken[A](request: Request[A]): Option[String] =
    request.headers.get(HeaderNames.AUTHORIZATION) collect {
      case headerTokenRegex(token) => token
    }

  private def parseUserId(jwt: (String, String, String)): Int = {
    Try(Json.parse(jwt._2)) match {
      case Success(json) => (json \ "user_id").get.as[Int]
      case Failure(_) =>
        throw new IllegalCallerException("JWT did not pass validation")
    }
  }
}
